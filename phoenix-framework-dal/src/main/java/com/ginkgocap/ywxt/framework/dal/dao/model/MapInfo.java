package com.ginkgocap.ywxt.framework.dal.dao.model;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:19
 * @Copyright Copyright©2015 www.gintong.com
 */
public class MapInfo {
    private String region;
    private Object[] params;
    private String key;
    private Object value;
    
    public MapInfo(String region,Object[] params){
        super();
        this.region = region;
        this.params = params;
        this.key =getKeyByParams();     
    }
    public MapInfo(String region, Object[] params,Object val) {
        super();
        this.region = region;
        this.params = params;
        this.key =getKeyByParams();
        this.value = val;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    public  String getKeyByParams() {
        if (null == params || params.length < 1) {
            return "";
        }
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            strBuf.append(params[i]);
            if (i != params.length - 1) {
                strBuf.append("_");
            }
        }
        return strBuf.toString();
    }

}
