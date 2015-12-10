package com.ginkgocap.ywxt.framework.dal.cache.route.strategy;

import com.ginkgocap.ywxt.framework.dal.cache.exception.StrategyException;

/**
 * 策略接口定义
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:52
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface IStrategy {
    /**
     * 根据Region的名字和CACHE中Key来决定用�?��CACHE(memcachedInstance)
     * 
     * @param regionName
     * @param key
     * @return
     */
    public Object playStrategy(String regionName, Object key) throws StrategyException;
}
