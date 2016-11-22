package test.xson.web;

import org.xson.web.RequestContext;

public class MyHandler {

	public void assembly0(RequestContext requestContext) {
		System.out.println("assembly0");
	}

	public void assembly1(RequestContext requestContext) {
		System.out.println("assembly1");
	}

	public void before0(RequestContext requestContext) {
		System.out.println("before0");
	}

	public void before1(RequestContext requestContext) {
		System.out.println("before1");
	}

	public void after0(RequestContext requestContext) {
		System.out.println("after0");
	}

	public void after1(RequestContext requestContext) {
		System.out.println("after1");
	}

	public void exec1(RequestContext requestContext) {
		System.out.println("exec1");
	}

	public void exec2(RequestContext requestContext) {
		System.out.println("exec2");
	}
}
