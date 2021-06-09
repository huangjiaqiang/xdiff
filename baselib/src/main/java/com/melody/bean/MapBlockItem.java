package com.melody.bean;

import com.melody.io.Output;

import java.util.Arrays;

/**
 * 映射块
 */
public class MapBlockItem extends BlockItem{
    //映射开始位置
    private int start = 0;

    private int size = 0;

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public byte[] getBytes() {
        throw new RuntimeException("not need");
    }

    @Override
    public void reset() {
        start = 0;
        size = 0;
    }
}
