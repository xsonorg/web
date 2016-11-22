package org.xson.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xson.web.util.ServletUtils;
import org.xson.web.xml.ControllerVo;

public abstract class AbstractPermissionFilter implements Filter {

	private static Logger	log	= Logger.getLogger(AbstractPermissionFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String urlPath = ServletUtils.parseRequestURI((HttpServletRequest) request);
		ControllerVo cVo = Container.getInstance().getControllerVo(urlPath);
		if (null == cVo) {
			log.error("Mismatched URL path: " + urlPath);
			return;
		}
		RequestContext requestContext = new RequestContext((HttpServletRequest) request, (HttpServletResponse) response);
		if (permissionCheck(cVo.getPermission(), requestContext)) {
			Container.getInstance().requestContextThreadLocal.set(requestContext);
			chain.doFilter(request, response);
		} else {
			authFailed(requestContext);
		}
	}

	/**
	 * 权限检测
	 * 
	 * @param permission
	 * @param requestContext
	 * @return
	 */
	abstract public boolean permissionCheck(String permission, RequestContext requestContext);

	/**
	 * 认证失败
	 */
	abstract public void authFailed(RequestContext requestContext);

}
