package com.ginkgocap.ywxt.framework.dal.dao.datasource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:52
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    private static Logger logger = Logger.getLogger(DynamicDataSource.class);

    protected Object determineCurrentLookupKey() {
        Object datasourceName = ContextHolder.getDataSource();
        //logger.info("current datasourceName is: " + ObjectUtils.toString(datasourceName, "idCenterDS"));
        if (datasourceName == null) {
            return "idCenterDS";
        } else {
            return datasourceName;
        }
    }


}
