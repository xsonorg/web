package org.xson.web.cache;

import org.xson.web.XcoWebException;

public class CacheException extends XcoWebException {

	private static final long	serialVersionUID	= 1L;

	public CacheException() {
		super();
	}

	public CacheException(String message) {
		super(message);
	}

	public CacheException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheException(Throwable cause) {
		super(cause);
	}
}
