package com.ginkgocap.ywxt.framework.dal.cache.utils;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:14:30
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class DebugTimeUtils {
    private static ThreadLocal<Long> preTime = new ThreadLocal<Long>();

    public static void begTime() {
        preTime.set(System.currentTimeMillis());
    }

    public static long getDistanceTime() {
        return System.currentTimeMillis() - preTime.get();
    }
}
