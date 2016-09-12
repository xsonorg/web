package org.xson.web;

import java.lang.reflect.Method;

public class MethodObject {

	private Method	method;

	private Object	instance;

	public MethodObject(Method method, Object instance) {
		this.method = method;
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public Object getInstance() {
		return instance;
	}

}
