package org.xson.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xson.web.util.XMLConfigBuilder;

public class Container {

	private Logger	logger	= Logger.getLogger(Container.class);

	private Container() {
	}

	private static Container	instance	= new Container();

	public static Container getInstance() {
		return instance;
	}

	public ThreadLocal<RequestContext>	requestContextThreadLocal	= new ThreadLocal<RequestContext>();
	protected Map<String, ControllerVo>	controllerMap;

	private int							errorCode					= -1;
	private String						errorMessage				= "系统错误";
	private int							errorCodeDataConversion		= -2;
	private String						errorMessageDataConversion	= "数据转换错误";
	private int							errorCodeDataValidate		= -3;
	private String						errorMessageDataValidate	= "数据验证错误";
	private String						errorRedirectPage			= "/404.html";
	private int							order						= 10;

	public void setControllerMap(Map<String, ControllerVo> controllerMap) {
		if (null == this.controllerMap) {
			this.controllerMap = controllerMap;
		}
	}

	protected ControllerVo getControllerVo(String url) {
		return controllerMap.get(url);
	}

	public InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
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

	public void init(String resource) throws Exception {
		logger.info("Start parsing: " + resource);
		InputStream inputStream = getResourceAsStream(null, resource);
		XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
		builder.parseNode();
		// initialization = true;
		logger.info("web framework init success...");
	}

	public void config(Map<String, String> properties) {

		if (properties.containsKey("errorCode".toUpperCase())) {
			this.errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}

		if (properties.containsKey("errorCodeDataConversion".toUpperCase())) {
			this.errorCodeDataConversion = Integer.parseInt(properties.get("errorCodeDataConversion".toUpperCase()));
		}

		if (properties.containsKey("errorCodeDataConversion".toUpperCase())) {
			this.errorCodeDataConversion = Integer.parseInt(properties.get("errorCodeDataConversion".toUpperCase()));
		}

		if (properties.containsKey("order".toUpperCase())) {
			this.order = Integer.parseInt(properties.get("order".toUpperCase()));
		}

		if (properties.containsKey("errorMessage".toUpperCase())) {
			this.errorMessage = properties.get("errorMessage".toUpperCase());
		}

		if (properties.containsKey("errorMessageDataConversion".toUpperCase())) {
			this.errorMessageDataConversion = properties.get("errorMessageDataConversion".toUpperCase());
		}

		if (properties.containsKey("errorMessageDataValidate".toUpperCase())) {
			this.errorMessageDataValidate = properties.get("errorMessageDataValidate".toUpperCase());
		}

		if (properties.containsKey("errorRedirectPage".toUpperCase())) {
			this.errorRedirectPage = properties.get("errorRedirectPage".toUpperCase());
		}

		logger.info("config setting success, version: " + Version.getVersion());
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getErrorCodeDataConversion() {
		return errorCodeDataConversion;
	}

	public String getErrorMessageDataConversion() {
		return errorMessageDataConversion;
	}

	public int getErrorCodeDataValidate() {
		return errorCodeDataValidate;
	}

	public String getErrorMessageDataValidate() {
		return errorMessageDataValidate;
	}

	public String getErrorRedirectPage() {
		return errorRedirectPage;
	}

	public int getOrder() {
		return order;
	}
}
