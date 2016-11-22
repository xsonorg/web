package org.xson.web.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.xson.common.object.XCO;
import org.xson.common.validate.URLParameterHandler;
import org.xson.web.RequestContext;
import org.xson.web.RequestContext.DataFormatEnum;

import com.alibaba.fastjson.JSON;

public class ServletUtils {

	public static String parseRequestURI(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int pos = uri.lastIndexOf(".");
		if (pos > -1) {
			return uri.substring(0, pos);
		}
		return uri;
	}

	public static boolean isAjax(HttpServletRequest request, DataFormatEnum dataFormat) {
		String requestType = request.getHeader("X-Requested-With");
		if (null != requestType && requestType.equalsIgnoreCase("XMLHttpRequest")) {
			return true;
		}
		if (DataFormatEnum.XCO == dataFormat) {
			return true;
		}
		if (DataFormatEnum.JSON == dataFormat) {
			return true;
		}
		return false;
	}

	public static DataFormatEnum parseDataFormat(String contextType) {
		if (null == contextType) {
			return DataFormatEnum.KV;
		}
		if (contextType.indexOf("xco") > -1) {
			return DataFormatEnum.XCO;
		}
		if (contextType.indexOf("json") > -1) {
			return DataFormatEnum.JSON;
		}
		if (contextType.indexOf("multipart/form-data") > -1) {
			return DataFormatEnum.FILE;
		}
		return DataFormatEnum.KV;
	}

	public static Object parseArgFromRequest(HttpServletRequest request, RequestContext context, String validateId) throws Exception {
		if (DataFormatEnum.XCO == context.getDataFormat()) {
			byte[] buffer = IOUtils.toByteArray(request.getInputStream());
			String xml = new String(buffer, "UTF-8");
			xml = java.net.URLDecoder.decode(xml, "UTF-8");
			// System.out.println(xml);
			return XCO.fromXML(xml);
		} else if (DataFormatEnum.JSON == context.getDataFormat()) {
			byte[] buffer = IOUtils.toByteArray(request.getInputStream());
			return JSON.parse(new String(buffer, "UTF-8"));
		} else if (DataFormatEnum.KV == context.getDataFormat()) {
			if (null != validateId) {
				return URLParameterHandler.parseXCOParameter(request, validateId);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void printHttpHeader(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			System.out.println(key + ":" + value);
		}
	}

}
