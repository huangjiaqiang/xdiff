package com.melody.util;

import com.melody.io.Output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**

 */
public class DiagnoseTool
{


    final static String BLOCK_INFO_PATH = "../assets/blockInfo";
    Output output = null;

    private DiagnoseTool()
    {}

    long totalSize = 0;

    public static DiagnoseTool obtain(String name){
        DiagnoseTool tool = new DiagnoseTool();
        try
        {
            File file = new File(BLOCK_INFO_PATH+name);
            file.delete();
            tool.output = new Output(new FileOutputStream(file));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return tool;
    }

    public void writeBlock(long blockSize)
    {
        output.writeLong(blockSize, true);
        totalSize += blockSize;
    }

    public void flush()
    {
        System.out.println("target file size:"+totalSize);
        output.flush();
    }


}
