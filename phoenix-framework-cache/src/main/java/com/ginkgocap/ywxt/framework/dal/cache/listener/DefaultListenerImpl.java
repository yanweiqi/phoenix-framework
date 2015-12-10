package com.ginkgocap.ywxt.framework.dal.cache.listener;

import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;



/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:14
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DefaultListenerImpl implements Listener {
    private static Logger logger = Logger.getLogger(DefaultListenerImpl.class);

    public DefaultListenerImpl() {

    }

    public void afterListener(Cache cache, Object key, Object value, String operateMethod) {

    }

    public void beforeListener(Cache cache, Object key, Object value, String operateMethod) {
        // try {
        // logger.info((new StringBuilder()).append("beforeListener cache :
        // ").append(cache.getRegion()).append(
        // " key : ").append(ObjectUtils.toString(key, "")).append(
        // " value : ").append(ObjectUtils.toString(value, "")).append(" method:
        // ").append(operateMethod)
        // .toString());
        // } catch (CacheException e) {
        // e.printStackTrace();
        // }
    }

    public void afterListener(Cache cache, Object[] key, Object[] value, String operateMethod) {
        // TODO Auto-generated method stub

    }

    public void beforeListener(Cache cache, Object[] key, Object[] value, String operateMethod) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) {

    }
}
