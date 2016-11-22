package org.xson.web.cache.helper;

import java.util.Map;

import org.xson.web.Container;
import org.xson.web.cache.MemcachedCache;
import org.xson.web.cache.vo.CacheVo;

import com.whalin.MemCached.MemCachedClient;

public class MemcachedHelper {

	/**
	 * 从系统容器中获取MemcachedCache实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static MemCachedClient getMemcachedCache(String cacheId) {
		Map<String, CacheVo> cacheVoMap = Container.getInstance().getCacheVoMap();
		CacheVo cacheVo = cacheVoMap.get(cacheId);
		if (null != cacheVo) {
			MemcachedCache memcachedCache = (MemcachedCache) cacheVo.getCache();
			return memcachedCache.getCachedClient();
		}
		return null;
	}

}
