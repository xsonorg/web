package org.xson.web.cache.vo;

import org.xson.web.Container;
import org.xson.web.async.AsyncTask;

public class CacheUseVo extends CacheBase {

	private Integer	time;

	public CacheUseVo(CacheVo cacheVo, String key, Integer time, String[] ignore, String service) {
		super(cacheVo, ignore, service);
		this.time = time;
		// 预处理key
		parseKey(service, key);
	}

	public void putObject(final Object arg, final Object value) {
		// 异步操作
		Container.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				String key = buildKey(arg);
				cacheVo.putObject(key, value, time, ignore, service);
			}
		});
	}

	public Object getObject(Object arg) {
		String key = buildKey(arg);
		return cacheVo.getObject(key);
	}

}
