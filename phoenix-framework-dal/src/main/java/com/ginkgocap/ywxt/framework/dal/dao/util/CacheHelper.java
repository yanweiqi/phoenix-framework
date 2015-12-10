package com.ginkgocap.ywxt.framework.dal.dao.util;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.RemoteCacheFactoryImpl;
import com.ginkgocap.ywxt.framework.dal.dao.helper.LogHelper;


/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:56
 * @Copyright Copyright©2015 www.gintong.com
 */
public  class CacheHelper {
	
	private static Log  log = LogFactory.getLog(CacheHelper.class);

    public static Cache getListCache(String regionName) {
       Cache cache = RemoteCacheFactoryImpl.getInstance().getCache(regionName, Boolean.TRUE);
       if(null == cache){
    	   LogHelper.cacheDontConfigured(log, regionName);
       }
       return cache;
    }

    public static Cache getClassCache(Class clazz) {
        if (null == clazz) {
            return null;
        }
        return getListCache(clazz.getName());
    }

    public static Cache getObjectCache(Object object){
        if(null == object){
            return null;
        }
        return getListCache(object.getClass().toString());
    }

    public static void put(String region,String key,Object value) throws CacheException{
    	Cache cache = getListCache(region);
    	if(null != cache){
    	   cache.put(key, value);
    	}
    }


    public static void save(Class objCls,Serializable id, Object obj) throws CacheException{
    	Cache cache = getClassCache(objCls);
    	if(null != cache){
    	   cache.save(id.toString(), obj);
    	}
    }

    public static Object get(String region,String key) throws CacheException{
    	Object obj = null;
    	Cache cache =getListCache(region);
    	if(null!= cache && null != key){
    		obj = cache.get(key);
    	}
    	return obj;
    }
    

    public static Object[] gets(String region,List keyLs) throws CacheException{
       Cache cache = getListCache(region);
       if(null != cache && null != keyLs && keyLs.size() >0){
    	   return cache.get((String[])keyLs.toArray(new String[keyLs.size()]));
       }
       return null;
    }
    
    public static void delete(String region,String key) throws CacheException{  	
    	Cache cache = getListCache(region);
    	if(null != cache && null != key){
    		cache.delete(key);
    	}
    }

    public static void remove(String region,String key) throws CacheException{
    	Cache cache =getListCache(region);
    	if(null != cache){
    		cache.remove(key);
    	}
    }
    public static void update(String region,String key,Object value) throws CacheException{
    	Cache cache = getListCache(region);
    	if(null != cache){
    		cache.update(key, value);
    	}
    }


    public static boolean isDelete(String region,String key) throws CacheException{
    	boolean res = true;
    	Cache cache = getListCache(region);
    	if(null != cache){
    		res = cache.isDelete(key);
    	}
    	return res;
    }


     
}
