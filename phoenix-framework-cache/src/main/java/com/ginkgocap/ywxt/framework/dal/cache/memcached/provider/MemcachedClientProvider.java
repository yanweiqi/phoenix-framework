package com.ginkgocap.ywxt.framework.dal.cache.memcached.provider;

import net.rubyeye.xmemcached.MemcachedClient;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:05
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface MemcachedClientProvider {

	public MemcachedClient getMemcachedClient();
	
	public boolean isDynamic();
}
