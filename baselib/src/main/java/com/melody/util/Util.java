

package com.melody.util;

import java.lang.reflect.Modifier;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * 工具类
 */
public class Util
{

	public static final int MAX_SAFE_ARRAY_SIZE = Integer.MAX_VALUE - 8;


	/** Returns the class formatted as a string. The format varies depending on the type. */
	static public String className (Class type) {
		if (type.isArray()) {
			Class elementClass = getElementClass(type);
			StringBuilder buffer = new StringBuilder(16);
			for (int i = 0, n = getDimensionCount(type); i < n; i++)
				buffer.append("[]");
			return className(elementClass) + buffer;
		}
		if (type.isPrimitive() || type == Object.class || type == Boolean.class || type == Byte.class || type == Character.class
			|| type == Short.class || type == Integer.class || type == Long.class || type == Float.class || type == Double.class
			|| type == String.class) {
			return type.getSimpleName();
		}
		return type.getName();
	}

	/** Returns the number of dimensions of an array. */
	static public int getDimensionCount (Class arrayClass) {
		int depth = 0;
		Class nextClass = arrayClass.getComponentType();
		while (nextClass != null) {
			depth++;
			nextClass = nextClass.getComponentType();
		}
		return depth;
	}

	/** Returns the base element type of an n-dimensional array class. */
	static public Class getElementClass (Class arrayClass) {
		Class elementClass = arrayClass;
		while (elementClass.getComponentType() != null)
			elementClass = elementClass.getComponentType();
		return elementClass;
	}

	/** Converts an "int" value between endian systems. */
	static public int swapInt (int i) {
		return ((i & 0xFF) << 24) | ((i & 0xFF00) << 8) | ((i & 0xFF0000) >> 8) | ((i >> 24) & 0xFF);
	}

	/** Converts a "long" value between endian systems. */
	static public long swapLong (long value) {
		return (((value >> 0) & 0xff) << 56) | (((value >> 8) & 0xff) << 48) | (((value >> 16) & 0xff) << 40)
			| (((value >> 24) & 0xff) << 32) | (((value >> 32) & 0xff) << 24) | (((value >> 40) & 0xff) << 16)
			| (((value >> 48) & 0xff) << 8) | (((value >> 56) & 0xff) << 0);
	}

	/**
	 * 判断是否为抽像类，若为数组类，则判断其元素类是否为抽像类
	 * @param type
	 * @return
	 */
	public static boolean isAbstractClass(Class type)
	{
		if (type.isArray())
		{
			type = type.getComponentType();
		}
		return Modifier.isAbstract(type.getModifiers());
	}


	//计算校验和
	public static long getChecksum(byte[] bytes, int off, int length) {

		long checksum = 0;

		int position = 0;
		while (position < length)
		{
			int remain = length - position;
			int readSize = Math.min(remain, Integer.BYTES);

			int checkItem = ByteUtils.bytesToInt(bytes, position+off, readSize);

			checksum += checkItem;

			position+=readSize;
		}
		return checksum;
	}

	//计算校验和
	public static long getCRC32Checksum(byte[] bytes, int off, int length) {
		Checksum crc32 = new CRC32();
		crc32.update(bytes, off, length);
		return crc32.getValue();
	}
}
