package com.melody.mergepatch;

import com.melody.JettLog;
import com.melody.algorithm.search.ForceSearch;
import com.melody.algorithm.search.IByteSearch;
import com.melody.bean.BlockItem;
import com.melody.bean.ByteBlockItem;
import com.melody.bean.MapBlockItem;
import com.melody.io.Input;
import com.melody.io.Output;
import com.melody.io.RandomFileInput;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.RandomAccess;

public class Main {

    final static int COMPARE_SIZE_MIN = 16;

    public static void main(String[] args) {
	// write your code here

        if (args.length < 3)
        {
            throw new RuntimeException("paramater length is too short!");
        }

        String source = args[1];
        String target = args[2];
        File sourceFile = new File(source);
        File patchFile = new File(target);
        if (!sourceFile.exists())
        {
            throw new RuntimeException("source file is not exist: "+ source);
        }
        if (!patchFile.exists())
        {
            throw new RuntimeException("source file is not exist: "+ target);
        }

        File targetFile = new File(patchFile.getAbsoluteFile().getAbsolutePath()+".target");
        targetFile.delete();
        Output output;
        try {
            output = new Output(new FileOutputStream(targetFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        byte[] sourceBytes;
        Input input;

        MappedByteInput sourceInput;

        try {
//            sourceBytes = Files.readAllBytes(sourceFile.toPath());

            sourceInput = new MappedByteInput(sourceFile.getAbsolutePath());
            input = new Input(new FileInputStream(patchFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int blockNum = 0;

        while (true)
        {
            try {
                if (input.available() == 0)
                {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int type = input.readInt(true);
            byte[] outputBytes = null;
            if (type == BlockItem.BLOCK_TYPE_BYTE)
            {
                long blocksize = input.readLong(true);
                outputBytes = new byte[(int)blocksize];
                input.read(outputBytes);
            }else
            {
                long start = input.readLong(true);
                long blockSize = input.readLong(true);
                outputBytes = new byte[(int)blockSize];
                sourceInput.read(outputBytes, (int)start);
//                outputBytes = Arrays.copyOfRange(sourceBytes, (int)start, (int)(blockSize+start));
            }
            blockNum++;

            output.write(outputBytes);
        }

        System.out.println("block nums:"+blockNum);

        output.flush();
        sourceInput.close();

    }

    /**
     * 执行完全匹配，返回匹配的长度是多少
     * @param source
     * @param sourceStart
     * @param input
     * @return
     */
    private static int exactMatch(byte[] source, int sourceStart, RandomFileInput input)
    {
        int readSize = 0;
        int sameSize = 0;
        int offStart = 0;
        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];

        for (int i = sourceStart; i < source.length; i ++)
        {
            if (readSize <= 0)
            {
                try {
                    readSize = input.read(readbuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (readSize < 0)
                {
                    //read end;
                    break;
                }else if (readSize < readbuffer.length)
                {
                    readbuffer = Arrays.copyOfRange(readbuffer, 0, readSize);
                }
                offStart = 0;
            }


            //byte比对
            if (readbuffer[offStart++] == source[i]) {

                sameSize++;
                readSize--;
            }
            else
            {
                //回退到不相同的点
                if (readSize > 0) {
                    try {
                        input.seek(input.getPosition() - readSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return sameSize;
            }
        }
        return 0;
    }
}
