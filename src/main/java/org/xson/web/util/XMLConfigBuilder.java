package org.xson.web.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xson.web.BuilderContext;
import org.xson.web.Container;

public class XMLConfigBuilder {

	private Logger			logger	= Logger.getLogger(XMLConfigBuilder.class);
	private XPathParser		parser	= null;
	private XmlNodeWrapper	root	= null;
	private BuilderContext	bc		= new BuilderContext();

	public XMLConfigBuilder(InputStream inputStream) {
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/controllers");
	}

	public void parseNode() {
		try {
			buildConfigNodes(this.root.evalNodes("config-property"));
			buildDomainNodes(this.root.evalNodes("domain"));
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
			builders[i].parseBeforeAfterNode();
			i++;
		}

		i = 0;
		for (String resource : resourceList) {
			logger.info("Start parsing: " + resource);
			builders[i].parseControllerNode();
			i++;
		}

		Container.getInstance().setControllerMap(this.bc.getControllerMap());
		this.bc.clear();
	}
}
