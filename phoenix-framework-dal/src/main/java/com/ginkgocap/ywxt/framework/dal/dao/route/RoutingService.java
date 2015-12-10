package com.ginkgocap.ywxt.framework.dal.dao.route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.DbStrategyItem;
import com.ginkgocap.ywxt.framework.dal.dao.datasource.ContextHolder;
import com.ginkgocap.ywxt.framework.dal.dao.exception.StrategyException;
import com.ginkgocap.ywxt.framework.dal.dao.route.strategy.IStrategy;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:49
 * @Copyright Copyright©2015 www.gintong.com
 */
public class RoutingService {
    private static Logger logger = Logger.getLogger(RoutingService.class);
    private static RoutingService routingService;
    private static Map<String, IStrategy> objectStrategy = new ConcurrentHashMap<String, IStrategy>();

    public static RoutingService getInstance() {
        if (routingService == null) {
            synchronized (RoutingService.class) {
            	if(routingService == null)
            		routingService = new RoutingService();
            }
        }
        return routingService;
    }

    private RoutingService() {
        // To do : init global strategy ...
    }

    public void setRoutingStrategyForObject(Class clazz, Object account, int rw) throws StrategyException {
        // To do : 根据Class决定配置属性
        IStrategy strategy = getStrategyForObject(clazz);
        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.ObjectShardingStrategy(clazz, account, rw)));
        } else {
            logger.error("don't find the " + ClassUtils.getShortClassName(clazz) + " dbStrategy");
        }
    }

    public void setRoutingStrategyForList(String listName, Object account, int rw) throws StrategyException {
        IStrategy strategy = getStrategyForList(listName);
        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.ListShardingStrategy(listName, account, rw)));
        } else {
            logger.error("don't find the " + ObjectUtils.toString(listName, "") + " dbStrategy");
        }
    }

    public void setRoutingStrategyForMap(String mapName, Object account, int rw) throws StrategyException {
        IStrategy strategy = getStrategyForMap(mapName);

        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.MapShardingStrategy(mapName, account, rw)));
        } else {
            logger.error("don't find the " + ObjectUtils.toString(mapName, "") + " dbStrategy");
        }
    }

    public void setRoutingStrategyForList(String listName, Object account, Object[] params, int rw)
            throws StrategyException {
        IStrategy strategy = getStrategyForList(listName);
        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.ListShardingStrategy(listName, account, params, rw)));
        } else {
            logger.error("don't find the " + ObjectUtils.toString(listName, "") + " dbStrategy");
        }
    }

    public void setRoutingStrategyForMap(String mapName, Object account, Object[] params, int rw)
            throws StrategyException {
        IStrategy strategy = getStrategyForMap(mapName);

        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.MapShardingStrategy(mapName, account, params, rw)));
        } else {
            logger.error("don't find the " + ObjectUtils.toString(mapName, "") + " dbStrategy");
        }

    }

    public void setRoutingStrategyForObject(Class clazz, Object account, Object params, int rw)
            throws StrategyException {
        IStrategy strategy = getStrategyForObject(clazz);
        if (strategy != null) {
            ContextHolder.setDataSource(String.valueOf(strategy.ObjectShardingStrategy(clazz, account, params, rw)));
        } else {
            logger.error("don't find the " + ClassUtils.getShortClassName(clazz) + " dbStrategy");
        }

    }

    /**
     * 是否读写分离
     * 
     * @param clazz
     * @param account
     * @return
     */
    public boolean isReadWrite(Class clazz, Object account) {
        // To do : 根据Class决定配置属性
        IStrategy strategy = objectStrategy.get(clazz.getClass().getName());
        if (strategy == null) {
            // 根据配置判断对象是否有独立策略配置
            // 有：初始化并存储到Map
            // 无：把全局策略存储到Map

            DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByClass(clazz);
            if (dbStrategyItem != null) {
                try {
                    String className = dbStrategyItem.getClasz();
                    if (StringUtils.isNotEmpty(className)) {
                        strategy = (IStrategy) Class.forName(dbStrategyItem.getClasz()).newInstance();
                        objectStrategy.put(clazz.getName(), strategy);
                    } else {
                        logger.error(StringUtils.defaultIfEmpty(dbStrategyItem.getName(), "")
                                + " stratey clazz property must have value, please set in dao.xml");
                    }

                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace(System.err);
                    } else {
                        logger.error("don't instance " + StringUtils.defaultIfEmpty(dbStrategyItem.getClasz(), ""));
                    }
                }
            }
        }
        if (strategy != null) {
            try {
                return strategy.isReadWrite(clazz, account);
            } catch (StrategyException e) {
                e.printStackTrace(System.err);
            }
        }
        return false;
    }

    /**
     * 用于遍历DataGroup
     * 
     * @param listName
     * @throws StrategyException
     */
    public boolean nextRoutingStrategyForList(String listName, int rw) throws StrategyException {
        IStrategy strategy = getStrategyForList(listName);
        if (strategy != null) {
            Object ds = strategy.NextListShardingStrategy(listName, rw);
            if (ds == null) {
                return false;
            } else {
                ContextHolder.setDataSource(String.valueOf(ds));
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {

        try {
            while (RoutingService.getInstance().nextRoutingStrategyForList("Photo_List_UserId_Id", 1)) {
                System.out.println("S");
            }

        } catch (StrategyException e) {
            e.printStackTrace();
        }
    }

    private IStrategy getStrategyForList(String listName) {
        IStrategy strategy = objectStrategy.get(listName);
        if (strategy == null) {
            // 根据配置判断对象是否有独立策略配置
            // 有：初始化并存储到Map
            // 无：把全局策略存储到Map
            DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByListName(listName);
            if (dbStrategyItem != null) {
                try {
                    String className = dbStrategyItem.getClasz();
                    if (StringUtils.isNotEmpty(className)) {
                        strategy = (IStrategy) Class.forName(className).newInstance();
                        objectStrategy.put(listName, strategy);
                    } else {
                        logger.error(StringUtils.defaultIfEmpty(dbStrategyItem.getName(), "")
                                + " stratey clazz property must have value, please set in dao.xml");
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace(System.err);
                    } else {
                        logger.error("don't instance " + StringUtils.defaultIfEmpty(dbStrategyItem.getClasz(), ""));
                    }
                }
            }
        }
        return strategy;
    }

    private IStrategy getStrategyForMap(String mapName) {
        IStrategy strategy = objectStrategy.get(mapName);
        if (strategy == null) {
            // 根据配置判断对象是否有独立策略配置
            // 有：初始化并存储到Map
            // 无：把全局策略存储到Map

            DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByMapName(mapName);
            if (dbStrategyItem != null) {
                try {
                    String className = dbStrategyItem.getClasz();
                    if (StringUtils.isNotEmpty(className)) {
                        strategy = (IStrategy) Class.forName(className).newInstance();
                        objectStrategy.put(mapName, strategy);
                    } else {
                        logger.error(StringUtils.defaultIfEmpty(dbStrategyItem.getName(), "")
                                + " stratey clazz property must have value, please set in dao.xml");
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace(System.err);
                    } else {
                        logger.error("don't instance " + StringUtils.defaultIfEmpty(dbStrategyItem.getClasz(), ""));
                    }
                }
            }
        }
        return strategy;
    }

    private IStrategy getStrategyForObject(Class clazz) {
        IStrategy strategy = objectStrategy.get(clazz.getClass().getName());
        if (strategy == null) {
            // 根据配置判断对象是否有独立策略配置
            // 有：初始化并存储到Map
            // 无：把全局策略存储到Map

            DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByClass(clazz);
            if (dbStrategyItem != null) {
                try {
                    String className = dbStrategyItem.getClasz();
                    if (StringUtils.isNotEmpty(className)) {
                        strategy = (IStrategy) Class.forName(dbStrategyItem.getClasz()).newInstance();
                        objectStrategy.put(clazz.getName(), strategy);
                    } else {
                        logger.error(StringUtils.defaultIfEmpty(dbStrategyItem.getName(), "")
                                + " stratey clazz property must have value, please set in dao.xml");
                    }

                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace(System.err);
                    } else {
                        logger.error("don't instance " + StringUtils.defaultIfEmpty(dbStrategyItem.getClasz(), ""));
                    }
                }
            }
        }
        return strategy;
    }
}
