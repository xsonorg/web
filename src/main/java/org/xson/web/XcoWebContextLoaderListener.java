package org.xson.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class XcoWebContextLoaderListener implements ServletContextListener {

	private Logger	log	= Logger.getLogger(XcoWebContextLoaderListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			// load controller config
			String webFrameworkResource = context.getInitParameter("web-framework.resource");
			org.xson.web.Container.getInstance().init(webFrameworkResource);
			// load validate
			String dateValidateResource = context.getInitParameter("date-validate.resource");
			if (null != dateValidateResource) {
				org.xson.common.validate.Container.init(dateValidateResource);
				// fix bug
				Container.getInstance().setIntegratedValidationFramework(true);
			}
			System.out.println("web success!!!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch (Exception e) {
			// log.error("web framework failed to initialize", e);
			log.error("web framework failed to initialize");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			org.xson.web.Container.getInstance().stop();
			System.out.println("web close......");
		} catch (Throwable e) {
			log.error(e);
		}
	}

}
