package com.melody.makepatch;

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

    public static void main0(String[] args) {
	// write your code here

        if (args.length < 3)
        {
            throw new RuntimeException("paramater length is too short!");
        }

        String source = args[1];
        String target = args[2];
        File sourceFile = new File(source);
        File targetFile = new File(target);
        if (!sourceFile.exists())
        {
            throw new RuntimeException("source file is not exist: "+ source);
        }
        if (!targetFile.exists())
        {
            throw new RuntimeException("source file is not exist: "+ target);
        }

        File patchFile = new File(targetFile.getAbsoluteFile().getAbsolutePath()+".patch");
        patchFile.delete();
        Output output;
        try {
            output = new Output(new FileOutputStream(patchFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        byte[] sourceBytes;
        RandomFileInput input;
        try {
            sourceBytes = Files.readAllBytes(sourceFile.toPath());
//            InputStream inputStream = new FileInputStream(targetFile);
//            input = new Input(inputStream);

            input = new RandomFileInput(targetFile, "r");



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];


        ByteBlockItem byteBlockItem = new ByteBlockItem();

        MapBlockItem mapBlockItem = new MapBlockItem(sourceBytes);

        //搜索引擎
        IByteSearch search = new ForceSearch();

        long targetFileLength = targetFile.length();

        while (input.getPosition() < targetFileLength) {

            int readSize = 0;

            if (input.getPosition() % 1024 == 0) {
                JettLog.d("position", "" + input.getPosition());
            }

            try {
                readSize = input.read(readbuffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (readSize < 0)
            {
                //read end
            }else
            {
                if (readSize != readbuffer.length)
                {
                    //read end
                    readbuffer = Arrays.copyOfRange(readbuffer, 0, readSize);
                }

                int matchStart = search.search(sourceBytes, readbuffer);

                if (matchStart >= 0)
                {
                    //有找到的话继续进行比对
                    //TODO 是否需要全局查找最优匹配
                    int patLengh = readbuffer.length;
                    int start = matchStart+patLengh;

                    //找到部分相同后进行精确匹配
                    int matchSize = exactMatch(sourceBytes, start, input);
                    mapBlockItem.setStart(matchStart);
                    mapBlockItem.setSize(matchSize+patLengh);
                    JettLog.d("same byte", ""+mapBlockItem.getSize()+" start:"+matchStart);
                }
            }


            if (mapBlockItem.getSize() == 0)
            {
                //没有找到相同的块,一步一步向前推进
//                byteBlockItem.append(Arrays.copyOf(readbuffer, 1));
//                readPisition = readPisition - ( readSize - 1);
                //没有找到相同的块, 向前推进readbuffer的长度
                byteBlockItem.append(readbuffer);
//                readPisition = readPisition - ( readSize - 1);
            }
            else
            {
                //找到相同块，先把byteBlockItem存储下来
                if (byteBlockItem.getSize() > 0)
                {
                    output.writeInt(BlockItem.BLOCK_TYPE_BYTE, true);
                    output.writeLong(byteBlockItem.getSize(), true);
                    output.writeBytes(byteBlockItem.getOutput());
                }
                byteBlockItem.reset();

                //记录相同模块映射
                output.writeInt(BlockItem.BLOCK_TYPE_MAP, true);
                output.writeLong(mapBlockItem.getStart(), true);
                output.writeLong(mapBlockItem.getSize(), true);
                mapBlockItem.reset();
            }
        }

        if (byteBlockItem.getSize() > 0)
        {
            output.writeInt(BlockItem.BLOCK_TYPE_BYTE, true);
            output.writeLong(byteBlockItem.getSize(), true);
            output.writeBytes(byteBlockItem.getOutput());
            byteBlockItem.reset();
        }

        output.flush();

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
