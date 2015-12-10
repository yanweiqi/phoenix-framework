package com.ginkgocap.ywxt.framework.dal.cache.memcached.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import net.rubyeye.xmemcached.MemcachedClient;

import com.ginkgocap.ywxt.framework.dal.cache.client.CacheClient;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.exception.CacheClientException;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.MemcachedClientProvider;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:32
 * @Copyright Copyright©2015 www.gintong.com
 */
public class CacheClientImpl implements CacheClient {

	private MemcachedClientProvider memcachedClientProvider;

	public CacheClientImpl() {
	}

	public CacheClientImpl(MemcachedClientProvider memcachedClientProvider) {
		this.memcachedClientProvider = memcachedClientProvider;
	}

	public void setMemcachedClientProvider(MemcachedClientProvider memcachedClientProvider) {
		this.memcachedClientProvider = memcachedClientProvider;
	}

	private MemcachedClient getMemcachedClient() {
		return this.memcachedClientProvider.getMemcachedClient();
	}

	@Override
	public boolean delete(String key) {
		try {
			return getMemcachedClient().delete(key);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean remove(String key) {
		try {
			return this.getMemcachedClient().delete(key);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean delete(String key, long opTime) {
		try {
			return this.getMemcachedClient().delete(key, opTime);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean set(String key, Object value) {
		try {
			return this.getMemcachedClient().set(key, 0, value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean set(String key, Object value, Date expiry) {
		try {
			return this.getMemcachedClient().set(key, getExpiryTime(expiry), value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean replace(String key, Object value) {
		try {
			return this.getMemcachedClient().replace(key, 0, value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean replace(String key, Object value, Date expiry) {
		try {
			return this.getMemcachedClient().replace(key, getExpiryTime(expiry), value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public Object get(String key) {
		try {
			return this.getMemcachedClient().get(key);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public <T> T[] getMultiArray(String[] keys) {
		ArrayList<T> resultList = new ArrayList<T>();
		try {

			if (!ArrayUtils.isEmpty(keys)) {
				List<String> keys_list = new ArrayList<String>();
				CollectionUtils.addAll(keys_list, keys);
				Map<String, T> reusltMap = this.getMemcachedClient().get(keys_list);

				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					if (StringUtils.isNotBlank(key)) {
						resultList.add(reusltMap.get(key));
					} else {
						resultList.add(null);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return (T[])resultList.toArray();
	}

	@Override
	public boolean flushAll() {
		try {
			this.getMemcachedClient().flushAll();
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean isDynamic() {
		return this.memcachedClientProvider.isDynamic();
	}

	@Override
	public long incr(String key, long inc) throws CacheClientException{
		try {
			return this.getMemcachedClient().incr(key,inc);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new CacheClientException(e.getMessage());
		}
	}

	@Override
	public long decr(String key, long inc) throws CacheClientException{
		try {
			return this.getMemcachedClient().decr(key,inc);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new CacheClientException(e.getMessage());
		}
	}

	@Override
	public boolean prepend(String key, String value) {
		try {
			return this.getMemcachedClient().prepend(key,value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean append(String key, String value) {
		try {
			return this.getMemcachedClient().append(key,value);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

	@Override
	public boolean setList(String key, String values) {
		return this.set(key, values);
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private int getExpiryTime(Date date) {
		if (date != null) {
			return Integer.valueOf(String.valueOf(date.getTime() / 1000));
		} else {
			return 0;
		}
	}

}
