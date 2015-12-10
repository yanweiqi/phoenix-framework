package com.ginkgocap.ywxt.framework.dal.cache;

import java.util.List;
import java.util.Map;

import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;
import com.ginkgocap.ywxt.framework.dal.cache.exception.NotFoundKeyException;
import com.ginkgocap.ywxt.framework.dal.cache.listener.Listener;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:14:37
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface Cache {
    public void regListener(Listener listener);

    /**
     * Adds data to the Server
     * 
     * @param key
     * @param value
     * @return
     * @throws CacheException
     */
    public boolean put(java.lang.String key, java.lang.Object value) throws CacheException;
    
    /**
     * Adds data to memcacheq, using different queue according to dispatchKey
     * 
     * @param key
     * @param value
     * @param dispatchKey
     * @return
     * @throws CacheException
     */
    public boolean put(java.lang.String key, java.lang.Object value, java.lang.String dispatchKey) throws CacheException;

    /**
     * Adds data to the Server
     * @param key
     * @param value
     * @return
     * @throws CacheException
     */
    public boolean save(java.lang.String key, java.lang.Object value) throws CacheException;

    /**
     * Adds many data to the Server
     * @param key
     * @param value
     * @return
     * @throws CacheException
     */
    public boolean save(Map<String, Object> objectsMap) throws CacheException;    
    /**
     * Retrieve a key from the server,
     * 
     * @param key
     * @return
     * @throws CacheException
     */
    public Object get(java.lang.String key) throws CacheException;
    

    /**
     * Retrieve multiple objects from server,
     * 
     * @param key
     * @return
     * @throws CacheException
     */
    public Object[] get(java.lang.String key[]) throws CacheException;    

    /**
     * 
     * @param key
     * @return
     * @throws CacheException
     */
    public boolean delete(java.lang.String key) throws CacheException;

    /**
     * 
     * @param key
     * @return
     * @throws CacheException
     */
    public boolean remove(java.lang.String key) throws CacheException;

    /**
     * Updates data on the server;
     * 
     * @param key
     * @param value
     * @return
     * @throws CacheException
     */
    public boolean update(java.lang.String key, java.lang.Object value) throws CacheException;

    public String getRegion() throws CacheException;

    public boolean isDelete(java.lang.String key) throws CacheException;
    
    /**
     * @param key
     * @param inc
     * @return
     * @throws CacheException
     */
    public long incr(String key, long inc) throws CacheException;
    
    /**
     * @param key
     * @param inc
     * @return
     * @throws CacheException
     */
    public long decr(String key, long inc) throws CacheException;
    
    /**
     * Add the string value to the head of the liststored at key. If the key
     * does not exist return ListResult.LIST_NOTEXIST, if List length too long
     * return ListResult.List_limit, otherwise return LIST_OK
     * 
     * @param key
     * @param value
     * @return
     */
    public ListResult ladd(String key, String value) throws NotFoundKeyException;

    /**
     * Add the string value to the trail of the liststored at key. If the key
     * does not exist return ListResult.LIST_NOTEXIST, if List length too long
     * return ListResult.List_limit, otherwise return LIST_OK
     * 
     * @param key
     * @param value
     * @return
     */
    public ListResult radd(String key, String value) throws NotFoundKeyException;

    /**
     * Return the specified elements of the list stored at the specified key.
     * Start and end are zero-based indexes. 0 is the first element of the list
     * (the list head), 1 the next element and so on. if the key does not exist
     * throws NotFoundKeyException;
     * 
     * @param key
     * @param beg
     * @param end
     * @return
     */
    public List<String> lrange(String key, int begIndex, int endIndex) throws NotFoundKeyException;

    /**
     * init List value; If the key exist, replace list of key, if the key don't
     * exist , set list value; if values length too longer , return
     * ListResult.List_limit
     * 
     * @param key
     * @param value
     * @return
     */
    public <T> ListResult setList(String key, List<T> value);

    /**
     * remove list of specify key from cache
     * 
     * @param key
     * @return
     */
    public ListResult removeList(String key) throws CacheException;  
    
    
    public Integer  lsize(String key) throws CacheException;
}
