package org.xson.web.util;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xson.web.BeforeAfterVo;
import org.xson.web.BuilderContext;
import org.xson.web.Container;
import org.xson.web.ControllerVo;
import org.xson.web.MethodObject;
import org.xson.web.RequestContext;

public class XMLPluginBuilder {

	private Logger			logger			= Logger.getLogger(XMLPluginBuilder.class);
	private XPathParser		parser			= null;
	private XmlNodeWrapper	root			= null;
	private BuilderContext	bc				= null;

	// 引用标志
	private String			refMark			= "@";
	private String			urlSeparator	= "/";

	public XMLPluginBuilder(InputStream inputStream, BuilderContext bc) {
		this.bc = bc;
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/controllers");
	}

	public void parseBeanNode() {
		try {
			buildBeanNode(this.root.evalNodes("bean"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void parseBeforeAfterNode() {
		try {
			buildBeforeAfterNode(this.root.evalNodes("before"), true);
			buildBeforeAfterNode(this.root.evalNodes("after"), false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void parseControllerNode() {
		try {
			buildControllerNode(this.root.evalNodes("c"));
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

	private void buildBeforeAfterNode(List<XmlNodeWrapper> contexts, boolean before) throws Exception {
		// <before call="a.b" order="0">
		for (XmlNodeWrapper context : contexts) {
			String call = StringUtils.trim(context.getStringAttribute("call"));
			String _order = StringUtils.trim(context.getStringAttribute("order"));

			if (before) {
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
				throw new RuntimeException("Before After missing <include|exclude>: " + call);
			}

			BeforeAfterVo baVo = new BeforeAfterVo(mo, order, includeList, excludeList);

			if (before) {
				this.bc.getBeforeList().add(baVo);
				this.bc.getBeforeMap().put(call, call);
			} else {
				this.bc.getAfterList().add(baVo);
				this.bc.getAfterMap().put(call, call);
			}
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

			List<BeforeAfterVo> beforeList = new ArrayList<BeforeAfterVo>();
			List<XmlNodeWrapper> beforeNodes = context.evalNodes("before");
			for (XmlNodeWrapper node : beforeNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				BeforeAfterVo baVo = new BeforeAfterVo(mo, order);
				beforeList.add(baVo);
			}

			List<BeforeAfterVo> afterList = new ArrayList<BeforeAfterVo>();
			List<XmlNodeWrapper> afterNodes = context.evalNodes("after");
			for (XmlNodeWrapper node : afterNodes) {
				String call = StringUtils.trim(node.getStringAttribute("call"));
				String _order = StringUtils.trim(node.getStringAttribute("order"));
				MethodObject mo = getMethodObject(call);
				int order = Container.getInstance().getOrder();
				if (null != _order) {
					order = Integer.parseInt(_order);
				}
				BeforeAfterVo baVo = new BeforeAfterVo(mo, order);
				afterList.add(baVo);
			}

			ControllerVo cVo = new ControllerVo(url, transfer, validate, execMethod, getBeforeAfterList(url, beforeList, true), getBeforeAfterList(
					url, afterList, false));

			this.bc.getControllerMap().put(cVo.getUrl(), cVo);
			logger.info("Add <c> :" + cVo.getUrl());
		}
	}

	private MethodObject getMethodObject(String str) throws Exception {
		MethodObject mo = null;
		// a.b
		if (str.startsWith(refMark)) {
			String[] array = str.split("\\.");
			if (array.length != 2) {
				throw new RuntimeException("Invalid bean reference: " + str);
			}
			String beanId = array[0].substring(1);
			String methodName = array[1];

			Object bean = this.bc.getBeanIdMap().get(beanId);
			if (null == bean) {
				throw new RuntimeException("Reference bean does not exist: " + beanId);
			}
			String moKey = bean.getClass().getName() + "." + methodName;
			mo = this.bc.getMoMap().get(moKey);
			if (null == mo) {
				Method method = bean.getClass().getMethod(methodName, RequestContext.class);
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
			method.setAccessible(true);
			mo = new MethodObject(method, instance);
			this.bc.getMoMap().put(str, mo);
		}
		return mo;
	}

	private String parseTransfer(String transfer, String url) {
		// @xxx/@0/@1
		// @xxx/xxxxxxxxxx/xxxx
		if (transfer.indexOf(refMark) > -1) {
			String[] urlArray = url.split(urlSeparator);
			String regex = "(@[a-zA-Z0-9-_]+)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(transfer);
			String newTransfer = transfer;
			while (matcher.find()) {
				String temp = matcher.group(1);
				newTransfer = newTransfer.replaceAll(temp, getReplaceString(temp, urlArray));
			}
			System.out.println(newTransfer);
			return newTransfer;
		} else {
			return transfer;
		}
	}

	private String getReplaceString(String group, String[] urlArray) {
		String ref = group.substring(1);
		if (ref.length() == 1 && isNumeric(ref.charAt(0))) {
			return urlArray[Integer.parseInt(ref)];
		}
		return this.bc.getDomainMap().get(ref);
	}

	private boolean isNumeric(int chr) {
		if (chr < 48 || chr > 57) {
			return false;
		}
		return true;
	}

	private String parseValidate(String validate, String url) {
		if ("@".equals(validate)) {
			return url;
		}
		if (validate.indexOf(refMark) > -1) {
			String[] urlArray = url.split(urlSeparator);
			String regex = "(@[a-zA-Z0-9-_]+)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(validate);
			String newValidate = validate;
			while (matcher.find()) {
				String temp = matcher.group(1);
				newValidate = newValidate.replaceAll(temp, urlArray[Integer.parseInt(temp.substring(1))]);
			}
			System.out.println(newValidate);
			return newValidate;
		} else {
			return validate;
		}
		// TODO 验证URL, 也可以以后验证
	}

	private List<MethodObject> getBeforeAfterList(String url, List<BeforeAfterVo> list, boolean before) {
		List<MethodObject> result = null;
		List<BeforeAfterVo> matchList = new ArrayList<BeforeAfterVo>();
		// 全局的
		List<BeforeAfterVo> globalList = null;
		if (before) {
			globalList = this.bc.getBeforeList();
		} else {
			globalList = this.bc.getAfterList();
		}
		for (BeforeAfterVo baVo : globalList) {
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
			for (BeforeAfterVo baVo : matchList) {
				result.add(baVo.getMo());
			}
		}

		return result;
	}
}
