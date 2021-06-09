package com.melody.bean;

public abstract class BlockItem {

    public final static int BLOCK_TYPE_MAP = 1;
    public final static int BLOCK_TYPE_BYTE = 2;

    //大小
    int size = 0;

    //字节码内容
    private byte[] bytes;
    abstract public long getSize();
    abstract public byte[] getBytes();

    abstract public void reset();

}
