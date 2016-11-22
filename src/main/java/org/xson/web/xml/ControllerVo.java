package org.xson.web.xml;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.web.RequestContext;
import org.xson.web.cache.vo.CacheUseVo;

import cn.gatherlife.rpc.RpcClient;

public class ControllerVo {

	private String				url;
	private String				transfer;
	private String				validate;
	private MethodObject		execMethod;
	private List<MethodObject>	assemblyMethods;
	private List<MethodObject>	beforeMethods;
	private List<MethodObject>	afterMethods;

	// 权限设置: 用户可自行处理
	private String				permission;

	private CacheUseVo			cacheUse;

	public ControllerVo(String url, String transfer, String validate, MethodObject execMethod, List<MethodObject> assemblyMethods,
			List<MethodObject> beforeMethods, List<MethodObject> afterMethods, String permission, CacheUseVo cacheUse) {
		this.url = url;
		this.transfer = transfer;
		this.validate = validate;
		this.execMethod = execMethod;
		this.assemblyMethods = assemblyMethods;
		this.beforeMethods = beforeMethods;
		this.afterMethods = afterMethods;

		this.permission = permission;
		this.cacheUse = cacheUse;
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

	public String getPermission() {
		return permission;
	}

	public CacheUseVo getCacheUse() {
		return cacheUse;
	}

	public void assembly(RequestContext context) throws Throwable {
		if (null != this.assemblyMethods) {
			for (MethodObject mo : this.assemblyMethods) {
				mo.getMethod().invoke(mo.getInstance(), context);
			}
		}
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
				XCO request = (XCO) context.getArg();
				if (null == request) {
					request = new XCO();
				}
				XCO result = RpcClient.call(transfer, request);
				context.setResult(result);
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public boolean cacheGet(RequestContext context) throws Throwable {
		if (null != cacheUse) {
			Object result = cacheUse.getObject(context.getArg());
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}
		return false;
	}

	public void cachePut(RequestContext context) throws Throwable {
		if (null != cacheUse) {
			cacheUse.putObject(context.getArg(), context.getResult());
		}
	}
}
