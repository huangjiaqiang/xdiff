package com.melody.io;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteInput
{
    private RandomAccessFile sourceRAF;
    private MappedByteBuffer mappedByteBuffer;

    long length = 0;

    public long length()
    {
        return length;
    }

    public MappedByteInput(String path) throws IOException
    {
        sourceRAF = new RandomFileInput(path);
        FileChannel fc = sourceRAF.getChannel();
        length = fc.size();
        mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, length);
    }

    public void read(byte[] bytes, int position)
    {
        mappedByteBuffer.position(position);
        mappedByteBuffer.get(bytes);
    }


    public int read(byte[] bytes)
    {
        if (mappedByteBuffer.remaining() < bytes.length)
        {
            mappedByteBuffer.get(bytes, 0, mappedByteBuffer.remaining());
            return mappedByteBuffer.remaining();
        }

        mappedByteBuffer.get(bytes);
        return bytes.length;
    }

    public long getPosition()
    {
        return mappedByteBuffer.position();
    }

    public void close()
    {
        try
        {
            sourceRAF.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void seek(int matchStart)
    {
        mappedByteBuffer.position(matchStart);
    }
}
