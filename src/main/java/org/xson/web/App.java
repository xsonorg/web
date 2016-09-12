package org.xson.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

	public static void main(String[] args) {
		try {
			String[] resources = { "config.xml" };
			Container.getInstance().init(resources[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private String parseTransfer1(String transfer, String url) {
//		// @xxx/@0/@1
//		// @xxx/xxxxxxxxxx/xxxx
//		if (transfer.indexOf(refMark) > -1) {
//			String[] transferArray = transfer.split(urlSeparator);
//			String[] urlArray = url.split(urlSeparator);
//			if (transferArray[0].startsWith(refMark)) {
//				transferArray[0] = this.bc.getDomainMap().get(transferArray[0].substring(1));
//				if (null == transferArray[0]) {
//					throw new RuntimeException("Non-existent domain references: " + transferArray[0]);
//				}
//			}
//			if (transferArray.length > 1) {
//				for (int i = 1; i < transferArray.length; i++) {
//					if (transferArray[i].startsWith(refMark)) {
//						transferArray[i] = urlArray[Integer.parseInt(transferArray[i].substring(1))];
//					}
//				}
//			}
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < transferArray.length; i++) {
//				sb.append(transferArray[i]);
//				if (i > 0) {
//					sb.append(urlSeparator);
//				}
//			}
//			return sb.toString();
//		} else {
//			return transfer;
//		}
//	}

	public static void main1(String[] args) {
		// @xxx/@0/@1
		// @xxx/xxxxxxxxxx/xxxx
		String s = "@xxx/@0-2/@1/@_ddd";
		String regex = "(@[a-zA-Z0-9-_]+)";
		// String regex ="http://.*?>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		String ss = s;
		while (matcher.find()) {
			System.out.println(matcher.group() + ":" + matcher.group(1));
			ss = ss.replaceAll(matcher.group(1), "---------");
			System.out.println(ss);
		}
		System.out.println("ooooooooooooooo");
		System.out.println(matcher.groupCount());
	}
}
