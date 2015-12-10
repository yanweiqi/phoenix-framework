package com.ginkgocap.ywxt.framework.dal.dao.config.model.dao;

import java.io.Serializable;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:31
 * @Copyright Copyright©2015 www.gintong.com
 */
public class IdCenterDsItem implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -5587314855408495330L;
    private String ds;

    public String getDs() {
        return ds;
    }

    public void setDs(String ds) {
        this.ds = ds;
    }
    
    /**
      * @return 
      * @author 
      */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("IdCenterDsItem[");
            buffer.append("ds = ").append(ds);
            buffer.append("]");
            return buffer.toString();
        }
    
}
