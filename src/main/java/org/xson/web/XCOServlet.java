package org.xson.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xson.common.object.XCO;
import org.xson.common.validate.URLParameterHandler;
import org.xson.common.validate.XCOValidate;
import org.xson.common.validate.XCOValidateException;
import org.xson.web.RequestContext.DataFormatEnum;
import org.xson.web.RequestContext.RequestTypeEnum;
import org.xson.web.handler.DefaultJSONResponseHandler;
import org.xson.web.handler.DefaultXCOResponseHandler;
import org.xson.web.util.ServletUtils;
import org.xson.web.xml.ControllerVo;

public class XCOServlet extends HttpServlet {

	private static final long	serialVersionUID	= 1L;

	private static Logger		log					= Logger.getLogger(XCOServlet.class);

	private ResponseHandler		xcoResponseHandler	= new DefaultXCOResponseHandler();
	private ResponseHandler		jsonResponseHandler	= new DefaultJSONResponseHandler();

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.GET);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handler(req, resp, RequestTypeEnum.POST);
	}

	private void handler(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {

		RequestContext context = pretreatmentContext(req, resp, requestType);

		Container container = Container.getInstance();

		ControllerVo cVo = container.getControllerVo(context.getUrl());

		if (null == cVo) {
			log.error("It does not match the URL: " + context.getUrl());
			context.setErrorInfo(container.getErrorCode(), container.getErrorMessage());
			doResponseError(context, null);
			return;
		}

		// data convert
		String validateId = cVo.getValidate();
		try {
			if (RequestTypeEnum.POST == requestType) {
				context.setArg(ServletUtils.parseArgFromRequest(req, context, validateId));
			} else {
				// GET
				if (null != validateId) {
					context.setArg(URLParameterHandler.parseXCOParameter(req, validateId));
				}
			}
		} catch (Exception e) {
			context.setErrorInfo(container.getErrorCodeDataConversion(), container.getErrorMessageDataConversion());
			doResponseError(context, e);
			return;
		}

		// assembly
		try {
			cVo.assembly(context);
		} catch (Throwable e) {
			context.setErrorInfo(container.getErrorCodeDataConversion(), container.getErrorMessageDataConversion());
			doResponseError(context, e);
			return;
		}

		// validate
		if (container.isIntegratedValidationFramework() && null != validateId) {
			try {
				boolean checkResult = XCOValidate.validate(validateId, (XCO) context.getArg());
				if (!checkResult) {
					context.setErrorInfo(container.getErrorCodeDataValidate(), container.getErrorMessageDataValidate());
					doResponseError(context, null);
				}
			} catch (Exception e) {
				if (e instanceof XCOValidateException) {
					XCOValidateException xcoEx = (XCOValidateException) e;
					context.setErrorInfo(xcoEx.getErrorCode(), xcoEx.getErrorMessage());
				} else {
					context.setErrorInfo(container.getErrorCodeDataValidate(), container.getErrorMessageDataValidate());
				}

				doResponseError(context, e);
				return;
			}
		}

		exec(context, cVo);
	}

	private void exec(RequestContext context, ControllerVo cVo) {
		Throwable ex = null;
		try {
			boolean cache = cVo.cacheGet(context);
			if (!cache) {
				cVo.before(context);
				cVo.exec(context);
				cVo.after(context);
				cVo.cachePut(context);
			}
		} catch (Throwable e) {
			ex = e;
		} finally {
			if (null == ex) {
				doResponseSuccess(context);
			} else {
				context.setErrorInfo(Container.getInstance().getErrorCode(), Container.getInstance().getErrorMessage());
				doResponseError(context, ex);
			}
		}
	}

	/**
	 * 对上下文进行预处理
	 */
	private RequestContext pretreatmentContext(HttpServletRequest req, HttpServletResponse resp, RequestTypeEnum requestType) {
		RequestContext context = Container.getInstance().requestContextThreadLocal.get();
		if (null == context) { // fix bug
			context = new RequestContext(req, resp, false);
			context.setRequestType(requestType);
		}
		return context;
	}

	private void doResponseError(RequestContext context, Throwable ex) {
		if (null != ex) {
			log.error(null, ex);
		}
		try {
			if (context.isAjax()) {
				if (DataFormatEnum.JSON == context.getDataFormat()) {
					jsonResponseHandler.onError(context);
				} else {
					xcoResponseHandler.onError(context);
				}
			} else {
				context.getResponse().sendRedirect(Container.getInstance().getErrorRedirectPage());
			}
		} catch (IOException e) {
			log.error("doResponseError Error", e);
		} finally {
			if (context.isInThread()) {
				Container.getInstance().requestContextThreadLocal.remove();
			}
		}
	}

	private void doResponseSuccess(RequestContext context) {
		HttpServletResponse response = context.getResponse();
		HttpServletRequest request = context.getRequest();
		try {
			if (context.isAjax()) {
				if (DataFormatEnum.JSON == context.getDataFormat()) {
					jsonResponseHandler.onSuccess(context);
				} else {
					xcoResponseHandler.onSuccess(context);
				}
			} else {
				String view = context.getView();
				if (null != view) {
					if (context.isForward()) {
						RequestDispatcher dispatcher = request.getRequestDispatcher(view);
						dispatcher.forward(request, response);
					} else {
						response.sendRedirect(context.getView());
					}
				} else {
					// response.sendRedirect(Container.getInstance().getErrorRedirectPage());
				}
			}
		} catch (Exception e) {
			log.error("doResponseSuccess Error", e);
		} finally {
			if (context.isInThread()) {
				Container.getInstance().requestContextThreadLocal.remove();
			}
		}
	}
}
