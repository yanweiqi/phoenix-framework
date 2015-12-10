package com.ginkgocap.ywxt.framework.dal.cache.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.CacheUtil;
import com.ginkgocap.ywxt.framework.dal.cache.ListResult;
import com.ginkgocap.ywxt.framework.dal.cache.LocalCacheFactory;
import com.ginkgocap.ywxt.framework.dal.cache.client.CacheClient;
import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;
import com.ginkgocap.ywxt.framework.dal.cache.exception.NotFoundKeyException;
import com.ginkgocap.ywxt.framework.dal.cache.listener.Listener;
import com.ginkgocap.ywxt.framework.dal.cache.route.ContextHolder;
import com.ginkgocap.ywxt.framework.dal.cache.route.RoutingService;
import com.ginkgocap.ywxt.framework.dal.cache.utils.DebugTimeUtils;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:50
 * @Copyright Copyright©2015 www.gintong.com
 */

public class CacheImpl implements Cache {
	private static Logger logger = Logger.getLogger(CacheImpl.class);
	private String regionName;
	private CacheClient client;
	private boolean hasRoute = false;
	private RoutingService routingService;
	private Listener listener;
	private boolean enableLocalCache = false; // 是否支持本地Cache
	private boolean enableDelMark = false; // 是否支持删除打标�?
	private Map<String, Object> delMarkMap = Collections.synchronizedMap(new LRUMap(5000));
	private Cache localCache;
	private boolean hasClassInfo = true;
	private static final byte DELETE_BYTE = '1';

	private int limitLen = 1000;
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

	public boolean isHasClassInfo() {
		return hasClassInfo;
	}

	public void setHasClassInfo(boolean hasClassInfo) {
		this.hasClassInfo = hasClassInfo;
	}

	public CacheImpl() {
	}

	/**
	 * 
	 * @param region
	 * @param client
	 * @param routingService
	 */
	public CacheImpl(String region, CacheClient client, RoutingService routingService) {
		this.client = client;
		this.regionName = region;
		this.hasRoute = client.isDynamic();
		this.routingService = routingService;
		if (this.hasRoute && this.routingService == null) {
			throw new RuntimeException("Must set RoutingService, because CacheClient is Dynamic CacheClient!");
		}
		if (StringUtils.isEmpty(regionName)) {
			throw new RuntimeException("region name must have value!");
		}
	}

