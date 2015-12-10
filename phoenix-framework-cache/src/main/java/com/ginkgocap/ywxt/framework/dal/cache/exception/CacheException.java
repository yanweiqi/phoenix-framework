package com.ginkgocap.ywxt.framework.dal.cache.exception;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:30
 * @Copyright Copyright©2015 www.gintong.com
 */
public class CacheException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 5585853019888729862L;

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
