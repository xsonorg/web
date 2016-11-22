package org.xson.web.cache.helper;

import java.util.Map;

import org.xson.thirdparty.redis.JedisClient;
import org.xson.web.Container;
import org.xson.web.cache.RedisCache;
import org.xson.web.cache.vo.CacheVo;

public class RedisCacheHelper {

	/**
	 * 从系统容器中获取JedisClient实例
	 * 
	 * @param cacheId
	 * @return
	 */
	public static JedisClient getJedisClient(String cacheId) {
		Map<String, CacheVo> cacheVoMap = Container.getInstance().getCacheVoMap();
		CacheVo cacheVo = cacheVoMap.get(cacheId);
		if (null != cacheVo) {
			RedisCache redisCache = (RedisCache) cacheVo.getCache();
			return redisCache.getClient();
		}
		return null;
	}

}
