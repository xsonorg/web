package org.xson.web.cache;

import java.io.InputStream;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.xson.web.util.Resources;

public class EhCacheCache extends AbstractCache {

	private CacheManager	cacheManager	= null;
	private Cache			cache			= null;

	@Override
	public void start(String resource, Map<String, String> properties) {
		if (null != cacheManager) {
			return;
		}
		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			this.cacheManager = CacheManager.create(inputStream);
			this.cache = cacheManager.getCache(cacheManager.getCacheNames()[0]);
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void stop() {
		if (null != cacheManager) {
			cacheManager.shutdown();
		}
	}

	@Override
	public Object getObject(Object key) {
		Element element = this.cache.get(key);
		if (null != element) {
			return element.getObjectValue();
		}
		return null;
	}

	// 1.TTI timeToIdleSeconds is the maximum number of seconds that an element
	// can exist in the cache without being accessed:
	// TTI用于设置对象在cache中的最大闲置时间，就是 在一直不访问这个对象的前提下，这个对象可以在cache中的存活时间。
	// 2.TTL timeToLiveSeconds is the maximum number of seconds that an element
	// can exist in the cache whether
	// or not is has been accessed.
	// TTL用于设置对象在cache中的最大存活时间，就是 无论对象访问或是不访问(闲置),这个对象在cache中的存活时间。
	// 3.If the eternal flag is set, elements are allowed to exist in the cache
	// eternally and none are evicted。
	// 当配置了 eternal ，那么TTI和TTL这两个配置将被覆盖，对象会永恒存在cache中，永远不会过期。

	@Override
	public void putObject(Object key, Object value, Integer time) {
		Element element = null;
		if (null == time) {
			element = new Element(key, value);
		} else {
			element = new Element(key, value, time.intValue(), time.intValue());
		}
		this.cache.put(element);
	}

	@Override
	public Object removeObject(Object key) {
		Object result = getObject(key);
		if (null != result) {
			this.cache.remove(key);
		}
		return result;
	}

	@Override
	public void clear() {
		this.cache.removeAll();
	}

	@Override
	public int getSize() {
		return cache.getSize();
	}

	// //得到缓存对象占用内存的大小
	// cache.getMemoryStoreSize();
	// //得到缓存读取的命中次数
	// cache.getStatistics().getCacheHits()
	// //得到缓存读取的错失次数
	// cache.getStatistics().getCacheMisses()

	public Cache getCache() {
		return cache;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}
}
