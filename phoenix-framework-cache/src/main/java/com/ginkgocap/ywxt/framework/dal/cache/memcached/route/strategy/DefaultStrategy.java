package com.ginkgocap.ywxt.framework.dal.cache.memcached.route.strategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.config.model.KeyPatternItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.RegionItem;
import com.ginkgocap.ywxt.framework.dal.cache.exception.StrategyException;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.config.helper.CacheConfigHelper;
import com.ginkgocap.ywxt.framework.dal.cache.route.strategy.IStrategy;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:25
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DefaultStrategy implements IStrategy {
    private static Logger logger = Logger.getLogger(DefaultStrategy.class);
    private static ThreadLocal<CRC32> crc32Thread = new ThreadLocal<CRC32>();

    public Object playStrategy(String regionName, Object key) throws StrategyException {
        if (StringUtils.isEmpty(regionName)) {
            throw new StrategyException("region name must have a value");
        }

        if (key == null) {
            throw new StrategyException("key must have a value");
        }
        RegionItem regionItem = CacheConfigHelper.getRegionItem(regionName);
        Object temp_key = regionItem != null && regionItem.isUseCRC32() && NumberUtils.isDigits(String.valueOf(key)) ? getCRC32(key):key;

        Map<String, KeyPatternItem> keyPatternMap = CacheConfigHelper.getKeyKeyPatternItems(regionName);
        
        if (MapUtils.isEmpty(keyPatternMap)) { //如果找不到配置，就用缺省的
            logger.warn("don't find KeyPatternItems of the " + regionName + " , begin use default set!");            
            keyPatternMap = CacheConfigHelper.getDefaultKeyKeyPatternItems();
        }

        if (MapUtils.isNotEmpty(keyPatternMap)) {
            KeyPatternItem keyPatternItem = findPatternItem(keyPatternMap, temp_key);
            return keyPatternItem != null ? keyPatternItem.getDatasource() : null;
        }
        return null;
    }

    /**
     * 想找到想用的patternItem
     * 
     * @param patternMap
     * @param account
     * @return
     */
    private KeyPatternItem findPatternItem(Map<String, KeyPatternItem> patternMap, Object key) {
        KeyPatternItem patternItem = null;
        Collection<KeyPatternItem> patternItems = patternMap.values();
        for (Iterator iterator = patternItems.iterator(); iterator.hasNext();) {
            KeyPatternItem item = (KeyPatternItem) iterator.next();
            if (item != null) {
                String value = item.getValue();
                if (StringUtils.isNotEmpty(value)) {
                    Pattern pattern = Pattern.compile(value);
                    if (pattern.matcher(ObjectUtils.toString(key)).find()) { // 找到了就退出
                        patternItem = item;
                        break;
                    }
                }
            }
        }
        return patternItem != null ? patternItem : patternItems.iterator().next();
    }
    
    private static Object getCRC32(Object account) {
    	CRC32 crc32 = crc32Thread.get();
    	if (crc32 == null) {
    		crc32 = new CRC32();
    		crc32Thread.set(crc32);
    	} else {
    		crc32.reset();
    	}
		crc32.update(account.toString().getBytes());
		return crc32.getValue();
    }
}
