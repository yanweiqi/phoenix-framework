package com.ginkgocap.ywxt.framework.dal.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.ListResult;
import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;
import com.ginkgocap.ywxt.framework.dal.cache.exception.NotFoundKeyException;
import com.ginkgocap.ywxt.framework.dal.cache.listener.Listener;
import com.ginkgocap.ywxt.framework.dal.cache.utils.DebugTimeUtils;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:00
 * @Copyright Copyright©2015 www.gintong.com
 */
public class LocalCacheImpl implements Cache {
	private static Logger logger = Logger.getLogger(LocalCacheImpl.class);
	private net.sf.ehcache.Cache ehCache;
	private String region;
	private Listener listener;
	private int limitLen = 300;
	private int initLen = 100;

	public int getLimitLen() {
		return limitLen;
	}

	public void setLimitLen(int limitLen) {
		this.limitLen = limitLen;
	}

	public int getInitLen() {
		return initLen;
	}

	public void setInitLen(int initLen) {
		this.initLen = initLen;
	}

	public LocalCacheImpl(net.sf.ehcache.Cache ehCache, String regionName) {
		this.ehCache = ehCache;
		this.region = regionName;
	}

	public net.sf.ehcache.Cache getEhCache() {
		return ehCache;
	}

	public void setEhCache(net.sf.ehcache.Cache ehCache) {
		this.ehCache = ehCache;
	}

	public boolean delete(String key) throws CacheException {
		return this.ehCache.remove(key);
	}

	public boolean remove(String key) throws CacheException {
		return this.ehCache.remove(key);
	}

	public Object get(String key) throws CacheException {
		Element element = this.ehCache.get(key);
		return element == null ? null : element.getObjectValue();
	}

	public String getRegion() throws CacheException {
		return this.region;
	}

	public boolean put(String key, Object value) throws CacheException {
		Element element = new Element(key, value);
		this.ehCache.put(element);
		return true;
	}
	
	public boolean put(String key, Object value, String dispatchKey) throws CacheException {
		return put(key, value);
	}

	public void regListener(Listener listener) {
		this.listener = listener;

	}

	public boolean update(String key, Object value) throws CacheException {
		return this.put(key, value);
	}

	public boolean save(String key, Object value) throws CacheException {
		return this.put(key, value);
	}

	public boolean isDelete(String key) throws CacheException {
		return false;
	}

	public Object[] get(String[] key) throws CacheException {
		if (ArrayUtils.isEmpty(key)) {
			return null;
		}

		Object[] lresult = new Object[key.length];
		for (int i = 0; i < key.length; i++) {
			lresult[i] = this.get(key[i]);
		}
		return lresult;

	}

	public boolean save(Map<String, Object> objectsMap) throws CacheException {
		for (String key : objectsMap.keySet()) {
			this.put(key, objectsMap.get(key));
		}
		return true;
	}

	@Override
	public long decr(String key, long inc) throws CacheException {
		throw new UnsupportedOperationException("decr is not supported in local cache");
	}

	@Override
	public long incr(String key, long inc) throws CacheException {
		throw new UnsupportedOperationException("incr is not supported in local cache");
	}

	@Override
	public ListResult ladd(String key, String value) throws NotFoundKeyException {
		try {
			this.remove(key);
			return ListResult.LIST_OK;
		} catch (CacheException e) {
			e.printStackTrace(System.err);
			return ListResult.LIST_ERROR;
		}
	}

	@Override
	public List<String> lrange(String key, int beg, int end) throws NotFoundKeyException {
		List<String> lResult = null;
        if (beg > end || end == 0 ) {
            return lResult;
        }

        DebugTimeUtils.begTime();
        if (StringUtils.isEmpty(key)) {
            logger.info("region:  key is empty or null");
            return null;
        }
        Object o = null;
        try {
            o = this.get(key);
           
            if (o != null) {
                String resultStr = (String) o;
                String[] strings = StringUtils.split(resultStr, ',');
                if (!ArrayUtils.isEmpty(strings)) {
                    String[] tempArray = (String[]) ArrayUtils.subarray(strings, beg, end);
                    if (!ArrayUtils.isEmpty(tempArray)) {
                        lResult = new ArrayList<String>();
                        for (int i = 0; i < tempArray.length; i++) {
                            lResult.add(tempArray[i]);
                        }
                        if (tempArray.length > this.limitLen) {
                            logger.info("list too long , begin remove");
                            this.remove(key);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new NotFoundKeyException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get time : " + DebugTimeUtils.getDistanceTime());
        }
        return lResult;
	}

	@Override
	public ListResult radd(String key, String value) throws NotFoundKeyException {
		try {
			this.remove(key);
			return ListResult.LIST_OK;
		} catch (CacheException e) {
			e.printStackTrace(System.err);
			return ListResult.LIST_ERROR;
		}

	}

	@Override
	public ListResult removeList(String key) throws CacheException {
		if(this.ehCache.remove(key)) {
			logger.debug(key + " key in cache, remove sucess.");
		} else {
			logger.debug(key + " key not in cache");

		}
		return ListResult.LIST_OK;
	}

	@Override
	public <T> ListResult setList(String key, List<T> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			if (values.size() > this.limitLen) {
				return ListResult.LIST_LIMT;
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < values.size(); i++) {
				Object value = values.get(i);
				if (sb.length() > 0) {
					sb.append(",").append(ObjectUtils.toString(value, ""));
				} else {
					sb.append(ObjectUtils.toString(value, ""));
				}
			}
			try {
				this.put(key, sb.toString());
				return ListResult.LIST_OK;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.info("value list is empty or null");
			return ListResult.LIST_OK;
		}
	}

	@Override
	public Integer lsize(String key) throws CacheException {
		Integer iResult = -1;
        DebugTimeUtils.begTime();
        if (StringUtils.isEmpty(key)) {
            logger.info("region:  key is empty or null");
            return null;
        }
        Object o = null;
        try {
            o = this.get(key);
            if (o != null) {
                String resultStr = (String) o;
                String[] strings = StringUtils.split(resultStr, ',');
                if (!ArrayUtils.isEmpty(strings)) {
                    if (!ArrayUtils.isEmpty(strings)) {
                        iResult = strings.length;
                    }
                }
            }

        } catch (Exception e) {
            throw new NotFoundKeyException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get time : " + DebugTimeUtils.getDistanceTime());
        }
        return iResult;
	}

}
