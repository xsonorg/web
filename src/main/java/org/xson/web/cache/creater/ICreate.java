package org.xson.web.cache.creater;

import org.xson.web.cache.ICache;
import org.xson.web.cache.vo.CacheVo;

public interface ICreate {
	
	public ICache create(CacheVo cacheVo);
	
}
