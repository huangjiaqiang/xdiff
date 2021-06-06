package com.melody.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 可随机读取文件
 */
public class RandomFileInput extends RandomAccessFile {

    private long position = 0;

    public long getPosition() {
        return position;
    }

    public RandomFileInput(String name) throws FileNotFoundException {
        super(name, "r");
    }

    public RandomFileInput(File file, String mode) throws FileNotFoundException {
        super(file, "r");
    }

    private RandomFileInput(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }


    @Override
    public int read() throws IOException {
        int ret = super.read();
        if (ret >= 0)
        {
            position++;
        }
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = super.read(b, off, len);
        if (ret >= 0)
        {
            position += off + ret;
        }
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = super.read(b);
        if (ret >= 0)
        {
            position += ret;
        }
        return ret;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int ret = super.skipBytes(n);
        if (ret >= 0)
        {
            position += ret;
        }
        return ret;
    }

    @Override
    public void write(int b) throws IOException {
        throw new RuntimeException("not suppurt");
    }

    @Override
    public void write(byte[] b) throws IOException {
        throw new RuntimeException("not suppurt");
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        throw new RuntimeException("not suppurt");
    }

    @Override
    public long getFilePointer() throws IOException {
        throw new RuntimeException("not suppurt");
    }

    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        position = pos;
    }

    @Override
    public long length() throws IOException {
        return super.length();
    }

    @Override
    public void setLength(long newLength) throws IOException {
        throw new RuntimeException("not suppurt");
    }
}
