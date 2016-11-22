package org.xson.web.cache.creater;

import net.sf.ehcache.CacheException;

import org.xson.web.cache.EhCacheCache;
import org.xson.web.cache.ICache;
import org.xson.web.cache.vo.CacheVo;

public class EhcacheCreater implements ICreate {

	public ICache create(CacheVo cacheVo) {
		EhCacheCache cache = new EhCacheCache();
		String resource = cacheVo.getResource();
		if (null == resource) {
			throw new CacheException("missing resource in ehcache type");
		}
		cache.start(resource, cacheVo.getProperties());
		return cache;
	}

}
