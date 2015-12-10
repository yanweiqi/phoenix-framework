package com.ginkgocap.ywxt.framework.dal.cache.config.model;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:15
 * @Copyright Copyright©2015 www.gintong.com
 */
public class QueueItem {
    private String datasource = null;
    private String name = null;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    /**
         * 
         * @return 
         * @author 
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("QueueItem[");
            buffer.append("datasource = ").append(datasource);
            buffer.append(",\n name = ").append(name);
            buffer.append("]");
            return buffer.toString();
        }
    
}
