package com.ginkgocap.ywxt.framework.dal.dao.helper;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;

import com.ginkgocap.ywxt.framework.dal.dao.model.SqlInfo;
import com.ginkgocap.ywxt.framework.dal.dao.util.ObjectUtil;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:15
 * @Copyright Copyright©2015 www.gintong.com
 */
public class LogHelper {
    //====================Debug 级别的信息 ===========================================
    public static void debugSQL(Log log, String sql) {
        if (log.isDebugEnabled()) {
            log.debug("SQL -->" + sql);
        }
    }

    public static void debugSQL(Log log, SqlInfo sqlInfo, boolean bAll, Number start, Number count) {
//		log.warn("\r\n ===============Sql Info =============");
//		StringBuffer strBuf = new StringBuffer();
//		int paramCnt = 0;
//		if(null != sqlInfo.getParams() && sqlInfo.getParams().length >0){
//			paramCnt = sqlInfo.getParams().length;
//		}
//		strBuf.append(" \r\nsql --- >"+sqlInfo.getSql())
//		      .append("BAll -->"+bAll)
//		      .append(" start -->"+bAll)
//		      .append(" count -->"+count)
//		      .append(" params count -->"+paramCnt);
//		if(null != sqlInfo.getParams()){
//            for(Object obj:sqlInfo.getParams()){
//        	strBuf.append(" param -->"+obj);
//        }
//		}
//        log.warn(strBuf.toString()+"\r\n");
    }

    public static void debugSQL(Log log, SqlInfo sqlInfo) {
//		log.warn("\r\n ===============Entity Sql Info =============");
//		StringBuffer strBuf = new StringBuffer();
//		int paramCnt = 0;
//		if(null != sqlInfo.getParams() && sqlInfo.getParams().length >0){
//			paramCnt = sqlInfo.getParams().length;
//		}
//		strBuf.append(" \r\nsql --- >"+sqlInfo.getSql())
//		      .append(" params count -->"+paramCnt);
//		if(null != sqlInfo.getParams()){
//            for(Object obj:sqlInfo.getParams()){
//        	strBuf.append(" param -->"+obj);
//        }
//		}
//        log.warn(strBuf.toString()+"\r\n");
    }

    public static void runningStartAndCount(Log log) {
//		log.info(" \r\n =====Running start ,count");
    }

    public static void debugDataSource(Log log, String listName, Object account_id) {
//		log.info("Using Datasource === > list  name-->"+listName+"  account_id :"+account_id);
    }


    //=====================Info 级别的信息 =============================================

    public static void mappingParams(Log log, Object accountId, String mappingName, Object[] params) {
//		StringBuffer strBuf = new StringBuffer();
//		if(null != params && params.length >0){
//			for(Object obj: params){
//			  strBuf.append(" param -->"+obj);
//			}
//		}
//		log.warn("Attention: using getMapping  with accountId -->"+accountId+" ,mappingName --->"+mappingName+",params -->"+strBuf.toString());
    }

    public static void usingMappingWithCache(Log log) {
//		log.warn(" Attention : Using Mapping with cache \r\n" );
    }

    public static void usingMappingWithDB(Log log) {
//		log.info(" Attention : Using Mapping with DB \r\n" );
    }

    //=====================提醒性质的信息
    public static void objectHasBeenDeleted(Log log, Class clazz, Serializable id) {
        if (log.isInfoEnabled()) {
            log.info("Attention:The object of class:" + clazz.toString() + " with id -->" + id.toString() + " has been deleted");
        }
    }

    public static void cacheDontConfigured(Log log, String region) {
        if (log.isInfoEnabled()) {
            log.info("Warn : The cache for region " + region + " cann't found");
        }
    }

    //=====================提示性质的信息
    public static void saveObjectCausedANewListSection(Log log) {
        if (log.isInfoEnabled()) {
            log.info("Attention: The new saved id caused a new list section added");
        }
    }

    public static void listHasNotVisited(Log log, String region, String key) {
        log.warn("The List info :" + region + " with key :" + key + " doesn't exist in Cache or it's size is 0 \r\n");
    }

    //删除一笔记录时，导致缓存中的整个List重构
    public static void listReLoadWhenDeleting(Log log, String region, String key, Serializable id) {
        if (log.isInfoEnabled()) {
            log.info("The List :" + region + " with key :" + key + " is rebuilded  when deleting id :" + id);
        }
    }


