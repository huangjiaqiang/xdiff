package com.melody.mergepatch;


import com.melody.io.RandomFileInput;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class MappedByteInput
{
    private RandomAccessFile sourceRAF;
    private MappedByteBuffer mappedByteBuffer;

    public MappedByteInput(String path) throws IOException
    {
        sourceRAF = new RandomFileInput(path);
        FileChannel fc = sourceRAF.getChannel();
        mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    }

    public void read(byte[] bytes, int position)
    {
        mappedByteBuffer.position(position);
        mappedByteBuffer.get(bytes);
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
}
