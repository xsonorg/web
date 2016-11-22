package org.xson.web.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xson.web.Container;
import org.xson.web.XcoWebException;
import org.xson.web.cache.vo.CacheVo;
import org.xson.web.cache.vo.CacheVo.CacheType;
import org.xson.web.util.StringUtils;

public class XMLConfigBuilder {

	private Logger			logger	= Logger.getLogger(XMLConfigBuilder.class);
	private XPathParser		parser	= null;
	private XmlNodeWrapper	root	= null;
	private BuilderContext	bc		= new BuilderContext();

	public XMLConfigBuilder(InputStream inputStream) {
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/web-config");
	}

	public void parseNode() {
		try {
			buildConfigNodes(this.root.evalNodes("config-property"));
			buildDomainNodes(this.root.evalNodes("domain"));
			buildCacheNodes(this.root.evalNodes("cache"));

			// 启动和设置默认cache
			setDefaultCache();
			startCache();

			buildPluginNodes(this.root.evalNodes("plugin"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new RuntimeException("<config-property> missing names or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			Container.getInstance().config(configMap);
		}
	}

	private void buildDomainNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// <domain id="xxx" base="http://www.baidu.com" />
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String base = StringUtils.trim(context.getStringAttribute("base"));
			if (null == id || null == base) {
				throw new RuntimeException("<domain> missing id or base");
			}
			if (this.bc.getDomainMap().containsKey(id)) {
				throw new RuntimeException("Duplicate domain: " + id);
			}
			this.bc.getDomainMap().put(id, base);
		}
	}

	private void buildCacheNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml
																			// validation
			if (this.bc.getCacheVoMap().containsKey(id)) {
				throw new XcoWebException("Duplicate cache:" + id);
			}
			String _type = StringUtils.trim(xNode.getStringAttribute("type"));
			CacheType type = null;
			if (null != _type) {
				type = getCacheType(_type);
			}
			if (null == type) {
				throw new XcoWebException("Unsupported cache type: " + _type);
			}

			String _defaultCache = StringUtils.trim(xNode.getStringAttribute("default"));
			boolean defaultCache = false;
			if (null != _defaultCache) {
				defaultCache = Boolean.parseBoolean(_defaultCache);
				if (defaultCache && null != this.bc.getDefaultCacheVo()) {
					throw new XcoWebException("The default cache already exists:" + id);
				}
			}

			// resource="tangyuan-mapper.xml"
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

			Map<String, String> propertiesMap = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			CacheVo cVo = new CacheVo(id, type, null, defaultCache, resource, propertiesMap);
			this.bc.getCacheVoMap().put(id, cVo);
			logger.info("add cache: " + id);

			if (defaultCache) {
				this.bc.setDefaultCacheVo(cVo);
			}
		}
	}

	private void setDefaultCache() {
		if (1 == this.bc.getCacheVoMap().size()) {
			for (Map.Entry<String, CacheVo> entry : this.bc.getCacheVoMap().entrySet()) {
				entry.getValue().setDefaultCache(true);
				this.bc.setDefaultCacheVo(entry.getValue());// 设置默认的cache
															// defaultCacheVo
				return;
			}
		}
	}

	/** 启动Cache */
	private void startCache() {
		for (Map.Entry<String, CacheVo> entry : this.bc.getCacheVoMap().entrySet()) {
			entry.getValue().start();
			logger.info("cache start: " + entry.getValue().getId());
		}
	}

	private CacheType getCacheType(String str) {
		if ("LOCAL".equalsIgnoreCase(str)) {
			return CacheType.LOCAL;
		} else if ("EHCACHE".equalsIgnoreCase(str)) {
			return CacheType.EHCACHE;
		} else if ("MEMCACHE".equalsIgnoreCase(str)) {
			return CacheType.MEMCACHE;
		} else if ("REDIS".equalsIgnoreCase(str)) {
			return CacheType.REDIS;
		} else {
			return null;
		}
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// <plugin resource="xxx.xml" />
		List<String> resourceList = new ArrayList<String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			if (null == resource) {
				throw new RuntimeException("<plugin> missing resource");
			}
			resourceList.add(resource);
		}

		int i = 0;
		XMLPluginBuilder[] builders = new XMLPluginBuilder[resourceList.size()];
		for (String resource : resourceList) {
			logger.info("Start parsing: " + resource);
			InputStream inputStream = Container.getInstance().getResourceAsStream(null, resource);
			builders[i] = new XMLPluginBuilder(inputStream, this.bc);
			builders[i].parseBeanNode();
			builders[i].parseInterceptNode();
			i++;
		}

		i = 0;
		for (String resource : resourceList) {
			logger.info("Start parsing: " + resource);
			builders[i].parseControllerNode();
			i++;
		}

		// 最后收尾
		Container.getInstance().setControllerMap(this.bc.getControllerMap());
		Container.getInstance().setCacheVoMap(this.bc.getCacheVoMap());
		this.bc.clear();
	}
}
