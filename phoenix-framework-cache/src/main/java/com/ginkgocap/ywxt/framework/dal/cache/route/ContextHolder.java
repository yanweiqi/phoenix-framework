package com.ginkgocap.ywxt.framework.dal.cache.route;

import org.apache.log4j.Logger;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:14:16
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class ContextHolder {
    private static Logger logger = Logger.getLogger(ContextHolder.class);
    private static final ThreadLocal<Object> contextHolder = new ThreadLocal<Object>();

    public static void setCachdName(Object memcachedName) {
        contextHolder.set(memcachedName);
    }

    public static Object getMemcachedName() {
        if (logger.isDebugEnabled()) {
            logger.debug("current memcached client name is: " +contextHolder.get() );
        }
        return contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }
}
