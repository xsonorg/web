package org.xson.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

	public static void main(String[] args) {
		String x = "{fdsfds324324}/@";
		// System.out.println(x.substring(1, x.length() - 1));
		String regex = "\\{.*\\}/@";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(x);
		System.out.println(matcher.matches());

		String y = "{xxxy}/axx/bxx";
		int pos = y.indexOf("}");
		System.out.println(y.substring(1, pos));
		System.out.println(y.substring(pos + 1));
	}

}
