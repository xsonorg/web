package org.xson.web.cache.helper;

import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.xson.web.Container;
import org.xson.web.cache.EhCacheCache;
import org.xson.web.cache.vo.CacheVo;

public class EhCacheHelper {

	/**
	 * 从系统容器中获取默认的EhCache实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static net.sf.ehcache.Cache getDefaultEhCache(String cacheId) {
		Map<String, CacheVo> cacheVoMap = Container.getInstance().getCacheVoMap();
		CacheVo cacheVo = cacheVoMap.get(cacheId);
		if (null != cacheVo) {
			EhCacheCache ehCacheCache = (EhCacheCache) cacheVo.getCache();
			return ehCacheCache.getCache();
		}
		return null;
	}

	/**
	 * 从系统容器中获取默认的EhCache CacheManager实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static CacheManager getEhCacheCacheManager(String cacheId) {
		Map<String, CacheVo> cacheVoMap = Container.getInstance().getCacheVoMap();
		CacheVo cacheVo = cacheVoMap.get(cacheId);
		if (null != cacheVo) {
			EhCacheCache ehCacheCache = (EhCacheCache) cacheVo.getCache();
			return ehCacheCache.getCacheManager();
		}
		return null;
	}

}
