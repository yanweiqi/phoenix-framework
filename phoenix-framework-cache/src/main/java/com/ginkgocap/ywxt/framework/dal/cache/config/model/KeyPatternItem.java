package com.ginkgocap.ywxt.framework.dal.cache.config.model;

import java.io.Serializable;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:09
 * @Copyright Copyright©2015 www.gintong.com
 */
public class KeyPatternItem implements Serializable {
	/**
     * 
     */
	private static final long serialVersionUID = 369483380571049497L;
	private String value;
	private String datasource;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		buffer.append("KeyPatternItem[");
		buffer.append("datasource = ").append(datasource);
		buffer.append(" value = ").append(value);
		buffer.append("]");
		return buffer.toString();
	}

}
