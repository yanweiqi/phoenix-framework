package com.ginkgocap.ywxt.framework.dal.dao.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ListItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.MapItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.method.ItemMethod;
import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:37
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ObjectUtil {

    private static final Log log = LogFactory.getLog(ObjectUtil.class);

    public static Object[] getParams(ItemMethod item, Object object) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Method[] methods = item.getKeyMethod();
        if (null == methods || methods.length < 1 || null == object) {
            // log.info("Warn---List/Map: The Class:"+object.getClass()+" 's key
            // property related methods is null or empty");
            return null;
        }
        Object[] params = new Object[methods.length];
//        StringBuffer keyBuffer = new StringBuffer();
        for (int i = 0; i < methods.length; i++) {// for1
            if (null != methods[i]) {
                Object propValue = methods[i].invoke(object);
                if (null == propValue) {
                    // log.error("Error--List/Map: Can't get property value of
                    // Class: "+object.getClass()+" by invoke
                    // Method:"+methods[i].getName());
                    return null;
                }
                params[i] = propValue;
            } else {
                // log.error("Error--List/Map: The Class
                // :"+object.getClass()+"'s one method for property getting or
                // setting is null or empty");
                return null;
            }
        }
        return params;
    }

    public static Object[] getOrderByParams(ListItem item, Object object) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Method[] methods = item.getOrderByMethod();
        if (null == methods || methods.length < 1 || null == object) {
            // log.info("Warn---List/Map: The Class:"+object.getClass()+" 's
            // orderBy property related methods is null or empty");
            return null;
        }
        Object[] params = new Object[methods.length];
//        StringBuffer keyBuffer = new StringBuffer();
        for (int i = 0; i < methods.length; i++) {// for1
            if (null != methods[i]) {
                Object propValue = methods[i].invoke(object);
                if (null == propValue) {
                    // log.error("Error--List/Map: Can't get property value of
                    // Class: "+object.getClass()+" by invoke
                    // orderByMethod:"+methods[i].getName());
                    return null;
                }
                params[i] = propValue;
            } else {
                // log.error("Error--List/Map: The Class
                // :"+object.getClass()+"'s one OrderBy method for property
                // getting or setting is null or empty");
                return null;
            }
        }
        return params;
    }

    public static Object getObjectValueOfList(Object obj, LsCacheInfo lsInfo) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        ListItem listItem = DaoHelper.getListItemByListName(lsInfo.getRegion());
        Method method = listItem.getValueMethod();
        if (null == method) {
            // log.info("Warn---List"+lsHelper.getRegion()+": The
            // Class:"+obj.getClass()+" 's value property related method is null
            // or empty");
            return null;
        }
        Object valueOfObj = method.invoke(obj);
        return valueOfObj;
    }

    public static Serializable getObjectId(Object object) throws Exception {
        Method method = DaoHelper.getIdMethod(object.getClass());
        if (null == method) {
            // log.error("Error: The Class :"+object.getClass().getName()+"'s
            // getId() method is null \r\n");
            return null;
        }
        return (Serializable) method.invoke(object);
    }

    public static List<MapInfo> getMapInfoList(Object object) throws Exception {
        List<MapInfo> list = new ArrayList<MapInfo>();
        List<MapItem> mapList = DaoHelper.getMapItemsByObjectName(object.getClass().getName());
        // 重要逻辑： 只有在对象中有相应Map元素的时候才进行处理。
        if (null != mapList && mapList.size() > 0) {
            for (MapItem item : mapList) {
                if (null == item) {
                    continue;
                }
                String region = item.getName();
                if (StringUtils.isBlank(region)) {
                    continue;
                }
                Object[] params = getParams(item, object);
                if (null == params || params.length < 1) {
                    // log.info("Attention: The params[keyProperty in dao.xml]
                    // for Region:"+region+" is null \r\n");
                    continue;
                }
                Method method = item.getValueMethod();
                if (null == method) {
                    log.info("Attention: Value Method in MapItem is null \r\n");
                    continue;
                }
                Object value = method.invoke(object);
                if (null == value) {
                    // log.info("Warn: The value of object's
                    // "+method.getName()+" for Region:"+region+" is null or
                    // empty \r\n");
                    continue;
                }
                list.add(new MapInfo(region, params, value));
            }
        }
        return list;
    }

 
    public static List<LsCacheInfo> getLsInfoList(Object object) throws Exception {
        List<LsCacheInfo> list = new ArrayList<LsCacheInfo>();
        List<ListItem> listItemList = DaoHelper.getListItemsByObjectName(object.getClass().getName());
        if (null != listItemList && listItemList.size() > 0) {
            for (ListItem item : listItemList) {
                if (null == item) {
                    // log.info("Attention : The ListItem of class
                    // "+object.getClass()+" is null \r\n");
                    continue;
                }
                // 得到List 所存放的Region 名字
                String region = item.getName();
                if (StringUtils.isEmpty(region)) {
                    // log.info("Attention: List name of class
                    // "+object.getClass()+"is empty ,check it on dao.xml
                    // \r\n");
                    continue;
                }
                // 如果得到<List>中由keyProperty 组成的在缓存中存放的key值不为空，则尝试更新缓存中的值
                Object[] params = getParams(item, object);
                if (null == params || params.length < 1) {
                    // log.info("Warn: The params for Region: "+region+" is null
                    // or empty \r\n");
                    continue;
                }
                // 如果得到<List>中由orderByProperty 组成的在缓存中存放的key值不为空，则尝试更新缓存中的值
                Object[] orderByParams = getOrderByParams(item, object);
                if (null == orderByParams || orderByParams.length < 1) {
                    // log.info("Warn: The orderByParams for Region: "+region+"
                    // is null or empty \r\n");
                }
                
                LsCacheInfo info = new LsCacheInfo(region, params, orderByParams,item.getLimitLen(),item.getInitLen(),item.isUpdate());
                list.add(info);
            }
        }
        return list;
    }


    public static Object getObjectStrategyPropert(Object obj) throws Exception {
        Method method = DaoHelper.getStrategyPropertyMethod(obj.getClass().getName());
        if (null == method) {
            return null;
        }
        return method.invoke(obj);
    }

}
