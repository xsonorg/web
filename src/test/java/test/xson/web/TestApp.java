package test.xson.web;

import org.junit.Test;

public class TestApp {

	@Test
	public void test01() {
		try {
			String webFrameworkResource = "config.xml";
			org.xson.web.Container.getInstance().init(webFrameworkResource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
	}
}
