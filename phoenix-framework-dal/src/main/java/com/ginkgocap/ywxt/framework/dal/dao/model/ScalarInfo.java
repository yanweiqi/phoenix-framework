package com.ginkgocap.ywxt.framework.dal.dao.model;

import org.hibernate.type.Type;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:14
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ScalarInfo {

	private String columnName;
	private Type   columnType;
	
	public ScalarInfo(String name,Type type){
		this.columnName = name;
		this.columnType = type;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Type getColumnType() {
		return columnType;
	}
	public void setColumnType(Type columnType) {
		this.columnType = columnType;
	}
	
}
