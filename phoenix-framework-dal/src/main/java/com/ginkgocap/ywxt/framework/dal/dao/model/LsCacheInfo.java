package com.ginkgocap.ywxt.framework.dal.dao.model;


/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:09
 * @Copyright Copyright©2015 www.gintong.com
 */
public class LsCacheInfo {

    private String region;
    private Object[] params;
    private String key;
    
    //从dal-1.0.6开始，缓存中不再存储ListInfo对象，而是分开存储Count和visiteInfoKey
    private String countKey;
    
    private Object[] orderByParams;       
    private String   orderByKey;
    
	private boolean update = false;;
    private Integer limitLen = 100;
    private Integer initLen  = 50 ;

    
    public LsCacheInfo(String reg,Object[] parameters) {
        region = reg;
        if(parameters != null){
        	params = new Object[parameters.length];
        	System.arraycopy(parameters, 0, params, 0, parameters.length);
        }
        key = getKeyByParams();
    }
    
    public LsCacheInfo(String reg,Object[] parameters,Object[] orderParams,Integer limitLen,Integer initLen,boolean bUpdate) {
        region = reg;
        if(parameters != null){
        	params = new Object[parameters.length];
        	System.arraycopy(parameters, 0, params, 0, parameters.length);
        }
        key = getKeyByParams();
        if(orderParams != null){
        	orderByParams = new Object[orderParams.length];
        	System.arraycopy(orderParams, 0, orderByParams, 0, orderParams.length);
        }
        orderByKey = getOrderByKeyByOrderByParams();
        this.update = bUpdate;
        this.limitLen = limitLen;
        this.initLen = initLen;
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
    	if(params != null){
    		this.params = new Object[params.length];
        	System.arraycopy(params, 0, this.params, 0, params.length);
    	}
    }

    public String getKey() {
    	if(null == key){
    		key = getKeyByParams();
    	}
        return key;
    }
    public String getListCntKey(){
    	return getKey()+"#C";
    }
   
    private String getKeyByParams() {
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
    
    private String getOrderByKeyByOrderByParams() {
        if (null == orderByParams || orderByParams.length < 1) {
            return "";
        }
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < orderByParams.length; i++) {
            strBuf.append(orderByParams[i]);
            if (i != orderByParams.length - 1) {
                strBuf.append("_");
            }
        }
        return strBuf.toString();
    }

	public Object[] getOrderByParams() {
		return orderByParams;
	}

	public void setOrderByParams(Object[] orderByParams) {
	 	if(orderByParams != null){
    		this.orderByParams = new Object[orderByParams.length];
        	System.arraycopy(orderByParams, 0, this.orderByParams, 0, orderByParams.length);
    	}
	}

	public String getOrderByKey() {
		if(null == orderByKey){
			orderByKey = getOrderByKeyByOrderByParams();
		}
		return orderByKey;
	}

	public void setOrderByKey(String orderByKey) {
		this.orderByKey = orderByKey;
	}
	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public Integer getLimitLen() {
		return limitLen;
	}

	public void setLimitLen(Integer limitLen) {
		this.limitLen = limitLen;
	}

	public Integer getInitLen() {
		return initLen;
	}

	public void setInitLen(Integer initLen) {
		this.initLen = initLen;
	}
	

}
