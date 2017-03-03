package org.xson.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.xson.tangyuan.TangYuanContainer;

public class XcoWebContextLoaderListener implements ServletContextListener {

	private Logger	log			= Logger.getLogger(XcoWebContextLoaderListener.class);

	private boolean	tangyuan	= false;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			// load tangyuan
			String tangyuanResource = context.getInitParameter("tangyuan.resource");
			if (null != tangyuanResource) {
				TangYuanContainer.getInstance().start(tangyuanResource);
				tangyuan = true;
				Container.getInstance().setIntegratedTangYuanFramework(true);
				log.info("tangyuan init success!!!");
			}

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

			log.info("web success!!!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch (Throwable e) {
			log.error("web framework failed to initialize");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (tangyuan) {
			try {
				TangYuanContainer.getInstance().stop();
				log.info("tangyuan close ...");
			} catch (Throwable e) {
				log.error("tangyuan close error.", e);
			}
		}
		try {
			org.xson.web.Container.getInstance().stop();
			log.info("web close......");
		} catch (Throwable e) {
			log.error(e);
		}
	}

}
