package org.xson.web.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.xson.web.XcoWebException;

public class SerializeUtil {

	/**
	 * 序列化
	 */
	public static byte[] serialize(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			// oos.writ
			byte[] bytes = baos.toByteArray();
			baos.close();
			baos.close();
			return bytes;
		} catch (Exception e) {
			throw new XcoWebException(e);
		}
	}

	/**
	 * 反序列化
	 */
	public static Object unserialize(byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			throw new XcoWebException(e);
		}
	}
}
