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
    
    private long mLength = 0;

    final static public int BUFFER_SIZE = 4096;

    //两个块之间的缓冲
    final static public int LINK_BUFFER_SIZE = 100;


    public long getPosition() {
        return position;
    }

    public RandomFileInput(String name) throws IOException
    {
        super(name, "r");
        mLength = getChannel().size();
    }

    public RandomFileInput(File file, String mode) throws IOException
    {
        super(file, "r");
        mLength = getChannel().size();
    }

    private RandomFileInput(String name, String mode) throws IOException
    {
        super(name, mode);
        mLength = getChannel().size();
    }


    @Override
    public int read() throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new RuntimeException("not support");
    }


    static class Buffer{
        int length = 0;
        byte[] bytes = new byte[BUFFER_SIZE];
        int position = 0;
        //可用字节数
        int available()
        {
            return length - position;
        }

        /**
         *
         * @param offset
         * @return　　real offset
         */
        int skip(int offset)
        {
            int realSkip = 0;
            int s = position + offset;

            if (s > length)
            {
                //向前超出了
                realSkip = available();
                position = length;
            }
            else if (s >= 0 )
            {
                //合理范围
                position = s;
                realSkip = offset;
            }
            else
            {
                //向后退超出了
                realSkip = - position;
                position = 0;
            }
            return realSkip;
        }

        void reset()
        {
            length = 0;
            position = 0;
        }

        int read(byte[] bytes)
        {
            return read(bytes, 0, bytes.length);
        }

        int read(byte[] bytes, int offset , int length)
        {
            int readSize = 0;
            int available = available();
            if (available >= length)
            {
                readSize =  length;
            }
            else if (available > 0)
            {
                readSize =  available;
            }

            if (readSize > 0)
            {
                System.arraycopy(this.bytes, position, bytes, offset, readSize);
                position += readSize;
            }

            return readSize;
        }
    }

    Buffer preBuffer = new Buffer();
    Buffer buffer = new Buffer();


    public long available()
    {
        return mLength - position;
    }

    public int read(byte[] b) throws IOException {

        int readSize = 0;

        while (readSize < b.length)
        {
            if (preBuffer.available() > 0)
            {
                readSize = preBuffer.read(b, readSize, b.length - readSize);
            }

            if (readSize < b.length)
            {
                if (buffer.available() > 0)
                {
                    readSize += buffer.read(b, readSize, b.length - readSize);
                }
            }

            position += readSize;

            if (readSize == b.length)
            {
                return readSize;
            }

            if (readSize < b.length)
            {
                if (available() > 0)
                {
                    //缓存已经读完，需要读取文件
                    Buffer temp = buffer;
                    buffer = preBuffer;
                    preBuffer = temp;

                    buffer.reset();
                    int size = super.read(buffer.bytes);
                    if (size < 0)
                    {
                        throw new RuntimeException("never happen");
                    }
                    buffer.length = size;
                }
                else
                {
                    //文件读取结束
                    if (readSize > 0)
                    {
                        return readSize;
                    }else
                    {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }
    
    

    /**
     * 支持有限的向前跳及向后跳
     * @param offset
     * @return
     */
    public void skip(int offset)
    {
        int needSkip = offset;

        if (preBuffer.available() > 0)
        {
            offset -= preBuffer.skip(offset);
            if (offset < 0)
            {
                //向后skip
                throw new RuntimeException("prebuffer not support skip:"+ offset);
            }

            if (offset > 0)
            {
                //向前skip
                offset -=  buffer.skip(offset);
            }
        }
        else 
        {
            offset -= buffer.skip(offset);

            if (offset > 0)
            {
                //向前skip
                throw new RuntimeException("forword not support skip:"+ offset);
            }

            if (offset < 0)
            {
                //向后skip
                offset -=  preBuffer.skip(offset);

                if (offset < 0)
                {
                    //向后skip
                    throw new RuntimeException("prebuffer not support skip:"+ offset);
                }
            }
        }
        
        this.position += needSkip - offset;

        if (offset != 0)
        {
            throw new RuntimeException(" current position:"+ position + "offset:"+offset);
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public void write(int b) throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public void write(byte[] b) throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public long getFilePointer() throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public void seek(long pos) throws IOException {
        throw new RuntimeException("not support");
    }

    @Override
    public long length() throws IOException {
        return super.length();
    }

    @Override
    public void setLength(long newLength) throws IOException {
        throw new RuntimeException("not support");
    }
}
