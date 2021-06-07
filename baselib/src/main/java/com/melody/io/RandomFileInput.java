package com.melody.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 可随机读取文件
 */
public class RandomFileInput extends RandomAccessFile {

    private long position = 0;

    final static public int BUFFER_SIZE = 4096;

    //两个块之间的缓冲
    final static public int LINK_BUFFER_SIZE = 100;

    private byte[] buffer = new byte[BUFFER_SIZE];

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

    int bufferPosition = 0;
    byte[] preBufferBytes = null;



    public int read1(byte[] b) throws IOException {

        if (b.length > BUFFER_SIZE)
        {
            throw new RuntimeException("not support");
        }

        //剩余字节数
        int remain = buffer.length - bufferPosition;
        if (remain < b.length)
        {
            int back = LINK_BUFFER_SIZE - remain;
            preBufferBytes = Arrays.copyOfRange(buffer, bufferPosition-back, LINK_BUFFER_SIZE);
            super.read(buffer);
            bufferPosition -= BUFFER_SIZE;
        }

        if (bufferPosition<0)
        {
            int preLenth = Math.abs(bufferPosition);
            bufferPosition = b.length - preLenth;
            System.arraycopy(preBufferBytes, LINK_BUFFER_SIZE+preLenth, b, 0, preLenth);
            System.arraycopy(buffer, 0, b, preLenth, bufferPosition);
        }else
        {
            System.arraycopy(buffer, bufferPosition, b, 0, b.length);
            bufferPosition += b.length;
        }

        int ret = super.read(b);
        if (ret >= 0)
        {
            position += ret;
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
