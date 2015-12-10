package com.ginkgocap.ywxt.framework.dal.dao.model;

import java.util.List;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:27
 * @Copyright Copyright©2015 www.gintong.com
 */
public class SqlInfo {

	private String sql;
	private Object[] params;
	private List<ScalarInfo> scalarList;
	
    public SqlInfo(){
    	;
    }
    
    public SqlInfo(String SQL,Object[] paramArray,List<ScalarInfo> scaList){
    	sql = SQL;
    	params = paramArray;
    	scalarList = scaList;
    }
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	public List<ScalarInfo> getScalarList() {
		return scalarList;
	}
	public void setScalarList(List<ScalarInfo> scalarList) {
		this.scalarList = scalarList;
	}
	
	public String getParamKey(){
		StringBuffer strBuf = new StringBuffer();
		if(null != params){
			for(Object obj: params){
				strBuf.append(obj).append("-");
			}
		}
		return strBuf.toString();
	}
	
}
