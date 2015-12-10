package com.ginkgocap.ywxt.framework.dal.dao.datasource;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:47
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ContextHolder {
    private static final ThreadLocal contextHolder = new ThreadLocal();

    public static void setDataSource(String datasource) {
        contextHolder.set(datasource);
    }

    public static String getDataSource() {
        return (String) contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }
}
