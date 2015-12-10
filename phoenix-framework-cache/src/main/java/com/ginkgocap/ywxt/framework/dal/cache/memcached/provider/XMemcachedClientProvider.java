package com.ginkgocap.ywxt.framework.dal.cache.memcached.provider;

import net.rubyeye.xmemcached.MemcachedClient;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:16
 * @Copyright Copyright©2015 www.gintong.com
 */
public class XMemcachedClientProvider implements MemcachedClientProvider {
	private MemcachedClient memcachedClient;
	public XMemcachedClientProvider(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}
	@Override
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}
	@Override
	public boolean isDynamic() {
		return false;
	}

}
