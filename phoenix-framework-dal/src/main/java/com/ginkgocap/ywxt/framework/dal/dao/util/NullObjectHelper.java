package com.ginkgocap.ywxt.framework.dal.dao.util;

import java.util.List;

import com.ginkgocap.ywxt.framework.dal.cache.NullObjectContent;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:29
 * @Copyright Copyright©2015 www.gintong.com
 */
public class NullObjectHelper {

	public static void removeNullMapsOfObject(Object object) throws Exception{
		List<MapInfo> newMaps = ObjectUtil.getMapInfoList(object);
		for (MapInfo info : newMaps) {
			NullObjectContent.remove(Constants.NullObjectPrefix+"_"+info.getRegion()+"_"+info.getKey()+"");
		}
	}
}
