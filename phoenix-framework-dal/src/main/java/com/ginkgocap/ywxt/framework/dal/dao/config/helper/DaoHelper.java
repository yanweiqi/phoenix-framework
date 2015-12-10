/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import com.ginkgocap.ywxt.framework.dal.dao.config.DaoConfig;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.DbStrategyItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.IdCenterDsItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ListItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.MapItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ObjectItem;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:01
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class DaoHelper {
    private static Logger logger = Logger.getLogger(DaoHelper.class);

    /**
     * 给出持久化对象对应的配置
     * 
     * @param objectName
     * @return
     */
    public static ObjectItem getObjectItemByObjectName(String objectName) {
        if (StringUtils.isEmpty(objectName)) {
            return null;
        } else {
            return DaoConfig.getInstance().getObjectItem(objectName);
        }
    }

    /**
     * 
     * @param objectName
     * @return
     */
    public static Method getDelPropertyMethod(String objectName) {
        ObjectItem item = getObjectItemByObjectName(objectName);
        return item == null ? null: item.getDelPropertyMethod();
    }
    
    
    /**
     * 
     * @param objectName
     * @return
     */
    public static Object getDelValue(String objectName) {
        ObjectItem item = getObjectItemByObjectName(objectName);
        return item == null ? null: item.getDelValueObject();
    }
    
    /**
     * 给出缺省的DbStrategyItem
     * 
     * @return
     */
    public static DbStrategyItem getDefaultDbStrategyItem() {
        return DaoConfig.getInstance().getDefaultDbStrategyItem();
    }

    /**
     * 通过持久对象的名字找到对象的DB策略配置
     * 
     * @param objectName
     * @return DbStrategyItem
     */
    public static DbStrategyItem getDbStrategyItemByObjectName(String objectName) {
        if (StringUtils.isEmpty(objectName)) {
            return null;
        }
        ObjectItem objectItem = getObjectItemByObjectName(objectName);
        return objectItem == null ? null : objectItem.getDbStrategyItem() != null ? objectItem.getDbStrategyItem()
                : getDefaultDbStrategyItem();
    }

    /**
     * 通过持久对象的Class找到对象的DB策略配置
     * 
     * @param objectName
     * @return DbStrategyItem
     */
    public static DbStrategyItem getDbStrategyItemByClass(Class clazz) {
        if (clazz == null) {
            return null;
        }
        return getDbStrategyItemByObjectName(clazz.getName());
    }

    
    /**
     * 通过对象找到对象配置了几个数据源（组）
     * 
     * @param objectName
     * @return DbStrategyItem
     */
    public static int  getCountDaoGroupByClass(Class clazz) {
        DbStrategyItem dbStrategyItem = getDbStrategyItemByObjectName(clazz.getName());
        return dbStrategyItem == null ? 0 : dbStrategyItem.getPatternItemList().size();
    }
    
    /**
     * 通过ListName找到Db的策略配置
     * 
     * @param listName
     * @return
     */
    public static DbStrategyItem getDbStrategyItemByListName(String listName) {
        ListItem item = getListItemByListName(listName);
        return item == null ? null : getDbStrategyItemByObjectName(item.getObjectName());
    }

    /**
     * 通过List名字找到对象配置了几个数据源（组）
     * 
     * @param objectName
     * @return DbStrategyItem
     */
    public static int  getCountDaoGroupByListName(String listName) {
        DbStrategyItem dbStrategyItem = getDbStrategyItemByListName(listName);
        return dbStrategyItem == null ? 0 : dbStrategyItem.getPatternItemList().size();
    }
    /**
     * 通过mapName找到Db的策略配置
     * 
     * @param listName
     * @return
     */
    public static DbStrategyItem getDbStrategyItemByMapName(String mapName) {
        MapItem item = getMapItemByMapName(mapName);
        return item == null ? null : getDbStrategyItemByObjectName(item.getObjectName());
    }
    /**
     * 通过Maping名字找到对象配置了几个数据源（组）
     * 
     * @param objectName
     * @return DbStrategyItem
     */
    public static int  getCountDaoGroupByMapName(String mapName) {
        DbStrategyItem dbStrategyItem = getDbStrategyItemByMapName(mapName);
        return dbStrategyItem == null ? 0 : dbStrategyItem.getPatternItemList().size();
    }
    /**
     * 通过对象找到对象对应的策略依据属性的方法
     * 
     * @param objectName
     * @return
     */
    public static Method getStrategyPropertyMethod(String objectName) {
        if (StringUtils.isEmpty(objectName)) {
            return null;
        } else {
            ObjectItem objectItem = getObjectItemByObjectName(objectName);
            return objectItem == null ? null : objectItem.getStrategyPropertyMethod();
        }
    }

    /**
     * 找出一个对象相关联的List配置列表
     * 
     * @param objectName
     * @return
     */
    public static List< ListItem > getListItemsByObjectName(String objectName) {
        if (StringUtils.isEmpty(objectName)) {
            return null;
        }
        ObjectItem objectItem = DaoConfig.getInstance().getObjectItem(objectName);
        List< ListItem > list = null;
        if (objectItem != null && MapUtils.isNotEmpty(objectItem.getListMap())) {
            Map< String, ListItem > listItems = objectItem.getListMap();
            list = new ArrayList< ListItem >();
            for (Iterator< ListItem > iterator = listItems.values().iterator(); iterator.hasNext();) {
                ListItem listItem = (ListItem) iterator.next();
                list.add(listItem);
            }
        }
        return list;
    }

    /**
     * 通过一个ListName找到List的配置文件
     * 
     * @param listName
     * @return
     */
    public static ListItem getListItemByListName(String listName) {
        if (StringUtils.isEmpty(listName)) {
            return null;
        }
        return DaoConfig.getInstance().getListItem(listName);
    }
    /**
     * 通过一个ListName找到List的配置文件
     * 
     * @param listName
     * @return
     */
//    public static CrossListItem getCrossListItemByListName(String listName) {
//        if (StringUtils.isEmpty(listName)) {
//            return null;
//        }
//        return DaoConfig.getInstance().getCrossListItem(listName);
//    }
    
    public static String getObjectNameByListName(String listName) {
        ListItem item = getListItemByListName(listName);
        return item == null ? null : item.getObjectName();
    }

    /**
     * 给出List对应的KeyProperty方法；
     * 
     * @param listName
     * @return
     */
    public static Method[] getListItemKeyMethod(String listName) {
        if (StringUtils.isEmpty(listName)) {
            return null;
        }
        ListItem item = getListItemByListName(listName);
        if (item != null) {
            return item.getKeyMethod();
        } else {
            return null;
        }
    }

    /**
     * 给出List对应的valueProperty方法；
     * 
     * @param listName
     * @return
     */
    public static Method getListItemValueMethod(String listName) {
        if (StringUtils.isEmpty(listName)) {
            return null;
        }
        ListItem item = getListItemByListName(listName);
        return item == null ? null : item.getValueMethod();
    }

    /**
     * 找出一个对象相关联的List配置列表
     * 
     * @param objectName
     * @return
     */
    public static List< MapItem > getMapItemsByObjectName(String objectName) {
        if (StringUtils.isEmpty(objectName)) {
            return null;
        }
        ObjectItem objectItem = DaoConfig.getInstance().getObjectItem(objectName);
        List< MapItem > list = null;
        if (objectItem != null && MapUtils.isNotEmpty(objectItem.getMapMap())) {
            Map< String, MapItem > mapItems = objectItem.getMapMap();
            list = new ArrayList< MapItem >();
            for (Iterator< MapItem > iterator = mapItems.values().iterator(); iterator.hasNext();) {
                MapItem mapItem = (MapItem) iterator.next();
                list.add(mapItem);
            }
        }
        return list;
    }

    /**
     * 通过一个ListName找到List的配置文件
     * 
     * @param listName
     * @return
     */
    public static MapItem getMapItemByMapName(String mapName) {
        if (StringUtils.isEmpty(mapName)) {
            return null;
        }
        return DaoConfig.getInstance().getMapItem(mapName);
    }

    /**
     * 给出List对应的KeyProperty方法；
     * 
     * @param listName
     * @return
     */
    public static Method[] getMapItemKeyMethod(String mapName) {
        if (StringUtils.isEmpty(mapName)) {
            return null;
        }
        MapItem item = getMapItemByMapName(mapName);
        if (item != null) {
            return item.getKeyMethod();
        } else {
            return null;
        }
    }

    /**
     * 给出List对应的valueProperty方法；
     * 
     * @param mapName
     * @return
     */
    public static Method getMapItemValueMethod(String mapName) {
        if (StringUtils.isEmpty(mapName)) {
            return null;
        }
        MapItem item = getMapItemByMapName(mapName);
        return item == null ? null : item.getValueMethod();
    }

    /**
     * 
     * @return
     */
    public static IdCenterDsItem getIdCenterDsItem() {
        return DaoConfig.getInstance().getIdCenterDsItem();
    }

    /**
     * 
     * @return
     */
    public static String getIdCenterDsName() {
        IdCenterDsItem centerDsItem = DaoConfig.getInstance().getIdCenterDsItem();
        if (centerDsItem == null) {
            logger.error("Please set idCenter datasource in dao.xml");
            return null;

        } else {
            return centerDsItem.getDs();
        }
    }

    /**
     * 给出List中valueProperty对应的Class方法；
     * 
     * @param listName
     * @return
     */
    public static Class getListItemValueClass(String listName) {
        if (StringUtils.isEmpty(listName)) {
            return null;
        }
        ListItem item = getListItemByListName(listName);
        Method method = item == null ? null : item.getValueMethod();
        return method == null ? null : method.getReturnType();
    }

    /**
     * 给出Map中valueProperty对应的Class方法；
     * 
     * @param listName
     * @return
     */
    public static Class getMapItemValueClass(String mapName) {
        if (StringUtils.isEmpty(mapName)) {
            return null;
        }
        MapItem item = getMapItemByMapName(mapName);
        Method method = item == null ? null : item.getValueMethod();
        return method == null ? null : method.getReturnType();
    }

    /**
     * 给出List中valueProperty对应的字段名字；
     * 
     * @param listName
     * @return
     */
    public static String getListItemValueColumnName(String listName) {
        if (StringUtils.isEmpty(listName)) {
            return null;
        }
        ListItem item = getListItemByListName(listName);

        return item == null ? null : item.getColumnName();
    }

    /**
     * 给出Map中valueProperty对应的Class方法；
     * 
     * @param listName
     * @return
     */
    public static String getMapItemValueColumnName(String mapName) {
        if (StringUtils.isEmpty(mapName)) {
            return null;
        }
        MapItem item = getMapItemByMapName(mapName);
        return item == null ? null : item.getColumnName();
    }

    public static SessionFactory getSessionFactory() {
        return DaoConfig.getInstance().getSessionFactory();
    }

    /**
     * 给出对象的ID方法
     * 
     * @param clazz
     * @return
     */
    public static Method getIdMethod(Class clazz) {
        if (clazz == null)
            return null;
        ObjectItem objectItem = getObjectItemByObjectName(clazz.getName());
        return objectItem == null ? null : objectItem.getIdMethod();
    }
    
}
