package com.ginkgocap.ywxt.framework.dal.cache.exception;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:37
 * @Copyright Copyright©2015 www.gintong.com
 */
public class NotFoundKeyException extends CacheException {
    /**
     * 
     */
    private static final long serialVersionUID = 5585853019888729862L;

    public NotFoundKeyException() {
        super();
    }

    public NotFoundKeyException(String message) {
        super(message);
    }

    public NotFoundKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundKeyException(Throwable cause) {
        super(cause);
    }
}
