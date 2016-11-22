package org.xson.web.cache.creater;

import org.xson.web.cache.ICache;
import org.xson.web.cache.RedisCache;
import org.xson.web.cache.vo.CacheVo;

public class RedisCreater implements ICreate {

	public ICache create(CacheVo cacheVo) {
		RedisCache cache = new RedisCache();
		cache.start(cacheVo.getResource(), cacheVo.getProperties());
		return cache;
	}

}
