package org.xson.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.web.util.ServletUtils;

/**
 * 请求上下文
 */
public class RequestContext {

	public enum RequestTypeEnum {
		GET, POST
	}

	public enum DataFormatEnum {
		XCO, JSON, KV, FILE
	}

	HttpServletRequest			request;

	HttpServletResponse			response;

	private String				url;

	private String				view;

	private boolean				forward;

	private String				contextType;

	private RequestTypeEnum		requestType;
	// 是否是异步请求
	private boolean				ajax;

	private Object				result;

	private Object				arg;

	private DataFormatEnum		dataFormat;

	// 是否存在上下文
	private boolean				inThread;
	private int					code	= 0;
	private String				message;

	// 附加对象
	private Map<String, Object>	attach	= null;

	public RequestContext(HttpServletRequest request, HttpServletResponse response) {
		this(request, response, true);
	}

	protected RequestContext(HttpServletRequest request, HttpServletResponse response, boolean inThread) {
		this.request = request;
		this.response = response;
		this.inThread = inThread;
		ServletUtils.printHttpHeader(request);// TODO
		this.url = ServletUtils.parseRequestURI(request);
		this.contextType = request.getContentType();// may be is null
		this.dataFormat = ServletUtils.parseDataFormat(this.contextType);
		this.ajax = ServletUtils.isAjax(request, this.dataFormat);
		System.out.println(this);// TODO
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public void setView(String view, boolean forward) {
		this.view = view;
		this.forward = forward;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public String getUrl() {
		return url;
	}

	public String getContextType() {
		return contextType;
	}

	public RequestTypeEnum getRequestType() {
		return requestType;
	}

	public boolean isAjax() {
		return ajax;
	}

	protected void setRequestType(RequestTypeEnum requestType) {
		this.requestType = requestType;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public Object getArg() {
		return arg;
	}

	public DataFormatEnum getDataFormat() {
		return dataFormat;
	}

	public void setArg(Object arg) {
		this.arg = arg;
	}

	public boolean isForward() {
		return forward;
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

	public void setErrorInfo(int code, String message) {
		if (this.code != code) {
			this.code = code;
		}
		if (null == this.message || !this.message.equals(message)) {
			this.message = message;
		}
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isInThread() {
		return inThread;
	}

	public Map<String, Object> getAttach() {
		if (null == attach) {
			attach = new HashMap<String, Object>();
		}
		return attach;
	}

	@Override
	public String toString() {
		return "url: " + url + ", view: " + view + ", contextType: " + contextType + ", ajax: " + ajax + ", dataFormat: " + dataFormat + ", arg:\n"
				+ arg;
	}
}