    //缓存中的对象已经删除，不能重新加载
    public static void objectHasRemoved(Log log, String region, String key) {
        log.warn("Attention : The object in region :" + region + " with key :" + key + " has been removed \r\n");
    }

    // 聚合用的sql 参数为 null
    public static void parameterIsNullOfSql(Log log, String sql) {

    }

    // 根据id list从数据库中加载未在缓存中的数据库时，未从数据库中找到全部为加载 id
    public static void failedLoadObjectLsByIdLs(Log log, Class cls) {

    }

    //批量加载Map时，从数据库中没有加载到指定数量的Maps
    public static void failedLoadMapsLsByParams(Log log, String mappname, List<Object[]> paramsList) {

    }

    public static void failedGetClassNameByListName(Log log, String listName) {
        log.warn("Attention: Cant' get class name by list name :" + listName);
    }

    public static void failedGetObjectId(Log log, Object obj) {
        log.warn("Attention:The object's id is null \r\n");
    }

    public static void failedGetObjectById(Log log, Object accountId, Class cls, Serializable id) {
        log.warn("Failed load Object of Class -->" + cls.getName() + ",id -->" + id + ",account_id -->" + accountId + " from database");
    }


    //save时，装载跨库聚合List的最新一段Section失败
    public static void failedGetLastedSectonFormCacheForCross(Log log) {

    }


    //执行数据库查询返回null
    public static void failedExecuteQueryFromDB(Log log, SqlInfo sqlInfo) {

    }


    //update,delete
    public static void failedGetIdFromObject(Log log, Object obj) {
        log.warn("Warn : The  id of object :" + obj + " is null when update/delete cache \r\n");
    }


    //得到一个对象所对应的CrossAggrList的CrossAggrListItem 列表时，某一个元素为 null
    public static void nullCrossAggrListItem(Log log, Object obj) {
        log.warn("Attention : one CrossAggrListItem of class " + obj.getClass().getName() + " is null \r\n");
    }

    //某个Object的keyProperty为null
    public static void nullKeyPropertyOfObject(Log log, String region, Object obj) {
        log.warn("Warn: The params of obj " + obj + " for Region: " + region + " is null or empty \r\n");
    }


    //VisitHelper相关信息
    /**
     * 加载ListInfo的锁，已经在 save /update /delete 时，被删除了。
     * 出现这种情况的场景为: 缓存中的ListInfo对象为null,此时没有对象的 save/update/delete 操作，且没有其他人加载，
     * 用户在VisitHelper中的相应ConcurrentHashMap中进行加锁成功，并开始进行从数据库的加载动作，
     * 但当加载未完成时，发生了对象的 save/update/delete 动作，此时会先从VisitHelper中的
     * ConcurrentHashMap中删除之前设置的ListInfo的锁，因此当之前的ListInfo加载成功，用户准备删除
     * 之前设置的锁时，发现该锁已经被删除了
     */
    public static void listInfoLockHasBeenRemoved(Log log, String region, String key) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("LockWarn : The ListInfo Lock in VisitHelper.listInfoMaps with region: ")
                .append(region)
                .append(" key : ")
                .append(key)
                .append(" has been removed before by save|update|delete action ,so please check the listinfo in cache to avoid something wrong  ");
        log.warn(strBuf.toString());
    }

    public static void sectionLockHasBeenRemoved(Log log, String region, String key) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("LockWarn : The Section Lock in VisitHelper.idListMaps with region: ")
                .append(region)
                .append(" key : ")
                .append(key)
                .append(" has been removed before by save|update|delete action ,so please check the section infos in cache to avoid something wrong  ");
        log.warn(strBuf.toString());
    }


    //===============================Error 级别信息
    //save一个对象失败
    public static void savedError(Log log, Object obj) {
        log.warn("Warn :Failed to save object of Class " + obj.getClass());
    }

    //delete一个对象失败
    public static void deleteError(Log log, Class cls, Object account_id, Serializable id, boolean delType) {
        log.warn("Failed to delete Object with class :" + cls + "  account_id :" + account_id + " id:" + id + " del type :" + delType);
    }

    //update一个对象失败
    public static void updateError(Log log, Object obj) {
        try {
            log.error("Error: Failed to update class :" + obj.getClass().getName() + " with id:" + ObjectUtil.getObjectId(obj) + " in database \r\n");
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


}
