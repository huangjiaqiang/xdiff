package com.melody.util;

/**
 * 用于计算代码执行时长工具
 */
public class ExecTimeUtil {

    public interface CodeExecution<I,O>{
        O exec(I i);
    }

    public interface CodeExecutionOut<O>{
        O exec();
    }

    public interface CodeExecutionNoReturn {
        void exec();
    }


    public static<I,O> O exec1(CodeExecution<I,O> codeBlock, I i)
    {
        // 开始时间
        long stime = System.currentTimeMillis();
        O ret = codeBlock.exec(i);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("consume time: %d.\n", (etime - stime));
        return ret;
    }

    public static<O> O exec2(CodeExecutionOut<O> codeBlock)
    {
        // 开始时间
        long stime = System.nanoTime();
        O ret = codeBlock.exec();
        // 结束时间
        long etime = System.nanoTime();
        // 计算执行时间
        System.out.printf("consume time: %d.\n", (etime - stime));
        return ret;
    }

    public static  void exec(CodeExecutionNoReturn codeBlock)
    {
        // 开始时间
        long stime = System.currentTimeMillis();
        codeBlock.exec();
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("consume time: %d.\n", (etime - stime));
    }

}
