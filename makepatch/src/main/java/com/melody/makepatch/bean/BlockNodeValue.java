package com.melody.makepatch.bean;

/**
 * byte 块
 */
public class BlockNodeValue implements Comparable<BlockNodeValue> {
    int position;
    int length;
    long crc32;

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public BlockNodeValue(int position, int length, long crc32) {
        this.position = position;
        this.length = length;
        this.crc32 = crc32;
    }


    @Override
    public int compareTo(BlockNodeValue o) {
        if (this.crc32 > o.crc32)
        {
            return 1;
        }else if (this.crc32 < o.crc32)
        {
            return -1;
        }
        return 0;
    }
}
