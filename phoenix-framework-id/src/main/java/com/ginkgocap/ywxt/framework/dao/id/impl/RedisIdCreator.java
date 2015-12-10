package com.ginkgocap.ywxt.framework.dao.id.impl;

import com.ginkgocap.ywxt.framework.dao.id.IdCreator;
import com.ginkgocap.ywxt.framework.dao.id.exception.CreateIdException;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:23:23
 * @Copyright Copyright©2015 www.gintong.com
 */
public class RedisIdCreator implements IdCreator {
	
	@Override
	public Long nextId(String sKey) throws CreateIdException {
		return null;
	}

}
