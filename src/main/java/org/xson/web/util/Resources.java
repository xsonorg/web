package org.xson.web.util;

import java.io.IOException;
import java.io.InputStream;

import org.xson.web.Container;

public class Resources {

	public static InputStream getResourceAsStream(String resource) throws IOException {
		return getResourceAsStream(Container.class.getClassLoader(), resource);
	}

	public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
		if (null == loader) {
			loader = Container.class.getClassLoader();
		}
		InputStream returnValue = loader.getResourceAsStream(resource);
		if (null == returnValue) {
			returnValue = loader.getResourceAsStream("/" + resource);
		}
		if (null != returnValue) {
			return returnValue;
		}
		return null;
	}
}
