package com.ginkgocap.ywxt.framework.dal.cache.listener;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;



/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:20
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface Listener {
    public abstract void beforeListener(Cache cache, Object key, Object value, String operateMethod);

    public abstract void afterListener(Cache cache, Object key, Object value, String operateMethod);
    
    
    public abstract void beforeListener(Cache cache, Object[] key, Object[] value, String operateMethod);

    public abstract void afterListener(Cache cache, Object[] key, Object[] value, String operateMethod);    

}
