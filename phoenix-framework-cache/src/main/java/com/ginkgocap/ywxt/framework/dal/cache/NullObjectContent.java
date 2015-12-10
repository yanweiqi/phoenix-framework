package com.ginkgocap.ywxt.framework.dal.cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;


/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:15:35
 * @Copyright Copyright©2015 www.gintong.com
 */
public class NullObjectContent {
    private static Logger logger = Logger.getLogger(NullObjectContent.class);
    private static int INITIALCAPACITY = 1500;
    public static Map<String, Integer> content = Collections.synchronizedMap(new LRUMap(INITIALCAPACITY));
    public static Map<String, Integer> puting = new ConcurrentHashMap<String, Integer>(150);
    public static Map<String, Integer> getting = new ConcurrentHashMap<String, Integer>(150);
    private static Integer VALUE = Integer.valueOf(1);

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    //2009-0518 remove
    //private static Cache cache = CacheFactoryImpl.getInstance().getCache("global"); // 全局Cache
    private static Cache cache = null;
    
    public static NullObjectContent _this = new NullObjectContent();

    public static void setNull(String key) {
        content.put(key, VALUE);
        if (!puting.containsKey(key)) {
            try {
                puting.put(key, VALUE);
                RemotePut put = new RemotePut(key);
                executorService.execute(put);
            } finally {
                puting.remove(key);
            }
        }

    }

    public static boolean isNull(String key) {
        Boolean b = content.containsKey(key);
        if (!getting.containsKey(key)) {
            try {
                getting.put(key, VALUE);
                RemoteGet get = new RemoteGet(key);
                executorService.execute(get);
            } finally {
                getting.remove(key);
            }
        }
        return b;
    }

    public static void remove(String key) {
        try {
            content.remove(key);
            if (null != cache) {
                cache.remove(key);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("fix me");
                    logger.trace("Warn :Global Cache is null ");
                }
            }
        } catch (com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException e) {
            logger.error(e.getMessage());
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * 
     * 
     * @author allenshen date: Dec 30, 2008 5:05:20 PM Copyright 2008 Sohu.com
     *         Inc. All Rights Reserved.
     */
    private static class RemotePut implements Runnable {
        private String key;

        public RemotePut(String key) {
            this.key = key;
        }

        public void run() {
            // 从远程（Memcache）装载Content
            try {
                if (null != cache) {
                    cache.put(key, VALUE);
                } else {
                    logger.warn("Warn :Global Cache is null ");
                }
            } catch (CacheException e) {
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 
     * 
     * 
     * @author allenshen date: Dec 30, 2008 5:05:20 PM Copyright 2008 Sohu.com
     *         Inc. All Rights Reserved.
     */
    private static class RemoteGet implements Runnable {
        private String key;

        public RemoteGet(String key) {
            this.key = key;
        }

        public void run() {
            // 从远程（Memcache）装载Content
            try {
                if (cache == null) {
                	if (logger.isDebugEnabled()) {
                		logger.debug("global cache is null");
                	}
                    return;
                }
                Object o = cache.get(key);
                if (o == null) {
                    content.remove(key);
                } else {
                    content.put(key, VALUE);
                }
            } catch (CacheException e) {
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }

    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                executorService.shutdown();
            }
        });
    }
}
