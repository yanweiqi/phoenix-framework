package com.ginkgocap.ywxt.framework.dao.id;

import com.ginkgocap.ywxt.framework.dao.id.exception.CreateIdException;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:23:33
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface IdCreator {
	
	Long nextId(String sKey) throws CreateIdException;
	
}
