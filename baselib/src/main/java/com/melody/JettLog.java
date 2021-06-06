package com.melody;




/**
 * Project Name: Jett
 * File Name:    MyLog.java
 * ClassName:    MyLog
 *
 * Description: 打日志.
 *
 * @author Jiaqiang Huang
 * @date 2018年09月14日 下午5:08
 *
 */
public class JettLog
{
    public static boolean TRACE = true;


    public static void d(Class target, String msg, Object... args)
    {
        log(1, target, msg, args);
    }
    
    
    public static void e(Class target, String msg, Object... args)
    {
        log(2, target, msg, args);
    }

    public static void log(int level, Class target, String msg, Object... args)
    {
        if (!TRACE)
        {
            return;
        }
        
        if (target == null)
        {
            return;
        }

        log(level, target.getSimpleName(), msg, args);
    }

    public static void d(Object target, String msg, Object... args)
    {
        log(1, target, msg, args);
    }

    public static void e(Object target, String msg, Object... args)
    {
        log(2, target, msg, args);
    }

    static void log(int level, Object target, String msg, Object... args)
    {
        if (!TRACE)
        {
            return;
        }
        
        if (target == null)
        {
            return;
        }

        log(level, target.getClass().getSimpleName(), msg, args);
    }

    public static void d(String tag, String msg, Object... args)
    { 
        log(1, tag, msg, args);
    }

    public static void e(String tag, String msg, Object... args)
    {
        log(2, tag, msg, args);
    }

    static void log(int level, String tag, String msg, Object... args)
    {
        if (!TRACE)
        {
            return;
        }
        
        if (TextUtils.isEmpty(tag))
        {
            return;
        }

        if (TextUtils.isEmpty(msg))
        {
            return;
        }

        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        switch (level)
        {
            case 1:
                System.out.println(tag+" "+msg);
                break;
            case 2:
                System.out.println(tag+" "+msg);
                break;
        }
    }

    public static void dFunc(Object target, String msg, Object... args)
    {
        if (!TRACE)
        {
            return;
        }

        String tag = getFuncName(target);

        if (TextUtils.isEmpty(tag))
        {
            return;
        }

        if (TextUtils.isEmpty(msg))
        {
            return;
        }

        if (args.length > 0) {
            msg = String.format(msg, args);
        }

        System.out.println(tag+" "+msg);
    }

    /**
     * 获取当前代码执行堆栈的方法名
     * @param thisObject
     * @return
     */
    public static String getFuncName(Object thisObject)
    {
        StackTraceElement element = Thread.currentThread().getStackTrace()[3+1];
        StringBuilder result = new StringBuilder();
        if (thisObject != null)
        {
            String objectClassName = thisObject.getClass().getSimpleName();
            if (!element.getFileName().contains(objectClassName))
            {
                result.append(objectClassName).append(thisObject.hashCode()).append(" ");
            }
        }

        result.append(element.getFileName().replace(".java", ""))
              .append(".")
              .append(element.getMethodName());
        result.append("(" + element.getLineNumber() + ") ");
        return result.toString();
    }

    /**
     * 打印当初调用方法堆栈
     */
    public static void dumpFuncStackTrace(String tag)
    {
        if (!TRACE)
        {
            return;
        }

        String msg = getFuncStack();
        d(tag, msg);
    }

    /**
     * 打印当初调用方法堆栈
     */
    public static String getFuncStack()
    {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuilder result = new StringBuilder();
        int count = Math.min(8, elements.length);
        for (int i=3; i<count; i++)
        {
            StackTraceElement item = elements[i];
            result.append(item.getFileName().replace(".java", ""))
                  .append(".")
                  .append(item.getMethodName());
            result.append("(" + item.getLineNumber() + ") \n");
        }
        return result.toString();
    }

}
