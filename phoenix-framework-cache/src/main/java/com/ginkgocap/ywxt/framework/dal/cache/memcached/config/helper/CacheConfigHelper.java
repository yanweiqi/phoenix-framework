package com.ginkgocap.ywxt.framework.dal.cache.memcached.config.helper;

import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.config.model.KeyPatternItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.QueueItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.RegionItem;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.config.XCacheConfig;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.MemcachedClientProvider;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:42
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class CacheConfigHelper {
    private static Logger logger = Logger.getLogger(CacheConfigHelper.class);


    
    /**
     * 给出Region对应的策略表达式Map
     * 
     * @param regionName
     * @return
     */
    public static RegionItem getRegionItem(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            if (regionItem == null) {
                logger.error("don't find the " + regionName + " RegionItem in " + XCacheConfig.CONFIG_FILE);
                return null;
            }
            return regionItem;
        } else {
            logger.error("region name must have a value");
        }
        return null;
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * 给出Region对应的策略表达式Map
     * 
     * @param regionName
     * @return
     */
    public static Map<String, KeyPatternItem> getKeyKeyPatternItems(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            if (regionItem == null) {
                logger.error("don't find the " + regionName + " RegionItem in " + XCacheConfig.CONFIG_FILE);
                return null;
            }
            return regionItem.getKeyPatternMap();
        } else {
            logger.error("region name must have a value");

        }
        return null;
    }

    /**
     * 给出Region对应的策略表达式Map
     * 
     * @param regionName
     * @return
     */
    public static Map<String, KeyPatternItem> getDefaultKeyKeyPatternItems() {
        RegionItem regionItem = XCacheConfig.getInstance().getDefaultRegionItem();
        if (regionItem == null) {
            logger.error("cannot find the default RegionItem in " + XCacheConfig.CONFIG_FILE);
            return null;
        }
        return regionItem.getKeyPatternMap();
    }

    /**
     * 通过名字给出MemcachedSource
     * 
     * @param name
     * @return
     */
    public static MemcachedClientProvider getMemcachedClientProvider(String name) {
        return StringUtils.isNotEmpty(name) ? XCacheConfig.getInstance().getMemcachedProvider(name) : null;
    }

    /**
     * 
     * 
     * @param name
     * @return
     */
    public static String getQueueSourceName(String queueName) {
        if (StringUtils.isBlank(queueName))
            return null;
        QueueItem item = XCacheConfig.getInstance().getQueueItem(queueName);
        return item != null ? item.getDatasource() : null;
    }

    
    
    public static MemcachedClientProvider getDefaultQueueMemcachedSource() {
        QueueItem item = XCacheConfig.getInstance().getQueueItem("default");
        return item == null ? null: getMemcachedClientProvider(item.getDatasource());
    }
    
    /**
     * 
     */
    /**
     * 通过名字给出MemcachedSource
     * 
     * @param name
     * @return
     */
    public static MemcachedClientProvider getDynamicMemcachedClientProvider() {
        return XCacheConfig.getInstance().getDynamicMemcachedClientProvider();
    }

    public static String getStrategyClassName(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            if (regionItem != null) {
                return regionItem.getStrategyClass();
            } else {
                logger.error("don't find regionName config: " + regionName);
                return null;
            }
        } else {
            logger.error("regionName is null or empty");
            return null;
        }
    }

    /**
     * 
     * @param regionName
     * @return
     */
    public static String getListenerClass(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            if (regionItem != null) {
                return regionItem.getListenerClass();
            } else {
                logger.error("don't find regionName config: " + regionName);
                return null;
            }
        } else {
            logger.error("regionName is null or empty");
            return null;
        }
    }

    /**
     * 
     * @param regionName
     * @return
     */
    public static int getListLimitLen(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            if (regionItem != null) {
                return regionItem.getLimitLen();
            } else {
                logger.error("don't find regionName config: " + regionName);
                return 0;
            }
        } else {
            logger.error("regionName is null or empty");
            return 0;
        }
    }    
    /**
     * 检查一个Region是否在配置文件中存在
     * 
     * @param regionName
     * @return
     */
    public static boolean isExistRegion(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            return regionItem == null ? false : true;
        }
        return false;
    }

    /**
     * 检查一个Region是否支持本地缓存
     * 
     * @param regionName
     * @return
     */
    public static boolean isLocalCache(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            return regionItem == null ? false : regionItem.isLocalCache();
        }
        return false;
    }

    /**
     * 检查一个Region是否支持远程缓存
     * 
     * @param regionName
     * @return
     */
    public static boolean isRemoteCache(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            return regionItem == null ? false : regionItem.isRemoteCache();
        }
        return false;
    }    
    /**
     * 检查一个Region是否支持系列化类消息
     * 
     * @param regionName
     * @return
     */
    public static boolean isHasClassInfo(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            RegionItem regionItem = XCacheConfig.getInstance().getRegionItem(regionName);
            return regionItem == null ? true : regionItem.isHasClassInfo();
        }
        return true;
    }
}
