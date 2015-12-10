package com.ginkgocap.ywxt.framework.dal.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.client.CacheClient;
import com.ginkgocap.ywxt.framework.dal.cache.exception.CacheException;
import com.ginkgocap.ywxt.framework.dal.cache.route.ContextHolder;


/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:07
 * @Copyright Copyright©2015 www.gintong.com
 */
public class MultiThreadGet {

    Logger logger = Logger.getLogger(MultiThreadGet.class);

    private final static int BATCH_GET_NUMBER = 30;

    private final static long TIME_OUT = 10;

    private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime() .availableProcessors());

    public static MultiThreadGet multiGet;

    public static Object[] get(final CacheClient client, String[] keys, final String sourceName) throws CacheException{
        if (!ArrayUtils.isEmpty(keys) && keys.length > BATCH_GET_NUMBER) {
            Object[] lObjects = new Object[0];
            Map<String, Object[]> resMap = new HashMap<String, Object[]>();
            try {
                CompletionService completionService = new ExecutorCompletionService(executors);
                int i = 0;
                int index = 0;
                // 启动线程
                while (true) {
                    final String[] section_keys = (String[]) ArrayUtils.subarray(keys, index, index += BATCH_GET_NUMBER);
                    if (ArrayUtils.isEmpty(section_keys)) {
                        break;
                    } 
                    else {
                        i++;
                        final Integer queueNum = Integer.valueOf(i);
                        completionService.submit(new Callable() {
                            public Object call() {
                                Object[] fakeResult = new Object[section_keys.length];
                                Map<String, Object[]> map = new HashMap<String, Object[]>();
                                try {
                                    if (client.isDynamic()) {
                                        ContextHolder.setCachdName("null".equals(sourceName) ? null : sourceName);
                                    }
                                    Object[] results = client.getMultiArray(section_keys);
                                    map.put(queueNum.toString(), results);
                                } catch (Exception e) {
                                    map.put(queueNum.toString(), fakeResult);
                                }
                                return map;
                            }

                        });
                    }// else
                }
                for (int j = 0; j < i; j++) {
                    Future future = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                    if (future != null) {
                        Map<String, Object[]> map = (Map<String, Object[]>) future.get();
                        resMap.putAll(map);
                    } else {
                        throw new CacheException("poll timeout!");
                    }
                }
                for (int j = 1; j <= i; j++) {
                    lObjects = ArrayUtils.addAll(lObjects, resMap.get(j + ""));
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            return lObjects;
        } else {
            return client.getMultiArray(keys);
        }
    }
public static void main(String[] args) {
    System.out.println(Runtime.getRuntime()
    .availableProcessors());
}
}
