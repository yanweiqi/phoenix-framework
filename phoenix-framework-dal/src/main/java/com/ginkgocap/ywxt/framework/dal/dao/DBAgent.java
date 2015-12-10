package com.ginkgocap.ywxt.framework.dal.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ginkgocap.ywxt.framework.dal.dao.model.SqlInfo;


/**
 * 
 * 
 *
 * @author allenshen   2015年9月15日 下午2:48:06
 * @blog_address http://syh0372.blog.sohu.com
 * Copyright © 2015 gintong Inc. All Rights Reserved.
 */

public interface DBAgent {

	//执行数据库更新的操作
	
	public Serializable save(Object account_id, Object object)throws Exception;
	
	public Map save(Object account_id,final List obs) throws Exception;
	
	public boolean delete(Object account_id, Object obj)throws Exception;
		
	public boolean update(Object account_id, Object object) throws Exception;
	
	public <T> T get(Object account_id, Class<T> clazz, Serializable id) throws Exception;
	
	public int count(Object account_id,String listName, final SqlInfo sqlInfo) throws Exception;
	
	public List getIdList(Object account_id,String listName, final SqlInfo sqlInfo,final Integer start,final Integer count) throws Exception;
	
	public Object getMapping(Object account_id,String listName, final SqlInfo sqlInfo) throws Exception;
	
	public <T> List<T> getObjectList(Object account_id,String listName, final SqlInfo sqlInfo) throws Exception;
 
	public <T> List<T> getEntityList(Object account_id,final Class cls, final SqlInfo sqlInfo,int strategy) throws Exception;

}