	/**
     * 
     */
	public boolean delete(String key) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or null");
			return false;
		}
		boolean b = false;
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
		exeBeforeListener(key, null, "delete");
		if (this.enableLocalCache) {
			getLocalCache().delete(key);
			if (this.enableDelMark) {
				getLocalCache().put(sDelMarkKey, Boolean.TRUE);
			}
		}
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			b = this.client.delete(sTempKey);
			if (this.enableDelMark) {
				if (this.isHasClassInfo()) {
					this.client.set(sDelMarkKey, Boolean.TRUE);
				} else {
					this.client.set(sDelMarkKey, DELETE_BYTE);
				}
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, null, "delete");

		if (logger.isDebugEnabled()) {
			logger.debug("delete time : " + DebugTimeUtils.getDistanceTime());
		}
		return b;
	}

	/**
     * 
     */
	public boolean remove(String key) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or null");
			return false;
		}
		boolean b = false;
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		exeBeforeListener(key, null, "remove");
		if (this.enableLocalCache) {
			getLocalCache().remove(key);
		}
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			b = this.client.remove(sTempKey);

		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, null, "remove");

		if (logger.isDebugEnabled()) {
			logger.debug("remove time : " + DebugTimeUtils.getDistanceTime());
		}
		return b;
	}

	/**
     * 
     */
	public Object get(String key) throws CacheException {

		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or null");
			return null;
		}
		Object o = null;
		exeBeforeListener(key, null, "get");
		String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
		if (this.enableLocalCache) {
			o = getLocalCache().get(key);
			if (this.enableDelMark && o != null) {
				Boolean b = (Boolean) getLocalCache().get(sDelMarkKey);
				if (b != null && b) {
					putDelKey(key);
					getLocalCache().delete(key);
					return null;
				}
			}
		}

		if (o == null) {
			String sTempKey = CacheUtil.keyEncode(regionName, key);
			try {
				if (this.hasRoute) {
					routingService.setRoutingStrategy(this.regionName, key);
				}

				if (this.enableDelMark) {
					String[] keys = new String[] { sTempKey, sDelMarkKey };
					Object[] rets = this.client.getMultiArray(keys);
					if (!ArrayUtils.isEmpty(rets)) {
						if (rets.length > 1 && rets[1] != null) {
							putDelKey(key);
							if (this.enableLocalCache && this.enableDelMark) {
								getLocalCache().put(sDelMarkKey, Boolean.TRUE);
							}
							// this.client.remove(sTempKey);
							o = null;
						} else {
							o = rets[0];
						}
					}
				} else {
					o = this.client.get(sTempKey);
				}

				if (this.enableLocalCache && o != null) {
					getLocalCache().put(key, o);
				}
			} catch (Exception e) {
				throw new CacheException(e);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.info("location cache have value");
			}
		}
		exeAfterListener(key, o, "get");
		if (logger.isDebugEnabled()) {
			logger.debug("get time : " + DebugTimeUtils.getDistanceTime());
		}
		return o;
	}

	/**
     * 
     */
	public boolean put(String key, Object value) throws CacheException {
		return put(key, value, key);
	}

	/**
     * 
     */
	public boolean put(String key, Object value, String dispatchKey) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key) || value == null) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or value is null");
			return false;
		}
		boolean bResult = false;
		exeBeforeListener(key, value, "put");
		if (this.enableLocalCache) {
			getLocalCache().put(key, value);
		}
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, dispatchKey);
			}
			Object mValue = value;
			bResult = this.client.set(sTempKey, mValue);
		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, value, "put");
		if (logger.isDebugEnabled()) {
			logger.debug("put time : " + DebugTimeUtils.getDistanceTime());
		}
		return bResult;
	}

	/**
     * 
     */
	public boolean update(String key, Object value) throws CacheException {
		DebugTimeUtils.begTime();
		boolean b = false;
		if (StringUtils.isEmpty(key) || value == null) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or value is null");
			return false;
		}
		exeBeforeListener(key, value, "update");
		if (this.enableLocalCache) {
			getLocalCache().update(key, value);
		}
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			b = this.client.set(sTempKey, value);

		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, value, "update");
		if (logger.isDebugEnabled()) {
			logger.debug("put time : " + DebugTimeUtils.getDistanceTime());
		}
		return b;
	}

	public String getRegion() throws CacheException {
		return regionName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public CacheClient getClient() {
		return client;
	}

	public void setClient(CacheClient client) {
		this.client = client;
	}

	public boolean isHasRoute() {
		return hasRoute;
	}

	public void setHasRoute(boolean hasRoute) {
		this.hasRoute = hasRoute;
	}

	public RoutingService getRoutingService() {
		return routingService;
	}

	public void setRoutingService(RoutingService routingService) {
		this.routingService = routingService;
	}

	public void regListener(Listener listener) {
		this.listener = listener;
	}

	/**
	 * 
	 * @return
	 */
	private Cache getLocalCache() {
		if (this.localCache == null) {
			this.localCache = LocalCacheFactory.getInstance().getCache(this.regionName);
		}
		return this.localCache;
	}

	private void exeBeforeListener(Object key, Object value, String operateMethod) {
		if (this.listener != null) {
			this.listener.beforeListener(this, key, value, operateMethod);
		}
	}

	private void exeAfterListener(Object key, Object value, String operateMethod) {
		if (this.listener != null) {
			this.listener.afterListener(this, key, value, operateMethod);
		}
	}

	private void exeBeforeListener(Object[] keys, Object[] values, String operateMethod) {
		if (this.listener != null) {
			this.listener.beforeListener(this, keys, values, operateMethod);
		}
	}

	private void exeAfterListener(Object keys[], Object values[], String operateMethod) {
		if (this.listener != null) {
			this.listener.afterListener(this, keys, values, operateMethod);
		}
	}

	public boolean save(String key, Object value) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key) || value == null) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or value is null");
			return false;
		}
		boolean bResult = false;
		exeBeforeListener(key, value, "save");
		if (this.enableLocalCache) {
			getLocalCache().save(key, value);
		}
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			bResult = this.client.set(sTempKey, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, value, "save");
		if (logger.isDebugEnabled()) {
			logger.debug("save time : " + DebugTimeUtils.getDistanceTime());
		}
		return bResult;
	}

	private void putDelKey(String key) {
		if (!delMarkMap.containsKey(key)) {
			delMarkMap.put(key, Boolean.TRUE);
		}
	}

	public boolean isDelete(String key) throws CacheException {
		// return false;
		if (this.enableDelMark) {
			return this.delMarkMap.containsKey(key);
		} else {
			return false;
		}
	}

	public void setEnableLocalCache(boolean enableLocalCache) {
		this.enableLocalCache = enableLocalCache;
	}

	public Object[] get(String[] keys) throws CacheException {
		DebugTimeUtils.begTime();
		if (ArrayUtils.isEmpty(keys)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " keys is empty or null");
			return null;
		}

		exeBeforeListener(keys, null, "get");

		// init get pre Environment

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> memCacheKeys = new ArrayList<String>();

		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (null != key) {
				if (this.enableLocalCache) {
					Object o = getLocalCache().get(key);
					if (this.enableDelMark) {
						String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
						Boolean b = (Boolean) getLocalCache().get(sDelMarkKey);
						if (b != null && b) {
							putDelKey(key);
						}
					}

					if (null != o) {
						resultMap.put(key, o);
					} else {
						memCacheKeys.add(key);
					}
				} else {
					memCacheKeys.add(key);
				}
			}
		}

		if (memCacheKeys.size() > 0) {
			Map<String, List<String>> sourceMap = new HashMap<String, List<String>>();
			Map<String, List<String>> delMarkSourceMap = new HashMap<String, List<String>>();
			for (String key : memCacheKeys) {

				if (this.hasRoute) {
					try {
						routingService.setRoutingStrategy(this.regionName, key);
					} catch (Exception e) {
						throw new CacheException(e);
					}
				}

				String cacheName = ObjectUtils.toString(ContextHolder.getMemcachedName(), "null");
				// ID分组
				List<String> sourceKeys = sourceMap.get(cacheName);
				if (sourceKeys == null) {
					sourceKeys = new ArrayList<String>();
					sourceMap.put(cacheName, sourceKeys);
				}
				String sTempKey = CacheUtil.keyEncode(regionName, key);
				sourceKeys.add(sTempKey);

				// 删除标记分组
				if (this.enableDelMark) {
					List<String> sourceDelMarkKeys = delMarkSourceMap.get(cacheName);
					if (sourceDelMarkKeys == null) {
						sourceDelMarkKeys = new ArrayList<String>();
						delMarkSourceMap.put(cacheName, sourceDelMarkKeys);
					}

					String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
					sourceDelMarkKeys.add(sDelMarkKey);
				}
			}
			// 总算�?��取了
			for (Entry<String, List<String>> entry : sourceMap.entrySet()) {
				String sourceName = entry.getKey();
				ContextHolder.setCachdName("null".equals(sourceName) ? null : sourceName);
				List<String> sourceKeys = entry.getValue();
				if (CollectionUtils.isNotEmpty(sourceKeys)) {
					String[] a = new String[sourceKeys.size()];
					a = sourceKeys.toArray(a);
					Object[] oResults = MultiThreadGet.get(this.client, a, ObjectUtils.toString(ContextHolder.getMemcachedName(), null));

					for (int i = 0; i < oResults.length; i++) {
						Object o = oResults[i];
						String key = CacheUtil.keyDecode(regionName, a[i]);
						if (null != o && null != key) {
							if (this.enableLocalCache) {
								getLocalCache().put(key, o);
							}
							resultMap.put(key, o);
						}
					}
				} else {
					continue; // 删除的不用检查了
				}

				// 删除标记
				if (this.enableDelMark) {
					List<String> sourceDelMarkKeys = delMarkSourceMap.get(sourceName);
					if (CollectionUtils.isNotEmpty(sourceDelMarkKeys)) {
						String[] a = new String[sourceDelMarkKeys.size()];
						a = sourceDelMarkKeys.toArray(a);
						Object[] oResults = this.client.getMultiArray(a);
						for (int i = 0; i < oResults.length; i++) {
							Object o = oResults[i];
							String key = a[i];
							if (null != o && key != null) {
								putDelKey(key);
								// getLocalCache().delete(key);
							}
						}
					}
				}
			}

		}

		Object[] lResult = new Object[keys.length];

		int size = keys.length;

		for (int i = 0; i < size; i++) {
			String key = keys[i];
			lResult[i] = key == null ? null : resultMap.get(key);
		}

		exeAfterListener(keys, lResult, "get");
		if (logger.isDebugEnabled()) {
			logger.debug("gets time : " + DebugTimeUtils.getDistanceTime());
		}

		return lResult;
	}

	public boolean save(Map<String, Object> objectsMap) throws CacheException {
		CacheException lastException = null;
		if (MapUtils.isNotEmpty(objectsMap)) {
			// Set<String> keySet = objectsMap.keySet();
			Set<Entry<String, Object>> set = objectsMap.entrySet();
			Iterator<Entry<String, Object>> iterator = set.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().getKey();
				Object o = iterator.next().getValue();
				if (o != null) {
					try {
						this.save(key, o);
					} catch (CacheException e) {
						lastException = e;
						e.printStackTrace(System.err);
					}
				}

			}
		}
		if (lastException != null) {
			throw lastException;
		}
		return true;
	}

	@Override
	public long decr(String key, long inc) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty");
			throw new CacheException("key is must have value");
		}

		long bResult = -1;
		exeBeforeListener(key, inc, "decr");
		if (this.enableLocalCache) {
			// getLocalCache().incr(key, inc);
			logger.warn("don't suport local cache in incr method");
		}
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}

			bResult = this.client.decr(sTempKey, inc);
		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, inc, "decr");
		if (logger.isDebugEnabled()) {
			logger.debug("decr time : " + DebugTimeUtils.getDistanceTime());
		}
		return bResult;
	}

	@Override
	public long incr(String key, long inc) throws CacheException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty");
			throw new CacheException("key is must have value");
		}
		long bResult = -1;
		exeBeforeListener(key, inc, "incr");
		if (this.enableLocalCache) {
			logger.warn("don't suport local cache in incr method");
		}
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}

			bResult = this.client.incr(sTempKey, inc);
		} catch (Exception e) {
			throw new CacheException(e);
		}
		exeAfterListener(key, inc, "inc");
		if (logger.isDebugEnabled()) {
			logger.debug("incr time : " + DebugTimeUtils.getDistanceTime());
		}
		return bResult;
	}

	@Override
	public ListResult ladd(String key, String value) throws NotFoundKeyException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key) || value == null) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or value is null");
			return ListResult.LIST_ERROR;
		}
		boolean bResult = false;
		exeBeforeListener(key, value, "ladd");
		// if (this.enableLocalCache) {
		// getLocalCache().save(key, value);
		// }
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			// Object mValue = this.isHasClassInfo() ? value :
			// BindingHelper.getByteFromObject(value);
			bResult = this.client.prepend(sTempKey, value + ",");
			if (!bResult) {
				return ListResult.LIST_NOTEXIST;
				// throw new NotFoundKeyException("don't find " + sTempKey);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		exeAfterListener(key, value, "ladd");
		if (logger.isDebugEnabled()) {
			logger.debug("ladd time : " + DebugTimeUtils.getDistanceTime());
		}
		return ListResult.LIST_OK;
	}

	@Override
	public List<String> lrange(String key, int beg, int end) throws NotFoundKeyException {
		List<String> lResult = null;
		if (beg > end || end == 0) {
			return lResult;
		}

		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or null");
			return null;
		}
		Object o = null;
		exeBeforeListener(key, null, "lrange");
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}

			o = this.client.get(sTempKey);

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
							logger.info(key + " list too long , begin remove");
							this.remove(key);
						}
					}
				}
			}

		} catch (Exception e) {
			throw new NotFoundKeyException(e);
		}
		exeAfterListener(key, o, "get");
		if (logger.isDebugEnabled()) {
			logger.debug("get time : " + DebugTimeUtils.getDistanceTime());
		}
		return lResult;
	}

	@Override
	public ListResult radd(String key, String value) throws NotFoundKeyException {
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key) || value == null) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or value is null");
			return ListResult.LIST_ERROR;
		}
		boolean bResult = false;
		exeBeforeListener(key, value, "radd");
		// if (this.enableLocalCache) {
		// getLocalCache().save(key, value);
		// }
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}
			// Object mValue = this.isHasClassInfo() ? value :
			// BindingHelper.getByteFromObject(value);
			bResult = this.client.append(sTempKey, "," + value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		exeAfterListener(key, value, "radd");
		if (logger.isDebugEnabled()) {
			logger.debug("ladd time : " + DebugTimeUtils.getDistanceTime());
		}
		if (!bResult) {
			return ListResult.LIST_NOTEXIST;
			// throw new NotFoundKeyException("don't find " + sTempKey);
			// NOT_STOREED
		}
		return ListResult.LIST_OK;
	}

	@Override
	public ListResult removeList(String key) throws CacheException {
		if (this.remove(key)) {
			return ListResult.LIST_OK;
		} else {
			return ListResult.LIST_LIMT;
		}
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
				if (this.hasRoute) {
					routingService.setRoutingStrategy(this.regionName, key);
				}
				String sTempKey = CacheUtil.keyEncode(regionName, key);
				if (this.client.setList(sTempKey, sb.toString())) {
					return ListResult.LIST_OK;
				} else {
					return ListResult.LIST_ERROR;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.info("value list is empty or null");
			return ListResult.LIST_OK;
		}
	}

	public Object[] gets(String[] keys) throws CacheException {
		DebugTimeUtils.begTime();
		if (ArrayUtils.isEmpty(keys)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " keys is empty or null");
			return null;
		}

		exeBeforeListener(keys, null, "get");

		// init get pre Environment

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> memCacheKeys = new ArrayList<String>();

		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (null != key) {
				if (this.enableLocalCache) {
					Object o = getLocalCache().get(key);
					if (this.enableDelMark) {
						String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
						Boolean b = (Boolean) getLocalCache().get(sDelMarkKey);
						if (b != null && b) {
							putDelKey(key);
						}
					}

					if (null != o) {
						resultMap.put(key, o);
					} else {
						memCacheKeys.add(key);
					}
				} else {
					memCacheKeys.add(key);
				}
			}
		}

		if (memCacheKeys.size() > 0) {
			Map<String, List<String>> sourceMap = new HashMap<String, List<String>>();
			Map<String, List<String>> delMarkSourceMap = new HashMap<String, List<String>>();
			for (String key : memCacheKeys) {

				if (this.hasRoute) {
					try {
						routingService.setRoutingStrategy(this.regionName, key);
					} catch (Exception e) {
						throw new CacheException(e);
					}
				}
				String cacheName = ObjectUtils.toString(ContextHolder.getMemcachedName(), "null");
				// ID分组
				List<String> sourceKeys = sourceMap.get(cacheName);
				if (sourceKeys == null) {
					sourceKeys = new ArrayList<String>();
					sourceMap.put(cacheName, sourceKeys);
				}
				String sTempKey = CacheUtil.keyEncode(regionName, key);
				sourceKeys.add(sTempKey);

				// 删除标记分组
				if (this.enableDelMark) {
					List<String> sourceDelMarkKeys = delMarkSourceMap.get(cacheName);
					if (sourceDelMarkKeys == null) {
						sourceDelMarkKeys = new ArrayList<String>();
						delMarkSourceMap.put(cacheName, sourceDelMarkKeys);
					}

					String sDelMarkKey = CacheUtil.delMarkKey(regionName, key);
					sourceDelMarkKeys.add(sDelMarkKey);
				}
			}
			// 总算�?��取了
			for (Entry<String, List<String>> entry : sourceMap.entrySet()) {
				String sourceName = entry.getKey();
				ContextHolder.setCachdName("null".equals(sourceName) ? null : sourceName);
				List<String> sourceKeys = entry.getValue();
				if (CollectionUtils.isNotEmpty(sourceKeys)) {
					String[] a = new String[sourceKeys.size()];
					a = sourceKeys.toArray(a);
					Object[] oResults = this.client.getMultiArray(a);
					for (int i = 0; i < oResults.length; i++) {
						Object o = oResults[i];
						String key = CacheUtil.keyDecode(regionName, a[i]);
						if (null != o && null != key) {
							if (this.enableLocalCache) {
								getLocalCache().put(key, o);
							}
							resultMap.put(key, o);
						}
					}
				} else {
					continue; // 删除的不用检查了
				}

				// 删除标记
				if (this.enableDelMark) {
					List<String> sourceDelMarkKeys = delMarkSourceMap.get(sourceName);
					if (CollectionUtils.isNotEmpty(sourceDelMarkKeys)) {
						String[] a = new String[sourceDelMarkKeys.size()];
						a = sourceDelMarkKeys.toArray(a);
						Object[] oResults = this.client.getMultiArray(a);
						for (int i = 0; i < oResults.length; i++) {
							Object o = oResults[i];
							String key = a[i];
							if (null != o && key != null) {
								putDelKey(key);
								// getLocalCache().delete(key);
							}
						}
					}
				}
			}

		}

		Object[] lResult = new Object[keys.length];

		int size = keys.length;

		for (int i = 0; i < size; i++) {
			String key = keys[i];
			lResult[i] = key == null ? null : resultMap.get(key);
		}

		exeAfterListener(keys, lResult, "get");
		if (logger.isDebugEnabled()) {
			logger.debug("gets time : " + DebugTimeUtils.getDistanceTime());
		}

		return lResult;
	}

	@Override
	public Integer lsize(String key) throws CacheException {
		Integer iResult = -1;
		DebugTimeUtils.begTime();
		if (StringUtils.isEmpty(key)) {
			logger.info("region: " + StringUtils.defaultIfEmpty(this.regionName, "") + " key is empty or null");
			return null;
		}
		Object o = null;
		exeBeforeListener(key, null, "lrange");
		String sTempKey = CacheUtil.keyEncode(regionName, key);
		try {
			if (this.hasRoute) {
				routingService.setRoutingStrategy(this.regionName, key);
			}

			o = this.client.get(sTempKey);

			if (o != null) {
				String resultStr = (String) o;
				String[] strings = StringUtils.split(resultStr, ',');
				if (!ArrayUtils.isEmpty(strings)) {
					iResult = strings.length;
				}
			}
		} catch (Exception e) {
			throw new NotFoundKeyException(e);
		}
		exeAfterListener(key, o, "get");
		if (logger.isDebugEnabled()) {
			logger.debug("get time : " + DebugTimeUtils.getDistanceTime());
		}
		return iResult;
	}

}
