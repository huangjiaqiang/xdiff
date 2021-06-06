package com.melody.makepatch;

import com.melody.JettLog;
import com.melody.algorithm.bintree.RBTree;
import com.melody.algorithm.bintree.RBTreeNode;
import com.melody.algorithm.search.ForceSearch;
import com.melody.algorithm.search.IByteSearch;
import com.melody.bean.BlockItem;
import com.melody.bean.ByteBlockItem;
import com.melody.bean.MapBlockItem;
import com.melody.io.Output;
import com.melody.io.RandomFileInput;
import com.melody.makepatch.bean.BlockNodeValue;
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
        RandomFileInput input;
        try {
            sourceBytes = Files.readAllBytes(sourceFile.toPath());
//            InputStream inputStream = new FileInputStream(targetFile);
//            input = new Input(inputStream);

            input = new RandomFileInput(targetFile, "r");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        RBTree<BlockNodeValue> sourceBlockTree = generateTree(sourceBytes);

        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];


        ByteBlockItem byteBlockItem = new ByteBlockItem();

        MapBlockItem mapBlockItem = new MapBlockItem(sourceBytes);


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
                    //有找到的话继续进行比对
                    //TODO 是否需要全局查找最优匹配
                    int patLengh = readbuffer.length;
                    int start = matchStart+patLengh;

                    //找到部分相同后进行精确匹配
                    int matchSize = exactMatch(sourceBytes, start, input);
                    mapBlockItem.setStart(matchStart);
                    mapBlockItem.setSize(matchSize+patLengh);
                    JettLog.d("same byte", ""+mapBlockItem.getSize()+" position:"+(input.getPosition() - mapBlockItem.getSize())+" start:"+matchStart);
                }else
                {
                    try {
                        input.seek(input.getPosition() - readSize + 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //没有找到相同的块,一步一步向前推进
                    byteBlockItem.append(readbuffer[0]);
//                readPisition = readPisition - ( readSize - 1);
                    //没有找到相同的块, 向前推进readbuffer的长度
//                byteBlockItem.append(readbuffer);
//                readPisition = readPisition - ( readSize - 1);
                }
            }


            if (mapBlockItem.getSize() != 0)
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
     * 初始化红黑树
     * @param sourceBytes
     */
    private static RBTree<BlockNodeValue> generateTree(byte[] sourceBytes)
    {
        //红黑树化
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
