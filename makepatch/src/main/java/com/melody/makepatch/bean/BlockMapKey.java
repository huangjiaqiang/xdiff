package com.melody.makepatch.bean;

public class BlockMapKey implements Comparable<BlockMapKey> {
    int index;
    int length;

    public BlockMapKey(int index, int length, int hashcode) {
        this.index = index;
        this.length = length;
        this.hashcode = hashcode;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }

    int hashcode;

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockMapKey) {
            return hashcode == ((BlockMapKey) obj).hashcode
                    && this.index == ((BlockMapKey) obj).index
                    && this.length == ((BlockMapKey) obj).length;
        }
        return false;
    }

    @Override
    public int compareTo(BlockMapKey o) {
        if (index > o.index)
        {
            return  1;
        }else if (index < o.index)
        {
            return -1;
        }
        return 0;
    }
}
