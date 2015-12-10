package com.ginkgocap.ywxt.framework.dal.cache.memcached.route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.exception.StrategyException;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.config.helper.CacheConfigHelper;
import com.ginkgocap.ywxt.framework.dal.cache.route.ContextHolder;
import com.ginkgocap.ywxt.framework.dal.cache.route.RoutingService;
import com.ginkgocap.ywxt.framework.dal.cache.route.strategy.IStrategy;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:13:33
 * @Copyright Copyright©2015 www.gintong.com
 */
public class RoutingServiceImpl implements RoutingService {
    private static Logger logger = Logger.getLogger(RoutingServiceImpl.class);
    private static Map<String, IStrategy> objectStrategy = new ConcurrentHashMap<String, IStrategy>();
    private static RoutingService routingService = null;

    public static RoutingService getInstance() {
        if (routingService == null) {
            synchronized (RoutingServiceImpl.class) {
                routingService = new RoutingServiceImpl();
            }
        }
        return routingService;
    }

    private RoutingServiceImpl() {

    }

    /* (non-Javadoc)
     * @see com.sohu.sns.dal.cache.route.RoutingService#setRoutingStrategy(java.lang.String, java.lang.Object)
     */
    public boolean setRoutingStrategy(String regionName, Object key) throws StrategyException {
        IStrategy strategy = objectStrategy.get(regionName);
        if (strategy == null) {
            // 根据配置判断对象是否有独立策略配置
            // 有：初始化并存储到Map
            // 无：把全局策略存储到Map
            String className = null;

            try {
                className = CacheConfigHelper.getStrategyClassName(regionName);
                if (StringUtils.isNotEmpty(className)) {
                    strategy = (IStrategy) Class.forName(className).newInstance();
                    objectStrategy.put(className, strategy);
                } else {
                    logger.error(StringUtils.defaultIfEmpty(regionName, "")
                            + " region strategyClass property must have value, please set in memcahced_client.xml");
                }

            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    e.printStackTrace(System.err);
                } else {
                    logger.error("don't instance " + StringUtils.defaultIfEmpty(className, ""));
                }
            }

        }

        if (strategy != null) {
            ContextHolder.setCachdName(String.valueOf(strategy.playStrategy(regionName, key)));
        }
        return false;
    }

}
