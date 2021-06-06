package com.melody.bean;

import java.util.Arrays;

/**
 * 原生字节块
 */
public class ByteBlockItem extends BlockItem{

    private byte[] bytes = null;

    private int BUFFER_SIZE = 4096;

    public ByteBlockItem() {
    }

    @Override
    public long getSize() {
        return this.size;
    }

    /**
     * 追加
     * @param appendByte
     */
    public void append(byte appendByte)
    {
        if (this.bytes == null)
        {
            this.bytes = new byte[BUFFER_SIZE];
        }

        if (size >= this.bytes.length)
        {
            byte[] newBytes = new byte[BUFFER_SIZE + bytes.length];
            System.arraycopy(this.bytes, 0, newBytes, 0, size);
            this.bytes = newBytes;
        }

        bytes[size] = appendByte;
        size++;
    }

    /**
     * 追加
     * @param appendBytes
     */
    public void append(byte[] appendBytes)
    {
        if (this.bytes == null)
        {
            this.bytes = new byte[BUFFER_SIZE];
        }

        if (this.bytes.length - size  < appendBytes.length)
        {
            byte[] newBytes = new byte[appendBytes.length + bytes.length + BUFFER_SIZE];
            System.arraycopy(this.bytes, 0, newBytes, 0, size);
            this.bytes = newBytes;
        }

        System.arraycopy(appendBytes, 0, this.bytes, size, appendBytes.length);

        size += appendBytes.length;
    }

    @Override
    public byte[] getOutput() {
        return Arrays.copyOfRange(this.bytes, 0, size);
    }

    @Override
    public void reset() {
        bytes = null;
        size = 0;
    }
}
