package com.ginkgocap.ywxt.framework.dal.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;

/**
 * 存储最大对象ID
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:15:11
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class MaxIdHolder {
    private static Logger logger = Logger.getLogger(MaxIdHolder.class);
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    private static java.util.Map<String, Long> maxIdMap = new ConcurrentHashMap<String, Long>();
    private static Long DEFAULT_MAXID = Long.MAX_VALUE;
    // remove 2009-05-18
    // private static Cache cache =
    // CacheFactoryImpl.getInstance().getCache("global"); // 全局Cache

    private static Cache cache = null;

    private static final Long RANGE = Long.valueOf(500);
    public static Map<String, Integer> getting = new ConcurrentHashMap<String, Integer>(150);
    private static ExecutorService executorGetService = Executors.newFixedThreadPool(10);
    private static ThreadPoolExecutor executorSetService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    static {
        executorSetService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                RemoteSet remoteSet = (RemoteSet) r;
                if (remoteSet != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("set key ").append(remoteSet.getKey()).append(" value ").append(remoteSet.getValue())
                            .append(" fail.");
                    logger.error(sb.toString());
                }
            }
        });
    }
    private static Integer VALUE = Integer.valueOf(1);

    public static void setRegionName(String regionName) {
        contextHolder.set(regionName);
    }

    /**
     * 设置MaxID
     * 
     * @param max_key
     * @param maxId
     */
    public static void setMaxId(String max_key, Long maxId) {
        if (StringUtils.isNotEmpty(max_key)) {
            Long storeMaxId = getMaxId(max_key);
            if (null == storeMaxId || storeMaxId < maxId) {
                maxIdMap.put(max_key, maxId);
                RemoteSet remoteSet = new RemoteSet(max_key, maxId);
                executorSetService.execute(remoteSet);
            }
        }
    }

    /**
     * 
     * @param max_key
     * @return
     */
    public static Long getMaxId(String max_key) {
        Long maxId = null;
        if (StringUtils.isNotEmpty(max_key)) {
            maxId = maxIdMap.get(max_key);
            if (!getting.containsKey(max_key)) {
                try {
                    getting.put(max_key, VALUE);
                    RemoteGet get = new RemoteGet(max_key);
                    executorGetService.execute(get);
                } finally {
                    getting.remove(max_key);
                }
            }

        }
        return maxId == null ? DEFAULT_MAXID : maxId + RANGE;
    }

    public static void clear() {
        contextHolder.remove();
    }

    /**
     * 
     * 
     * 
     * @author allenshen date: Feb 2, 2009 10:46:31 AM Copyright 2008 Sohu.com
     *         Inc. All Rights Reserved.
     */
    private static class RemoteSet implements Runnable {
        private String key;
        private Object value;

        public RemoteSet(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public void run() {
            // 从远程（Memcache）装载Content
            try {
                if (null != cache && value != null) {
                    Long maxId = maxIdMap.get(key);
                    if ((Long) value >= maxId) {
                        cache.put(key, value);
                    }
                } else {
                    logger.warn("fix me");
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
     * @author allenshen date: Feb 2, 2009 10:46:27 AM Copyright 2008 Sohu.com
     *         Inc. All Rights Reserved.
     */
    private static class RemoteGet implements Runnable {
        private String key;

        // private java.util.Map<String, Long> maxIdMap;

        public RemoteGet(String key) {
            this.key = key;
            // this.maxIdMap = maxIdMap;
        }

        public void run() {
            // 从远程（Memcache）装载Content
            try {
                if (null != cache) {
                    Object value = cache.get(key);
                    if (value != null) {
                        Long vLong = (Long) value;
                        maxIdMap.put(key, vLong);
                    }
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("fix me");
                        logger.trace("Warn :Global Cache is null ");
                    }
                }
            } catch (CacheException e) {
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
