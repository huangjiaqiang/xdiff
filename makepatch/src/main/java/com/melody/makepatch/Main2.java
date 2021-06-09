package com.melody.makepatch;

import com.melody.algorithm.bintree.RBTree;
import com.melody.bean.BlockItem;
import com.melody.bean.ByteBlockItem;
import com.melody.bean.MapBlockItem;
import com.melody.io.Output;
import com.melody.io.RandomFileInput;
import com.melody.makepatch.bean.BlockMapKey;
import com.melody.makepatch.bean.BlockNodeValue;
import com.melody.util.DiagnoseTool;
import com.melody.util.ExecUtil;
import com.melody.util.Util;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Main2 {

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
        RandomAccessFile sourceInput = null;
        try {
            sourceInput = new RandomAccessFile(sourceFile, "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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


//        byte[] sourceBytes;


//        sourceBytes=  ExecUtil.exec2(() -> {
//            try {
//                return Files.readAllBytes(sourceFile.toPath());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        RandomFileInput input = ExecUtil.exec2(()->{
            try {
                return new RandomFileInput(targetFile, "r");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];

        RandomAccessFile finalSourceInput = sourceInput;
        HashMap<BlockMapKey, Integer> sourceBlockMap = ExecUtil.exec2(()->{
            try {
                return generateMap(finalSourceInput);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            sourceInput.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBlockItem byteBlockItem = new ByteBlockItem();

        MapBlockItem mapBlockItem = new MapBlockItem();

        DiagnoseTool diagnoseTool = DiagnoseTool.obtain("1");


        /*
         * BlockItem数量　
         */
        int blockItemCount = 0;

        long targetFileLength = targetFile.length();

        while (input.getPosition() < targetFileLength) {

            int readSize = 0;

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

                int matchStart = -1;
                boolean isMatch = false;


                int hashcode = Arrays.hashCode(readbuffer);
                BlockMapKey key = new BlockMapKey(0, readSize, hashcode);

                while (!isMatch)
                {

                    Object startObj = sourceBlockMap.get(key);
                    if (startObj == null)
                    {
                        break;
                    }
                    matchStart = (int)startObj;

                    try {
                        sourceInput.seek(matchStart);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] compareBytes = new byte[key.getLength()];
                    int ret = 0;
                    try {
                        ret = sourceInput.read(compareBytes, 0, key.getLength());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (ret != key.getLength())
                    {
                        throw new RuntimeException("never happen!!");
                    }
                    isMatch = Arrays.equals(readbuffer, compareBytes);
                    key.setIndex(key.getIndex()+1);
                }

                if (isMatch)
                {
                    //有找到的话继续进行比对
                    //TODO 是否需要全局查找最优匹配
                    int patLengh = readbuffer.length;
                    int start = matchStart+patLengh;

                    //找到部分相同后进行精确匹配
                    int matchSize = 0;
                    try {
                        matchSize = exactMatch(sourceInput, start, input);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mapBlockItem.setStart(matchStart);
                    mapBlockItem.setSize(matchSize+patLengh);
//                    JettLog.d("same byte", ""+mapBlockItem.getSize()+" position:"+(input.getPosition() - mapBlockItem.getSize())+" start:"+matchStart);
                }else
                {
                    input.skip( - readSize + 1);
                    //没有找到相同的块,一步一步向前推进
                    byteBlockItem.append(readbuffer[0]);
                }
            }


            if (mapBlockItem.getSize() != 0)
            {
                //找到相同块，先把byteBlockItem存储下来
                if (byteBlockItem.getSize() > 0)
                {
//                    diagnoseTool.writeBlock(byteBlockItem.getSize());

                    output.writeInt(BlockItem.BLOCK_TYPE_BYTE, true);
                    output.writeLong(byteBlockItem.getSize(), true);
                    output.writeBytes(byteBlockItem.getBytes(), 0, (int)byteBlockItem.getSize());
                    byteBlockItem.reset();
                }


                //记录相同模块映射
//                diagnoseTool.writeBlock(mapBlockItem.getSize());


                output.writeInt(BlockItem.BLOCK_TYPE_MAP, true);
                output.writeLong(mapBlockItem.getStart(), true);
                output.writeLong(mapBlockItem.getSize(), true);
                mapBlockItem.reset();
            }
        }



        if (byteBlockItem.getSize() > 0)
        {
            diagnoseTool.writeBlock(byteBlockItem.getSize());

            output.writeInt(BlockItem.BLOCK_TYPE_BYTE, true);
            output.writeLong(byteBlockItem.getSize(), true);
            output.writeBytes(byteBlockItem.getBytes(), 0, (int)byteBlockItem.getSize());
            byteBlockItem.reset();
        }

        output.flush();
//        diagnoseTool.flush();
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
     * 获取map
     * @param sourceInput
     */
    private static HashMap<BlockMapKey, Integer> generateMap(RandomAccessFile sourceInput) throws IOException {
        /**
         * 用存存储相同的hashcode的块有多少个
         */
        long inputLength = sourceInput.length();
        int blockSize = (int)(inputLength/COMPARE_SIZE_MIN + 1);
        HashMap<Integer, Integer> blockNumMap = new HashMap<>(blockSize);
        HashMap<BlockMapKey, Integer> blockMap = new HashMap<>(blockSize);

        int readPosition = 0;
        byte[] buffer = new byte[COMPARE_SIZE_MIN];
        while (readPosition < inputLength)
        {
            long remain = inputLength - readPosition;
            int readSize = remain > COMPARE_SIZE_MIN? COMPARE_SIZE_MIN:(int)remain;

            sourceInput.read(buffer);
            int hashcode = Arrays.hashCode(Arrays.copyOfRange(buffer, 0, readSize));
            Object indexObj = blockNumMap.get(hashcode);
            int index = indexObj!=null? (int)indexObj :0;
            blockNumMap.put(hashcode, index+1);

            BlockMapKey value = new BlockMapKey(index, readSize, hashcode);
            blockMap.put(value, readPosition);

            readPosition += readSize;
        }
        return blockMap;
    }

    /**
     * 执行完全匹配，返回匹配的长度是多少
     * @param sourceInput
     * @param sourceStart
     * @param input
     * @return
     */
    private static int exactMatch(final RandomAccessFile sourceInput, int sourceStart, RandomFileInput input) throws IOException {
        int readSize = 0;
        int sameSize = 0;
        int offStart = 0;
        byte[] readbuffer = new byte[COMPARE_SIZE_MIN];

        long sourceLength = sourceInput.length();

        byte[] sourceBuffer = new byte[COMPARE_SIZE_MIN];

        for (int i = sourceStart; i < sourceLength; i ++)
        {
            if (readSize <= 0)
            {
                try {
                    readSize = input.read(readbuffer);
                    sourceInput.read(sourceBuffer);

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
            if (readbuffer[offStart] == sourceBuffer[offStart]) {
                offStart++;

                sameSize++;
                readSize--;
            }
            else
            {
                //回退到不相同的点
                if (readSize > 0) {
                    input.skip(-readSize);
                }

                return sameSize;
            }
        }
        return 0;
    }
}
