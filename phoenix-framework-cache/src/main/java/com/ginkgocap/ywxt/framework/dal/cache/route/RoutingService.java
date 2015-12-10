package com.ginkgocap.ywxt.framework.dal.cache.route;

import com.ginkgocap.ywxt.framework.dal.cache.exception.StrategyException;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:14:22
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface RoutingService {
    public abstract boolean setRoutingStrategy(String regionName, Object key) throws StrategyException;
}