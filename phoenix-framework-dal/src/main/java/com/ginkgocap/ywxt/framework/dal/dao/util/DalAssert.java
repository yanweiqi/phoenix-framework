package com.ginkgocap.ywxt.framework.dal.dao.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.hibernate.SessionFactory;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.MapItem;
import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:18
 * @Copyright Copyright©2015 www.gintong.com
 */

public class DalAssert {

    /**
     * 用于判断数据的合法性 ,主要用于检查对象的策略id.
     * 
     * @param userId :
     *            策略id
     * @param obj :
     *            检查的对象
     */
    public static void assertObjectIllegal(Object userId, Object obj) throws DaoException {
        if (null != userId && null != obj) {
            Method method = DaoHelper.getStrategyPropertyMethod(obj.getClass().getName());
            if (null == method) {
                throw new DaoException(200, "Error: method doesn't found");
            }
            Object requiredId = null;
            try {
                requiredId = method.invoke(obj);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (null != requiredId && (!requiredId.equals(userId))) {
                throw new DaoException(DaoException.USERID_MATCH_ERROR,
                        "Error: The account_id doesn't match the object strategy property" +
                        "Detail: userId --"+userId+" obj's userId --"+requiredId+"\r\n");
            }
        }
    }

    public static void assertListIllegal(Object userId, List list) throws DaoException {
        if (null != userId && null != list && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                assertObjectIllegal(userId, list.get(i));
            }
        }
    }

    /**
     * 用于确定决定策略的Class 对象是否为空
     * 
     * @param cls
     *            :用于决定策略的Class类型
     * @throws DaoException
     */
    public static void assertClassTypeNotNull(Class cls) throws DaoException {
        if (null == cls) {
            throw new DaoException(DaoException.CLASS_TYPE_NULL);
        }
    }

    /**
     * 用于确定对象是否为NULL
     */
    public static void assertObjectNotNull(Object obj) throws DaoException {
        if (null == obj) {
            throw new DaoException(DaoException.OBJECT_NULL_EXCEPTION);
        }
    }
    public static void assertObjectNotNull(List  obs) throws DaoException {
        if (null == obs) {
            throw new DaoException(DaoException.OBJECT_NULL_EXCEPTION);
        }
        if( 0 == obs.size()){
        	throw new DaoException(DaoException.OBJECT_NULL_EXCEPTION);
        }
    	for(Object obj : obs){
            if (null == obj) {
                throw new DaoException(DaoException.OBJECT_NULL_EXCEPTION);
            }
    	}
    }

    public static void assertObjectNotNull(Object obj, String description) throws DaoException {
        if (null == obj) {
            throw new DaoException(DaoException.NULLPOINTER_EXCEPTION, description);
        }
    }

    /**
     * 用于确定SessionFactory是否为NULL
     */
    public static void assertSessionFactoryNotNull(SessionFactory sessionFactory) throws DaoException {
        if (null == sessionFactory) {
            throw new DaoException(DaoException.SessionFactory_NULL_EXCEPTION);
        }
    }

    /**
     * 用于确定dao.xml中配置的map元素是否出现异常
     * 
     * @throws DaoException
     */
    public static void assertDaoMapException(MapItem mapItem) throws DaoException {
        if (null == mapItem) {
            throw new DaoException(DaoException.MAP_NULLEXCEPTION);
        }
        if (null == mapItem.getSqlitem()) {
            throw new DaoException(DaoException.SQL_NULLEXCEPTION);
        }
    }
    /**
     * 用于检查，类似getMapList 这样的需求中，传入参数的有效性
     */
    public static void assertTwoListEquals(List list1 ,List list2) throws DaoException{
    	if(list1.size() != list2.size()){
    		throw new DaoException(DaoException.LIST_NOT_MATCH);
    	}
    }

}
