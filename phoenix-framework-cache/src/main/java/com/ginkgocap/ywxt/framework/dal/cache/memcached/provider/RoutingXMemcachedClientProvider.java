package com.ginkgocap.ywxt.framework.dal.cache.memcached.provider;

import java.util.Map;

import com.ginkgocap.ywxt.framework.dal.cache.route.ContextHolder;

import net.rubyeye.xmemcached.MemcachedClient;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:11
 * @Copyright Copyright©2015 www.gintong.com
 */
public class RoutingXMemcachedClientProvider implements MemcachedClientProvider {

	@Override
	public MemcachedClient getMemcachedClient() {
		return determineTargetMemcachedClient();
	}
	
	
	private Map<String, MemcachedClient> resolvedMemcachedClient = null;
	private MemcachedClient resolvedDefaultMemcachedClient;



	public void setResolvedMemcachedSources(Map<String, MemcachedClient> resolvedMemcachedClient) {
		this.resolvedMemcachedClient = resolvedMemcachedClient;
	}

	protected MemcachedClient determineTargetMemcachedClient() {
		if (this.resolvedMemcachedClient == null) {
			throw new RuntimeException("MemcachedSource router not initialized");
		}
		Object lookupKey = determineCurrentLookupKey();
		MemcachedClient memcachedClient = (MemcachedClient) this.resolvedMemcachedClient.get(lookupKey);
		if (memcachedClient == null) {
			memcachedClient = this.resolvedDefaultMemcachedClient;
		}
		if (memcachedClient == null) {
			throw new IllegalStateException("Cannot determine target memcached client for lookup key [" + lookupKey + "]");
		}
		return memcachedClient;
	}

	protected Object determineCurrentLookupKey() {
		Object memcachedName = ContextHolder.getMemcachedName();
		return memcachedName;
	}

	public boolean isDynamic() {
		return true;
	}

}
