package org.xson.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析过程中的上下文
 */
public class BuilderContext {

	private Map<String, ControllerVo>	controllerMap	= new HashMap<String, ControllerVo>();

	private Map<String, String>			domainMap		= new HashMap<String, String>();
	private Map<String, Object>			beanIdMap		= new HashMap<String, Object>();
	private Map<String, Object>			beanClassMap	= new HashMap<String, Object>();

	private Map<String, MethodObject>	moMap			= new HashMap<String, MethodObject>();

	private List<BeforeAfterVo>			beforeList		= new ArrayList<BeforeAfterVo>();
	private List<BeforeAfterVo>			afterList		= new ArrayList<BeforeAfterVo>();

	private Map<String, String>			beforeMap		= new HashMap<String, String>();
	private Map<String, String>			afterMap		= new HashMap<String, String>();

	public Map<String, String> getDomainMap() {
		return domainMap;
	}

	public Map<String, Object> getBeanIdMap() {
		return beanIdMap;
	}

	public Map<String, Object> getBeanClassMap() {
		return beanClassMap;
	}

	public Map<String, ControllerVo> getControllerMap() {
		return controllerMap;
	}

	public List<BeforeAfterVo> getBeforeList() {
		return beforeList;
	}

	public List<BeforeAfterVo> getAfterList() {
		return afterList;
	}

	public Map<String, String> getBeforeMap() {
		return beforeMap;
	}

	public Map<String, String> getAfterMap() {
		return afterMap;
	}

	public Map<String, MethodObject> getMoMap() {
		return moMap;
	}

	public void clear() {
		this.domainMap.clear();
		this.beanIdMap.clear();
		this.beanClassMap.clear();
		this.moMap.clear();
		this.beforeList.clear();
		this.afterList.clear();
		this.beforeMap.clear();
		this.afterMap.clear();

		this.domainMap = null;
		this.beanIdMap = null;
		this.beanClassMap = null;
		this.moMap = null;
		this.beforeList = null;
		this.afterList = null;
		this.beforeMap = null;
		this.afterMap = null;

		this.controllerMap = null;
	}
}
