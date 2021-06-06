package com.melody.util;

import java.nio.ByteBuffer;

public class ByteUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    private static ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }


    public static int bytesToInt(byte[] bytes, int offset, int length) {
        intBuffer.put(bytes, offset, length);
        intBuffer.flip();//need flip
        int ret = intBuffer.getInt();
        intBuffer.clear();
        return ret;
    }
}