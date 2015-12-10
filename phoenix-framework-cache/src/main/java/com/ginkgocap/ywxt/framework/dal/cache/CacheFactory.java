package com.ginkgocap.ywxt.framework.dal.cache;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:14:43
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface CacheFactory {
    public Cache getCache(String name);

    public Cache getCache(String name, boolean isDynamic);

    public void removeCache(String name);
}
