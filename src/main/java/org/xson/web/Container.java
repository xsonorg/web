package org.xson.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xson.web.async.AsyncTask;
import org.xson.web.async.AsyncTaskThread;
import org.xson.web.cache.vo.CacheVo;
import org.xson.web.xml.ControllerVo;
import org.xson.web.xml.XMLConfigBuilder;

public class Container {

	private Logger logger = Logger.getLogger(Container.class);

	private Container() {
	}

	private static Container instance = new Container();

	public static Container getInstance() {
		return instance;
	}

	public ThreadLocal<RequestContext>	requestContextThreadLocal		= new ThreadLocal<RequestContext>();
	protected Map<String, ControllerVo>	controllerMap					= null;

	private int							errorCode						= -1;
	private String						errorMessage					= "系统错误";
	private int							errorCodeDataConversion			= -2;
	private String						errorMessageDataConversion		= "数据转换错误";
	private int							errorCodeDataValidate			= -3;
	private String						errorMessageDataValidate		= "数据验证错误";
	private String						errorRedirectPage				= "/404.html";
	private int							order							= 10;

	/** 远程服务模式|本地服务模式 */
	// private boolean remoteServiceMode = true;
	private boolean						localServiceMode				= false;

	/** URL默认映射模式 */
	private boolean						urlAutoMappingMode				= false;

	/** 是否集成TangYuan框架 */
	private boolean						integratedTangYuanFramework		= false;

	/** 是否集成验证框架 */
	private boolean						integratedValidationFramework	= false;

	/** 异步线程 */
	private AsyncTaskThread				asyncTaskThread					= null;

	/** 缓存容器集合 */
	private Map<String, CacheVo>		cacheVoMap						= null;

	private boolean						initialization					= false;

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
		if (!initialization) {
			logger.info("Start parsing: " + resource);
			InputStream inputStream = getResourceAsStream(null, resource);
			XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
			builder.parseNode();

			asyncTaskThread = new AsyncTaskThread();
			asyncTaskThread.start();

			initialization = true;
			logger.info("web framework init success...");
		}
	}

	public void stop() throws Throwable {
		if (initialization) {
			asyncTaskThread.stop();

			// if (null != cacheVoMap) {}
			for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
				entry.getValue().getCache().stop();
				logger.info("cache close: " + entry.getValue().getId());
			}

			logger.info("web framework stop...");
		}
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

		// if (properties.containsKey("remoteServiceMode".toUpperCase())) {
		// this.remoteServiceMode = Boolean.parseBoolean(properties.get("remoteServiceMode".toUpperCase()));
		// }

		if (properties.containsKey("localServiceMode".toUpperCase())) {
			this.localServiceMode = Boolean.parseBoolean(properties.get("localServiceMode".toUpperCase()));
		}

		if (properties.containsKey("urlAutoMappingMode".toUpperCase())) {
			this.urlAutoMappingMode = Boolean.parseBoolean(properties.get("urlAutoMappingMode".toUpperCase()));
		}

		if (localServiceMode && !integratedTangYuanFramework) {
			throw new XcoWebException("The local service mode requires tangyuan framework support");
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

	public boolean isRemoteServiceMode() {
		return !localServiceMode;
	}

	// public boolean isUrlDefaultMappingMode() {
	// return urlDefaultMappingMode;
	// }

	/** 是否映射服务名 */
	public boolean isMappingServiceName() {
		if (urlAutoMappingMode && localServiceMode) {
			return true;
		}
		return false;
	}

	public boolean isIntegratedValidationFramework() {
		return integratedValidationFramework;
	}

	public void setIntegratedValidationFramework(boolean integratedValidationFramework) {
		this.integratedValidationFramework = integratedValidationFramework;
	}

	public void setIntegratedTangYuanFramework(boolean integratedTangYuanFramework) {
		this.integratedTangYuanFramework = integratedTangYuanFramework;
	}

	public void addAsyncTask(AsyncTask task) {
		asyncTaskThread.addTask(task);
	}

	public Map<String, CacheVo> getCacheVoMap() {
		return cacheVoMap;
	}

	public void setCacheVoMap(Map<String, CacheVo> cacheVoMap) {
		if (null == this.cacheVoMap) {
			this.cacheVoMap = cacheVoMap;
		}
	}

}
