package org.xson.web.cache.creater;

import org.xson.web.cache.ICache;
import org.xson.web.cache.MemcachedCache;
import org.xson.web.cache.vo.CacheVo;

public class MemcachedCreater implements ICreate {

	public ICache create(CacheVo cacheVo) {
		MemcachedCache cache = new MemcachedCache();
		cache.start(cacheVo.getResource(), cacheVo.getProperties());
		return cache;
	}

}
