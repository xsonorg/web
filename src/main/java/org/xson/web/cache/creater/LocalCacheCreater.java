package org.xson.web.cache.creater;

import java.util.Map;

import org.xson.web.cache.FIFOCache;
import org.xson.web.cache.ICache;
import org.xson.web.cache.LRUCache;
import org.xson.web.cache.LocalCache;
import org.xson.web.cache.LoggingCache;
import org.xson.web.cache.ScheduledCache;
import org.xson.web.cache.SoftCache;
import org.xson.web.cache.SynchronizedCache;
import org.xson.web.cache.WeakCache;
import org.xson.web.cache.vo.CacheVo;
import org.xson.web.cache.vo.CacheVo.CacheStrategyType;

public class LocalCacheCreater implements ICreate {

	public ICache create(CacheVo cacheVo) {

		Map<String, String> properties = cacheVo.getProperties();

		ICache localCache = new LocalCache(cacheVo.getId());

		CacheStrategyType strategyType = CacheStrategyType.LRU;
		String strategy = properties.get("strategy");
		if (null != strategy) {
			if ("FIFO".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.FIFO;
			} else if ("SOFT".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.SOFT;
			} else if ("WEAK".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.WEAK;
			} else if ("TIME".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.TIME;
			}
		}

		int maxSize = 1024;
		String _maxSize = properties.get("maxSize");
		if (null != _maxSize) {
			maxSize = Integer.parseInt(_maxSize);
		}

		int survivalTime = 10; // 10秒
		String _survivalTime = properties.get("survivalTime");
		if (null != _survivalTime) {
			survivalTime = Integer.parseInt(_survivalTime);
		}

		// 根据设置
		if (CacheStrategyType.LRU == strategyType) {
			localCache = new LRUCache(localCache, maxSize);
		} else if (CacheStrategyType.FIFO == strategyType) {
			localCache = new FIFOCache(localCache, maxSize);
		} else if (CacheStrategyType.SOFT == strategyType) {
			localCache = new SoftCache(localCache, maxSize);
		} else if (CacheStrategyType.WEAK == strategyType) {
			localCache = new WeakCache(localCache, maxSize);
		} else if (CacheStrategyType.TIME == strategyType) {
			localCache = new ScheduledCache(localCache, survivalTime);
		}

		// 如果是local必须
		localCache = new SynchronizedCache(localCache);

		// log可选
		boolean log = false;
		String _log = properties.get("log");
		if (null != _log) {
			log = Boolean.parseBoolean(_log);
		}
		if (log) {
			localCache = new LoggingCache(localCache);
		}

		return localCache;
	}

}
