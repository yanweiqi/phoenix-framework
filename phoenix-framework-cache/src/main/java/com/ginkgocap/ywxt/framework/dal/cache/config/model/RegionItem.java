package com.ginkgocap.ywxt.framework.dal.cache.config.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:22
 * @Copyright Copyright©2015 www.gintong.com
 */
public class RegionItem implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2427500931734139981L;
    private static Logger logger = Logger.getLogger(RegionItem.class);
    private String name;
    private String listenerClass;
    private String strategyClass;
    private boolean hasClassInfo = true;

    private int limitLen = 300;
    private int initLen = 100;
    
    private Map<String, KeyPatternItem> keyPatternMap = new HashMap<String, KeyPatternItem>();
    private boolean localCache;

    private boolean remoteCache = true;
    private boolean useCRC32 = false;
    
    
    
    
    
    public boolean isRemoteCache() {
		return remoteCache;
	}

	public void setRemoteCache(boolean remoteCache) {
		this.remoteCache = remoteCache;
	}

	public boolean isLocalCache() {
        return localCache;
    }

    public void setLocalCache(boolean localCache) {
        this.localCache = localCache;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListenerClass() {
        return listenerClass;
    }

    public void setListenerClass(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    public Map<String, KeyPatternItem> getKeyPatternMap() {
        return keyPatternMap;
    }

    public void addKeyPatternItem(KeyPatternItem item) {
        if (item != null && StringUtils.isNotEmpty(item.getValue())) {
            if (!keyPatternMap.containsKey(item.getValue())) {
                keyPatternMap.put(item.getValue(), item);
            } else {
                logger.error("region: " + StringUtils.defaultIfEmpty(name, "") + " same patternitem <"
                        + item.getValue() + "> exist!");
            }
        } else {
            logger.error("KeyPatternItem object is null or value is null, please set in memcached_client.xml.");
        }
    }
    
    public void resetKeyPatternItems(List<KeyPatternItem> items) {
    	if (CollectionUtils.isEmpty(items)) return;
    	
    	Map<String, KeyPatternItem> newKeyPs = new HashMap<String, KeyPatternItem>();
    	for(KeyPatternItem item : items) {
    		if (item != null && StringUtils.isNotEmpty(item.getValue())) {
                if (!newKeyPs.containsKey(item.getValue())) {
                	newKeyPs.put(item.getValue(), item);
                } else {
                    logger.error("region: " + StringUtils.defaultIfEmpty(name, "") + " same patternitem <"
                            + item.getValue() + "> exist!");
                }
            } else {
                logger.error("KeyPatternItem object is null or value is null, please set in memcached_client.xml.");
            }
    	}
    	
    	//replace
    	if (MapUtils.isNotEmpty(newKeyPs)) {
    		this.keyPatternMap = newKeyPs;
    	}
    }

    public String getStrategyClass() {
        return strategyClass;
    }

    public void setStrategyClass(String strategyClass) {
        this.strategyClass = strategyClass;
    }

    public boolean isHasClassInfo() {
        return hasClassInfo;
    }

    public void setHasClassInfo(boolean hasClassInfo) {
        this.hasClassInfo = hasClassInfo;
    }

    public int getLimitLen() {
        return limitLen;
    }

    public void setLimitLen(int limitLen) {
        this.limitLen = limitLen;
    }

    public int getInitLen() {
        return initLen;
    }

    public void setInitLen(int initLen) {
        this.initLen = initLen;
    }

    
    
    public boolean isUseCRC32() {
		return useCRC32;
	}

	public void setUseCRC32(boolean useCRC32) {
		this.useCRC32 = useCRC32;
	}

	@Override
	public String toString() {
		return "RegionItem [name=" + name + ", listenerClass=" + listenerClass + ", strategyClass=" + strategyClass + ", hasClassInfo=" + hasClassInfo + ", limitLen=" + limitLen
				+ ", initLen=" + initLen + ", keyPatternMap=" + keyPatternMap + ", localCache=" + localCache + ", remoteCache=" + remoteCache + ", useCRC32=" + useCRC32 + "]";
	}



}
