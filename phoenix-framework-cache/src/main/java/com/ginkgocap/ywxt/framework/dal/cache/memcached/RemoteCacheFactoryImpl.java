package com.ginkgocap.ywxt.framework.dal.cache.memcached;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.CacheFactory;
import com.ginkgocap.ywxt.framework.dal.cache.LocalCacheFactory;
import com.ginkgocap.ywxt.framework.dal.cache.client.CacheClient;
import com.ginkgocap.ywxt.framework.dal.cache.impl.CacheImpl;
import com.ginkgocap.ywxt.framework.dal.cache.listener.Listener;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.client.CacheClientImpl;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.config.helper.CacheConfigHelper;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.MemcachedClientProvider;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.route.RoutingServiceImpl;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:39
 * @Copyright Copyright©2015 www.gintong.com
 */

public class RemoteCacheFactoryImpl implements CacheFactory {
	private static Logger logger = Logger.getLogger(RemoteCacheFactoryImpl.class);
	private static CacheFactory cacheFactory;
	private Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

	private MemcachedClientProvider clientProvider; // 非动态
	private MemcachedClientProvider dClientProvider; // 动态
	private Lock lock = new ReentrantLock(false);

	private RemoteCacheFactoryImpl() {
		init();
	}

	private void init() {
		this.clientProvider = CacheConfigHelper.getMemcachedClientProvider("default");
		this.dClientProvider = CacheConfigHelper.getDynamicMemcachedClientProvider();
	}

	public static CacheFactory getInstance() {
		if (cacheFactory == null) {
			synchronized (RemoteCacheFactoryImpl.class) {
				if (cacheFactory == null) {
					cacheFactory = new RemoteCacheFactoryImpl();
				}
			}
		}
		return cacheFactory;
	}

	public Cache getCache(String name) {
		Cache cache = null;
		if (StringUtils.isEmpty(name)) {
			logger.warn("cache name must have value");
			return null;
		} else {
			cache = caches.get(name);
			if (cache == null) {
				if (!CacheConfigHelper.isExistRegion(name)) {// 这个Name（region）不需要Cache
					logger.warn("Please config " + name + " cache in memcached_client.xml");
					return null;
				}
				lock.lock();
				try {
					cache = caches.get(name);
					if (cache == null) {
						if (CacheConfigHelper.isRemoteCache(name)) {
							CacheClient cacheClient = new CacheClientImpl(this.clientProvider);
							cache = new CacheImpl(name, cacheClient, RoutingServiceImpl.getInstance());
							if (cache != null) {
								String listenerClass = CacheConfigHelper.getListenerClass(name);
								Listener listener = null;
								if (StringUtils.isNotEmpty(listenerClass)) {
									try {
										listener = (Listener) Class.forName(listenerClass).newInstance();
									} catch (Exception e) {
										logger.error(e.getMessage());
										e.printStackTrace(System.err);
									}
								}
								cache.regListener(listener);
								((CacheImpl) cache).setEnableLocalCache(CacheConfigHelper.isLocalCache(name));
								((CacheImpl) cache).setHasClassInfo(CacheConfigHelper.isHasClassInfo(name));

								int initLen = CacheConfigHelper.getListLimitLen(name);
								((CacheImpl) cache).setLimitLen(initLen);

							}
						} else {
							if (CacheConfigHelper.isLocalCache(name)) {
								cache = LocalCacheFactory.getInstance().getCache(name);
							}
						}
						if (cache != null) {
							caches.put(name, cache);
						}
					}
				} finally {
					lock.unlock();
				}
			}


		}
		return cache;
	}

	/**
     * 
     */
	public Cache getCache(String name, boolean isDynamic) {
		Cache cache = null;
		if (StringUtils.isEmpty(name)) {
			logger.warn("cache name must have value");
			return null;
		} else {
			cache = caches.get(name);
			if (cache == null) {
				if (!CacheConfigHelper.isExistRegion(name)) {// 这个Name（region）不需要Cache
					return null;
				}
				lock.lock();
				try {
					cache = caches.get(name);
					if (cache == null) {
						if (CacheConfigHelper.isRemoteCache(name)) {
							if (logger.isDebugEnabled()) {
								logger.debug("cache not exist! begin create cache " + name);
							}
							CacheClient cacheClient = new CacheClientImpl();
							if (isDynamic) {
								((CacheClientImpl) cacheClient).setMemcachedClientProvider(this.dClientProvider);
							} else {
								((CacheClientImpl) cacheClient).setMemcachedClientProvider(this.clientProvider);
							}
							cache = new CacheImpl(name, cacheClient, RoutingServiceImpl.getInstance());
							if (cache != null) {
								String listenerClass = CacheConfigHelper.getListenerClass(name);
								Listener listener = null;
								if (StringUtils.isNotEmpty(listenerClass)) {
									try {
										listener = (Listener) Class.forName(listenerClass).newInstance();
									} catch (Exception e) {
										logger.error(e.getMessage());
										e.printStackTrace(System.err);
									}
								}
								cache.regListener(listener);
								((CacheImpl) cache).setEnableLocalCache(CacheConfigHelper.isLocalCache(name));
								((CacheImpl) cache).setHasClassInfo(CacheConfigHelper.isHasClassInfo(name));

								int initLen = CacheConfigHelper.getListLimitLen(name);
								((CacheImpl) cache).setLimitLen(initLen);

								
							}
						}else {
							if (CacheConfigHelper.isLocalCache(name)) {
								cache = LocalCacheFactory.getInstance().getCache(name);
							}
						}
						if (cache != null) {
							caches.put(name, cache);
						}
					} else {
						// if (logger.isDebugEnabled()) {
						// logger.debug(name + " cache have exist!");
						// }
					}
				} finally {
					lock.unlock();
				}
			} else {
				// if (logger.isDebugEnabled()) {
				// logger.debug(name + " cache have exist!");
				// }
			}
			if (cache != null && cache instanceof CacheImpl) {
				CacheClient cacheClient = ((CacheImpl) cache).getClient();
				if (cacheClient.isDynamic() != isDynamic) {
					logger.info("cacheClient dynamic is : " + cacheClient.isDynamic() + " , request is " + isDynamic);
					throw new RuntimeException("Cache don't match specify Dynamic module");
				}
			}

		}
		return cache;
	}

	public void removeCache(String name) {
		if (StringUtils.isNotEmpty(name))
			caches.remove(name);
	}
}
