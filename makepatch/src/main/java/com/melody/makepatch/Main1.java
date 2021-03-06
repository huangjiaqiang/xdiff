package com.melody.makepatch;

import com.melody.algorithm.bintree.RBTree;
import com.melody.bean.BlockItem;
import com.melody.bean.ByteBlockItem;
import com.melody.bean.MapBlockItem;
import com.melody.io.Output;
import com.melody.io.RandomFileInput;
import com.melody.makepatch.bean.BlockNodeValue;
import com.melody.util.ExecUtil;
import com.melody.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Main1 {

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


        sourceBytes=  ExecUtil.exec2(() -> {
            try {
                return Files.readAllBytes(sourceFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        RandomFileInput input = ExecUtil.exec2(()->{
            try {
                return new RandomFileInput(targetFile, "r");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        RBTree<BlockNodeValue> sourceBlockTree = generateTree(sourceBytes);

        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];


        ByteBlockItem byteBlockItem = new ByteBlockItem();

        MapBlockItem mapBlockItem = new MapBlockItem();


        long targetFileLength = targetFile.length();

        while (input.getPosition() < targetFileLength) {

            int readSize = 0;

            if (input.getPosition() % 1024 == 0) {
//                JettLog.d("position", "" + input.getPosition());
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

                long crc32 = Util.getCRC32Checksum(readbuffer, 0, readSize);
                BlockNodeValue tempValue = new BlockNodeValue(0, 0, crc32);
                BlockNodeValue retValue = sourceBlockTree.find(tempValue);
//                int matchStart = search.search(sourceBytes, readbuffer);
                int matchStart = retValue!=null? retValue.getPosition():-1;


                boolean isMatch = false;
                if (matchStart >= 0)
                {
                    byte[] compareBytes = Arrays.copyOfRange(sourceBytes, retValue.getPosition(), retValue.getPosition()+retValue.getLength());
                    isMatch = Arrays.equals(readbuffer, compareBytes);
                }


                if (isMatch)
                {
                    //?????????????????????????????????
                    //TODO ????????????????????????????????????
                    int patLengh = readbuffer.length;
                    int start = matchStart+patLengh;

                    //???????????????????????????????????????
                    int matchSize = exactMatch(sourceBytes, start, input);
                    mapBlockItem.setStart(matchStart);
                    mapBlockItem.setSize(matchSize+patLengh);
//                    JettLog.d("same byte", ""+mapBlockItem.getSize()+" position:"+(input.getPosition() - mapBlockItem.getSize())+" start:"+matchStart);
                }else
                {
                    input.skip(- readSize + 1);

                    //????????????????????????,????????????????????????
                    byteBlockItem.append(readbuffer[0]);
//                readPisition = readPisition - ( readSize - 1);
                    //????????????????????????, ????????????readbuffer?????????
//                byteBlockItem.append(readbuffer);
//                readPisition = readPisition - ( readSize - 1);
                }
            }


            if (mapBlockItem.getSize() != 0)
            {
                //????????????????????????byteBlockItem????????????
                if (byteBlockItem.getSize() > 0)
                {
                    output.writeInt(BlockItem.BLOCK_TYPE_BYTE, true);
                    output.writeLong(byteBlockItem.getSize(), true);
                    output.writeBytes(byteBlockItem.getBytes());
                }
                byteBlockItem.reset();

                //????????????????????????
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
            output.writeBytes(byteBlockItem.getBytes());
            byteBlockItem.reset();
        }

        output.flush();

    }

    /**
     * ??????????????????
     * @param sourceBytes
     */
    private static RBTree<BlockNodeValue> generateTree(byte[] sourceBytes)
    {
        //????????????
        RBTree<BlockNodeValue> sourceBlockTree = new RBTree<>();
        int readPosition = 0;
        long sourceLength = sourceBytes.length;
        while (readPosition < sourceLength)
        {
            long remain = sourceLength - readPosition;
            int rsize = remain > COMPARE_SIZE_MIN? COMPARE_SIZE_MIN:(int)remain;

            long crc32 = Util.getCRC32Checksum(sourceBytes, readPosition, rsize);
            BlockNodeValue value = new BlockNodeValue(readPosition, rsize, crc32);
            sourceBlockTree.addNode(value);
            readPosition += rsize;
        }

        return sourceBlockTree;
    }

    /**
     * ???????????????????????????????????????????????????
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


            //byte??????
            if (readbuffer[offStart++] == source[i]) {

                sameSize++;
                readSize--;
            }
            else
            {
                //????????????????????????
                if (readSize > 0) {
                    input.skip(-readSize);
                }

                return sameSize;
            }
        }
        return 0;
    }
}
