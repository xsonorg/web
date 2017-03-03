package org.xson.web.xml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.web.Container;
import org.xson.web.RequestContext;
import org.xson.web.XcoWebException;
import org.xson.web.cache.vo.CacheUseVo;
import org.xson.web.cache.vo.CacheVo;
import org.xson.web.util.StringUtils;
import org.xson.web.xml.InterceptVo.InterceptType;

public class XMLPluginBuilder {

	private Logger			logger			= Logger.getLogger(XMLPluginBuilder.class);
	private XPathParser		parser			= null;
	private XmlNodeWrapper	root			= null;
	private BuilderContext	bc				= null;

	// 引用标志
	private String			refMark			= "@";
	private String			urlSeparator	= "/";
	private String			leftBrackets	= "{";
	private String			rightBrackets	= "}";

	public XMLPluginBuilder(BuilderContext bc) {
		this.bc = bc;
	}

	public XMLPluginBuilder(InputStream inputStream, BuilderContext bc) {
		this.bc = bc;
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/web-controller");
	}

	public void parseBeanNode() {
		try {
			buildBeanNode(this.root.evalNodes("bean"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void parseInterceptNode() {
		try {
			buildInterceptNode(this.root.evalNodes("assembly"), InterceptType.ASSEMBLY);
			buildInterceptNode(this.root.evalNodes("before"), InterceptType.BEFORE);
			buildInterceptNode(this.root.evalNodes("after"), InterceptType.AFTER);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void parseControllerNode() {
		try {
			if (Container.getInstance().isRemoteServiceMode()) {
				buildControllerNode(this.root.evalNodes("c"));
			} else {
				buildControllerNodeWithLocalMode(this.root.evalNodes("c"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void parseControllerNodeAutoMapping() {
		try {
			buildControllerNodeAutoMapping();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void buildBeanNode(List<XmlNodeWrapper> contexts) throws Exception {
		// <bean id="" class="" />
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String clazz = StringUtils.trim(context.getStringAttribute("class"));
			if (this.bc.getBeanIdMap().containsKey(id)) {
				throw new RuntimeException("Duplicate Bean: " + id);
			}
			Object obj = this.bc.getBeanClassMap().get(clazz);
			if (null == obj) {
				obj = Class.forName(clazz).newInstance();
				this.bc.getBeanClassMap().put(clazz, obj);
			}
			this.bc.getBeanIdMap().put(id, obj);
			logger.info("Add <bean> :" + clazz);
		}
	}

	private void buildInterceptNode(List<XmlNodeWrapper> contexts, InterceptType type) throws Exception {
		for (XmlNodeWrapper context : contexts) {

			String call = StringUtils.trim(context.getStringAttribute("call"));
			String _order = StringUtils.trim(context.getStringAttribute("order"));

			if (InterceptType.ASSEMBLY == type) {
				if (this.bc.getBeforeMap().containsKey(call)) {
					throw new RuntimeException("Duplicate Assembly: " + call);
				}
			} else if (InterceptType.BEFORE == type) {
				if (this.bc.getBeforeMap().containsKey(call)) {
					throw new RuntimeException("Duplicate Before: " + call);
				}
			} else {
				if (this.bc.getAfterMap().containsKey(call)) {
					throw new RuntimeException("Duplicate After: " + call);
				}
			}

			MethodObject mo = getMethodObject(call);

			int order = Container.getInstance().getOrder();
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			List<String> includeList = new ArrayList<String>();
			List<String> excludeList = new ArrayList<String>();

			// <include></include>
			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
			for (XmlNodeWrapper include : includeNodes) {
				String body = StringUtils.trim(include.getStringBody());
				if (null != body) {
					includeList.add(body);
				}
			}
			if (includeList.size() == 0) {
				includeList = null;
			}

			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trim(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}
			if (excludeList.size() == 0) {
				excludeList = null;
			}

			if (null == includeList && null == excludeList) {
				throw new RuntimeException("Intercept missing <include|exclude>: " + call);
			}

			InterceptVo baVo = new InterceptVo(mo, order, includeList, excludeList);

			if (InterceptType.ASSEMBLY == type) {
				this.bc.getAssemblyList().add(baVo);
				this.bc.getAssemblyMap().put(call, call);
			} else if (InterceptType.BEFORE == type) {
				this.bc.getBeforeList().add(baVo);
				this.bc.getBeforeMap().put(call, call);
			} else {
				this.bc.getAfterList().add(baVo);
				this.bc.getAfterMap().put(call, call);
			}

		}
	}

	/** 解析控制器,自动映射模式 */
	private void buildControllerNodeAutoMapping() throws Exception {
		Set<String> serviceKeys = TangYuanContainer.getInstance().getServicesKeySet();
		for (String key : serviceKeys) {
			String url = serviceNameToUrl(key);
			if (this.bc.getControllerMap().containsKey(url)) {
				throw new RuntimeException("Duplicate URL: " + url);
			}

			String transfer = key;
			String validate = null;
			MethodObject execMethod = null;
			String permission = null;
			CacheUseVo cacheUse = null;

			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission,
					cacheUse);

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			logger.info("Add auto <c> :" + cVo.getUrl());
		}
	}

	/** 解析控制器,本地服务模式 */
	private void buildControllerNodeWithLocalMode(List<XmlNodeWrapper> contexts) throws Exception {
		for (XmlNodeWrapper context : contexts) {
			String url = StringUtils.trim(context.getStringAttribute("url"));
			if (this.bc.getControllerMap().containsKey(url)) {
				throw new RuntimeException("Duplicate URL: " + url);
			}
			// TODO url check null

			String validate = StringUtils.trim(context.getStringAttribute("validate"));
			if (null != validate) {
				validate = parseValidate(validate, url);
			}

			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			MethodObject execMethod = null;
			if (null != exec) {
				execMethod = getMethodObject(exec);
			}

			String transfer = StringUtils.trim(context.getStringAttribute("transfer"));
			if (null == transfer && null == execMethod) {
				transfer = url;
			}

			transfer = urlToServiceName(transfer);

			// 权限
			String permission = StringUtils.trim(context.getStringAttribute("permission"));

			// 缓存
			String _cacheUse = StringUtils.trim(context.getStringAttribute("cache"));
			CacheUseVo cacheUse = null;
			if (null != _cacheUse && _cacheUse.length() > 0) {
				cacheUse = parseCacheUse(_cacheUse, url);
			}

			// 私有的assembly
			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> assemblyNodes = context.evalNodes("assembly");
			for (XmlNodeWrapper node : assemblyNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				assemblyList.add(baVo);
			}

			// 私有的before
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> beforeNodes = context.evalNodes("before");
			for (XmlNodeWrapper node : beforeNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				beforeList.add(baVo);
			}

			// 私有的after
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> afterNodes = context.evalNodes("after");
			for (XmlNodeWrapper node : afterNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				afterList.add(baVo);
			}

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission,
					cacheUse);

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			logger.info("Add <c> :" + cVo.getUrl());
		}
	}

	private void buildControllerNode(List<XmlNodeWrapper> contexts) throws Exception {
		for (XmlNodeWrapper context : contexts) {
			// 基于安全考虑, 不能支持*,
			String url = StringUtils.trim(context.getStringAttribute("url"));
			if (this.bc.getControllerMap().containsKey(url)) {
				throw new RuntimeException("Duplicate URL: " + url);
			}
			String transfer = StringUtils.trim(context.getStringAttribute("transfer"));
			if (null != transfer) {
				transfer = parseTransfer(transfer, url);
			}
			String validate = StringUtils.trim(context.getStringAttribute("validate"));
			if (null != validate) {
				validate = parseValidate(validate, url);
			}

			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			MethodObject execMethod = null;
			if (null != exec) {
				execMethod = getMethodObject(exec);
			}

			if (null == transfer && null == execMethod) {
				throw new RuntimeException("transfer and exec can not be empty, url: " + url);
			}

			// 权限
			String permission = StringUtils.trim(context.getStringAttribute("permission"));

			// 缓存
			String _cacheUse = StringUtils.trim(context.getStringAttribute("cache"));
			CacheUseVo cacheUse = null;
			if (null != _cacheUse && _cacheUse.length() > 0) {
				cacheUse = parseCacheUse(_cacheUse, url);
			}

			// 私有的assembly
			List<InterceptVo> assemblyList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> assemblyNodes = context.evalNodes("assembly");
			for (XmlNodeWrapper node : assemblyNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				assemblyList.add(baVo);
			}

			// 私有的before
			List<InterceptVo> beforeList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> beforeNodes = context.evalNodes("before");
			for (XmlNodeWrapper node : beforeNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				beforeList.add(baVo);
			}

			// 私有的after
			List<InterceptVo> afterList = new ArrayList<InterceptVo>();
			List<XmlNodeWrapper> afterNodes = context.evalNodes("after");
			for (XmlNodeWrapper node : afterNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				InterceptVo baVo = new InterceptVo(mo, order);
				afterList.add(baVo);
			}

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getInterceptList(url, assemblyList, InterceptType.ASSEMBLY),
					getInterceptList(url, beforeList, InterceptType.BEFORE), getInterceptList(url, afterList, InterceptType.AFTER), permission,
					cacheUse);

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			logger.info("Add <c> :" + cVo.getUrl());
		}
	}

	private MethodObject getMethodObject(String str) throws Exception {
		MethodObject mo = null;
		// a.b
		// if (str.startsWith(refMark)) {
		if (str.startsWith(leftBrackets)) {
			String[] array = str.split("\\.");
			if (array.length != 2) {
				throw new RuntimeException("Invalid bean reference: " + str);
			}
			// String beanId = array[0].substring(1);
			String beanId = array[0].substring(1, array[0].length() - 1);
			String methodName = array[1];

			Object bean = this.bc.getBeanIdMap().get(beanId);
			if (null == bean) {
				throw new RuntimeException("Reference bean does not exist: " + beanId);
			}
			String moKey = bean.getClass().getName() + "." + methodName;
			mo = this.bc.getMoMap().get(moKey);
			if (null == mo) {
				Method method = bean.getClass().getMethod(methodName, RequestContext.class);
				// 如果方法不存在或者参数类型不匹配，这里会抛出异常
				method.setAccessible(true);
				mo = new MethodObject(method, bean);
				this.bc.getMoMap().put(moKey, mo);
			}
		} else {
			mo = this.bc.getMoMap().get(str);
			if (null != mo) {
				return mo;
			}
			int pos = str.lastIndexOf(".");
			String className = str.substring(1, pos);
			String methodName = str.substring(pos + 1, str.length());

			Object instance = this.bc.getBeanClassMap().get(className);
			Class<?> clazz = null;
			if (null == instance) {
				clazz = Class.forName(className);
				instance = clazz.newInstance();
				this.bc.getBeanClassMap().put(className, instance);
			} else {
				clazz = instance.getClass();
			}
			Method method = clazz.getMethod(methodName, RequestContext.class);
			// 如果方法不存在或者参数类型不匹配，这里会抛出异常
			method.setAccessible(true);
			mo = new MethodObject(method, instance);
			this.bc.getMoMap().put(str, mo);
		}
		return mo;
	}

	/**
	 * URL合并
	 */
	private String urlMerge(String domain, String path) {
		if (domain.endsWith(urlSeparator) && path.startsWith(urlSeparator)) {
			return domain + path.substring(1);
		} else if (domain.endsWith(urlSeparator) || path.startsWith(urlSeparator)) {
			return domain + path;
		} else {
			return domain + urlSeparator + path;
		}
	}

	private String parseTransfer(String transfer, String url) {
		// 第一种情况: {xxx}/@
		String regex = "\\{.*\\}/@";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(transfer);
		if (matcher.matches()) {
			int pos = transfer.indexOf(rightBrackets);
			String domainRef = transfer.substring(1, pos);
			String domain = this.bc.getDomainMap().get(domainRef);
			if (null == domain) {
				// TODO: 完善
				throw new RuntimeException("域名引用错误: " + transfer);
			}
			return urlMerge(domain, url);
		}
		// 第二种情况: {xxx}/axx/bxx
		if (transfer.startsWith(leftBrackets)) {
			int pos = transfer.indexOf(rightBrackets);
			String domainRef = transfer.substring(1, pos);
			String domain = this.bc.getDomainMap().get(domainRef);
			if (null == domain) {
				// TODO: 完善
				throw new RuntimeException("域名引用错误: " + transfer);
			}
			String path = transfer.substring(pos + 1);
			return urlMerge(domain, path);
		}
		// 第三种情况: 完全手写
		return transfer;
	}

	private String parseValidate(String validate, String url) {
		if (refMark.equals(validate)) {
			String validateKey = url;
			if (validateKey.startsWith(urlSeparator)) {
				validateKey = validateKey.substring(1);
			}
			// if (validateKey.endsWith(urlSeparator)) {
			//
			// }
			return validateKey;
		}
		return validate;
	}

	private List<MethodObject> getInterceptList(String url, List<InterceptVo> list, InterceptType type) {
		List<MethodObject> result = null;
		List<InterceptVo> matchList = new ArrayList<InterceptVo>();
		// 全局的
		List<InterceptVo> globalList = null;

		if (InterceptType.ASSEMBLY == type) {
			globalList = this.bc.getAssemblyList();
		} else if (InterceptType.BEFORE == type) {
			globalList = this.bc.getBeforeList();
		} else {
			globalList = this.bc.getAfterList();
		}

		for (InterceptVo baVo : globalList) {
			if (baVo.match(url)) {
				matchList.add(baVo);
			}
		}
		if (list.size() > 0) {
			matchList.addAll(list);
		}

		if (matchList.size() > 0) {
			Collections.sort(matchList);// sort
			result = new ArrayList<MethodObject>();
			for (InterceptVo baVo : matchList) {
				result.add(baVo.getMo());
			}
		}

		return result;
	}

	/**
	 * 解析: ID:xxx; key:xxx; time:1000; ignore:a,b<br />
	 * 最短cache="@"
	 * 
	 */
	private CacheUseVo parseCacheUse(String cacheUse, String url) {
		CacheUseVo cacheUseVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			if (array.length == 1 && (-1 == array[0].indexOf("key:"))) {
				// 特殊情况
				CacheVo cacheVo = this.bc.getDefaultCacheVo();
				if (null == cacheVo) {
					throw new XcoWebException("未匹配的cache.id: " + cacheUse);
				}
				return new CacheUseVo(cacheVo, cacheUse, null, null, url);
			}

			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			CacheVo cacheVo = null;
			if (map.containsKey("id".toUpperCase())) {
				cacheVo = this.bc.getCacheVoMap().get(map.get("id".toUpperCase()));
			} else {
				cacheVo = this.bc.getDefaultCacheVo();
			}
			if (null == cacheVo) {
				throw new XcoWebException("未匹配的cache.id: " + cacheUse);
			}

			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XcoWebException("不存在的cache.key: " + cacheUse);
			}

			// TODO: 可用expire代替
			Integer time = null;
			if (map.containsKey("time".toUpperCase())) {
				time = Integer.parseInt(map.get("time".toUpperCase()));
			}
			String[] ignore = null;
			// if (map.containsKey("ignore".toUpperCase())) {
			// ignore = map.get("ignore".toUpperCase()).split(",");
			// }
			cacheUseVo = new CacheUseVo(cacheVo, key, time, ignore, url);
		}
		return cacheUseVo;
	}

	/** URL到本地服务名 */
	private String urlToServiceName(String url) {
		// <c url="/sos-project/getMyProjectP10" transfer="{service}/sos-project/getMyProjectP10" />
		if (null == url) {
			return null;
		}
		if (url.startsWith(urlSeparator)) {
			url = url.substring(1);
		}
		if (url.endsWith(urlSeparator)) {
			url = url.substring(0, url.length() - 1);
		}
		return url.replaceAll("/", ".");
	}

	private String serviceNameToUrl(String serviceName) {
		if (!serviceName.startsWith(urlSeparator)) {
			serviceName = urlSeparator + serviceName;
		}
		return serviceName.replaceAll("\\.", "/");
	}

}
