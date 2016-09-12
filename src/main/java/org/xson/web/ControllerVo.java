package org.xson.web;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ControllerVo {

	private String				url;
	private String				transfer;
	private String				validate;
	private MethodObject		execMethod;
	private List<MethodObject>	beforeMethods;
	private List<MethodObject>	afterMethods;

	// TODO forward="true"

	public ControllerVo(String url, String transfer, String validate, MethodObject execMethod, List<MethodObject> beforeMethods, List<MethodObject> afterMethods) {
		this.url = url;
		this.transfer = transfer;
		this.validate = validate;
		this.execMethod = execMethod;
		this.beforeMethods = beforeMethods;
		this.afterMethods = afterMethods;
	}

	public String getUrl() {
		return url;
	}

	public String getTransfer() {
		return transfer;
	}

	public String getValidate() {
		return validate;
	}

	public void before(RequestContext context) throws Throwable {
		try {
			if (null != this.beforeMethods) {
				for (MethodObject mo : this.beforeMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public void after(RequestContext context) throws Throwable {
		try {
			if (null != this.afterMethods) {
				for (MethodObject mo : this.beforeMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public void exec(RequestContext context) throws Throwable {
		try {
			if (null != this.execMethod) {
				this.execMethod.getMethod().invoke(this.execMethod.getInstance(), context);
			} else {
				// TODO: 需要依赖Rpc工具实现
				// XCO result = RpcClient.call(transfer);
				// context.setResult(result);
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

}
