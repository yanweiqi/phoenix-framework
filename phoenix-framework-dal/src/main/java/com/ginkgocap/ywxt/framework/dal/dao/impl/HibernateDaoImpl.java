package com.ginkgocap.ywxt.framework.dal.dao.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;

import com.ginkgocap.ywxt.framework.dal.dao.DBAgent;
import com.ginkgocap.ywxt.framework.dal.dao.Dao;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ObjectItem;
import com.ginkgocap.ywxt.framework.dal.dao.datasource.ContextHolder;
import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;
import com.ginkgocap.ywxt.framework.dal.dao.helper.LogHelper;
import com.ginkgocap.ywxt.framework.dal.dao.model.SqlInfo;
import com.ginkgocap.ywxt.framework.dal.dao.route.RoutingService;
import com.ginkgocap.ywxt.framework.dal.dao.route.strategy.IStrategy;
import com.ginkgocap.ywxt.framework.dal.dao.util.DalAssert;
import com.ginkgocap.ywxt.framework.dal.dao.util.ObjectUtil;
import com.ginkgocap.ywxt.framework.dal.dao.util.SqlUtil;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:55
 * @Copyright Copyright©2015 www.gintong.com
 */
public class HibernateDaoImpl implements Dao {


	private static final int singleUsedTime = 15;
	
	private static final int lotsUsedTime = 50;
	
	private Log log = LogFactory.getLog(CompositeDaoImpl.class);
	
	private Log permLog = LogFactory.getLog("DalPerformance");
	
	private DBAgent dbAgent;
	
	public HibernateDaoImpl(){
		  dbAgent = DBAgentHibernateImpl.getInstance();
	}
    /**
     * ======================== Save ===========================================
     */
    public Serializable save(Object account_id, Object object) throws DaoException {
		DalAssert.assertObjectNotNull(object);
		DalAssert.assertObjectIllegal(account_id, object);
		
		long t1 = System.currentTimeMillis();
		
		Serializable id = null;
		try{
			RoutingService.getInstance().setRoutingStrategyForObject(object.getClass(), account_id, null,IStrategy.STRATEGY_W);
			id = dbAgent.save(account_id, object);
			if (null == id) {
				LogHelper.savedError(log, object);
			}
		}
		catch(Exception e){
			log.error(e.getMessage(), e);
			e.printStackTrace(System.out);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: save region: "+object.getClass().getName()+" params: "+id+" time: "+usedTime);
			}
		}
        return id;
    }
    public Serializable save(Object object) throws DaoException {
        return save(null,object);
    }
    public List save(Object account_id, List objects) throws DaoException {
		DalAssert.assertObjectNotNull(objects);
		DalAssert.assertListIllegal(account_id, objects);
		
		List objList = new ArrayList();
		for (Object obj : objects) {
			if (obj != null) {
				save(account_id, obj);
				objList.add(obj);
			}
		}
		return objList;
    }
    public List save(List objects) throws DaoException {
        return  save(null,objects);
    }
    
    /**
     * ======================= Batch Save ======================================
     */
    
    public List batchSave(Object account_id, List  objects) throws DaoException {
		DalAssert.assertObjectNotNull(objects);
		
		List objList = new ArrayList();
		
		try{
		  Map<Serializable,Object> objMap = batchSaveReturnMap(account_id, objects);
		  if(null != objMap && objMap.size() >0){
			  Iterator<Entry<Serializable, Object>> iter = objMap.entrySet().iterator();
			  while(iter.hasNext()){
				  Entry<Serializable, Object> entry = iter.next();
				  Serializable id = entry.getKey();
				  if(null != id ){
				   objList.add(objMap.get(id));
				 }
			  }
		  }
		}
		catch(Exception e){
			e.printStackTrace(System.out);
		}
		
        return objList;
    }

    public List batchSave(List  objects) throws DaoException {
    	return batchSave(null , objects);
    }
    
    public Map<Serializable,Object> batchSaveReturnMap(Object account_id, List objects) throws DaoException {    
    	long t1 = System.currentTimeMillis();
    	
    	Map<Serializable,Object> idObjMap = null;
    	   	
    	try{  		  	  
    	  RoutingService.getInstance().setRoutingStrategyForObject(objects.get(0).getClass(), account_id, null,IStrategy.STRATEGY_W);
    	  
    	  idObjMap = dbAgent.save(account_id, objects);	
    	}
    	catch(Exception e){
    	  processException(e);
    	}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= lotsUsedTime){
			if(permLog.isWarnEnabled()){
				Object obj = objects.get(0);
				if(null != obj && null != obj.getClass()){
		    	 permLog.warn("method: BatchSave region: "+objects.get(0).getClass().getName()+" size: "+objects.size()+" time: "+usedTime);
				}
			}
		}
		if(null == idObjMap){
			idObjMap = new HashMap<Serializable,Object>();
		}
		return idObjMap;
    }

   /**
    *  ======================= update ======================================
    */  
    public boolean update(Object account_id, Object object) throws DaoException {
		DalAssert.assertObjectNotNull(object);
		DalAssert.assertObjectIllegal(account_id, object);
		
		long t1 = System.currentTimeMillis();
		
		Serializable objectId =null;
		boolean bUpdated = true;
		
		try{
			objectId = ObjectUtil.getObjectId(object);
			if (null == objectId) {
				LogHelper.failedGetObjectId(log,object);
				return bUpdated;
			}
//			object = get(account_id, object.getClass(), objectId);
//			if (null == object) {
//				LogHelper.failedGetObjectById(log,account_id, object.getClass(), objectId);
//				return bUpdated;
//			}
	    	RoutingService.getInstance().setRoutingStrategyForObject(object.getClass(), account_id, null,IStrategy.STRATEGY_W);
	    	  
			bUpdated = dbAgent.update(account_id, object);
			if(!bUpdated){
				LogHelper.updateError(log, object);
			}
		}
		catch(Exception e){
			bUpdated = false;
			processException(e);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: update region: "+object.getClass().getName()+" params: "+objectId+" time: "+usedTime);
			}
		}	
        return bUpdated;
    }
    
    public boolean update(Object object) throws DaoException {
    	return update(null,object);
    }
	
    /**
     *  ======================= batch update ======================================
     */  	
    
    public boolean batchUpdate(Object account_id, List objects) throws DaoException {
		DalAssert.assertObjectNotNull(objects);
		DalAssert.assertListIllegal(account_id, objects);
		
		boolean bUpdated = true;
		try {
			for (Object obj : objects) {
				update(account_id, obj);
			}
		} 
		catch (Exception e) {
			bUpdated = false;
			processException(e);
		}
		return bUpdated;
    }
    
    public boolean batchUpdate(List objects) throws DaoException {
    	return batchUpdate(null,objects);
    }
    
    /**
     *  ======================= delete ======================================
     */  
    
    public boolean delete(Object account_id, Class clazz, Serializable id) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(id);
		
		long t1 = System.currentTimeMillis();
		
		boolean bDeleted = true;
		Object obj = null;
		
		try{
			obj = get(account_id, clazz, id);
			if(null == obj){
				LogHelper.failedGetObjectById(log,account_id, clazz, id);
				return bDeleted;
			}
			RoutingService.getInstance().setRoutingStrategyForObject(clazz, account_id,null,IStrategy.STRATEGY_W);
			bDeleted = dbAgent.delete(account_id, obj);
		}
		catch(Exception e){
			bDeleted = false;
			processException(e);
		}	
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: realDelete region: "+clazz.getName()+" params: "+id+" time: "+usedTime);
			}
		}
        return bDeleted;
    }
    
    public boolean delete(Class clazz, Serializable id) throws DaoException {
        return delete(null,clazz,id);
    }
    
    /**
     *  ======================= delete List ======================================
     */  
    
    public boolean deleteList(Object account_id, Class clazz, List ids) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(ids);
		
		boolean bDeleted = true;
		try {
			for (Object id : ids) {
				delete(account_id, clazz, (Serializable) id);
			}
		} catch (Exception e) {
			bDeleted = false;
			processException(e);
		}
		return bDeleted;
    }
    public boolean deleteList(Class clazz, List ids) throws DaoException {
        return deleteList(null, clazz, ids);
    }
    
	public boolean deleteList(Object account_id, String list_name,Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(list_name);
		DalAssert.assertObjectNotNull(params);
		
		boolean res = true;
		try {
			List idList = getIdList(account_id, list_name, params);
			if (null != idList && idList.size() > 0) {
				String clsName = DaoHelper.getObjectNameByListName(list_name);
				if(StringUtils.isEmpty(clsName)){
					LogHelper.failedGetClassNameByListName(log, list_name);
					return true;
				}
            	res = deleteList(account_id, Class.forName(clsName),idList);
			}
		} catch (Exception e) {
			processException(e);
		}
		return res;
	}
	public boolean deleteList(String list_name, Object[] params) throws DaoException {
		return deleteList(null, list_name, params);
	}
	public boolean deleteList(Object account_id, String list_name, Object param) throws DaoException {
		if (null == param) {
			return deleteList(account_id, list_name, new Object[] {});
		} else {
			return deleteList(account_id, list_name, new Object[] { param });
		}
	}
	public boolean deleteList(String list_name, Object param) throws DaoException {
		return deleteList(null, list_name, param);
	}


    /**
     *  =======================fake delete ======================================
     */     

    
    public boolean fakeDelete(Object account_id, Class clazz, Serializable id) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(id);
		
		long t1 = System.currentTimeMillis();
		
		boolean bDeleted = true;
		Object obj = null;
		
		try{   	 
			 obj = get(account_id, clazz, id);
			 if(null == obj){
				LogHelper.failedGetObjectById(log,account_id, clazz, id);
				return bDeleted;
		     } 	     
	  	     RoutingService.getInstance().setRoutingStrategyForObject(clazz, account_id, null, IStrategy.STRATEGY_W);
	  	     
			 Method method = DaoHelper.getDelPropertyMethod(obj.getClass().getName());
			 Object delTag = DaoHelper.getDelValue(obj.getClass().getName());
			 
			 DalAssert.assertObjectNotNull(method);
			 DalAssert.assertObjectNotNull(delTag);
			 
			 method.invoke(obj, delTag);	
			 dbAgent.update(account_id, obj);
		}
		catch(Exception e){
			bDeleted = false;
			processException(e);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	  permLog.warn("method: fakeDelete region: "+obj.getClass().getName()+" params: "+id+" time: "+usedTime);
			}
		 }
		
        return bDeleted;
    }

    
    public boolean fakeDelete(Class clazz, Serializable id) throws DaoException {
        return fakeDelete(null,clazz, id);
    }

    
    /**
     *  ======================= fakedelete List ======================================
     */   
    
    public boolean fakeDeleteList(Object account_id, Class clazz, List ids) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(ids);
		
		boolean bDeleted = true;
		try {
			for (Object id : ids) {
				fakeDelete(account_id, clazz, (Serializable) id);
			}
		} catch (Exception e) {
			bDeleted = false;
			processException(e);
		}
		return bDeleted;
    }   
    
    public boolean fakeDeleteList(Class clazz, List ids) throws DaoException {
        return fakeDeleteList(null, clazz, ids);
    }
    
    
	public boolean fakeDeleteList(Object account_id, String list_name,Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(list_name);
		DalAssert.assertObjectNotNull(params);
		
		boolean res = true;
		try {
			List idList = getIdList(account_id, list_name, params);
			if (null != idList && idList.size() > 0) {
				String clsName = DaoHelper.getObjectNameByListName(list_name);
				if(StringUtils.isEmpty(clsName)){
					LogHelper.failedGetClassNameByListName(log, list_name);
					return true;
				}
				res = fakeDeleteList(account_id, Class.forName(clsName),idList);
			}
		} 
		catch (Exception e) {
			processException(e);
		}
		return res;
	}

	public boolean fakeDeleteList(String list_name, Object[] params) throws DaoException {
		return fakeDeleteList(null, list_name, params);
	}

	public boolean fakeDeleteList(Object account_id, String list_name,Object param) throws DaoException {
		if (null == param) {
			return fakeDeleteList(account_id, list_name, new Object[] {});
		} else {
			return fakeDeleteList(account_id, list_name, new Object[] { param });
		}
	}

	public boolean fakeDeleteList(String list_name, Object param)throws DaoException {
		return fakeDeleteList(null, list_name, param);
	}
    
    
    /**
     *  ======================= count ======================================
     */     
	public int count(Object account_id, String listName, Object[] params)throws DaoException {
		DalAssert.assertObjectNotNull(listName);
		DalAssert.assertObjectNotNull(params);
		long t1 = System.currentTimeMillis();
		
		SqlInfo sqlInfo = null;
		int count = 0;
		
		try {
			sqlInfo = SqlUtil.getListCountSql(listName, params);
			
			RoutingService.getInstance().setRoutingStrategyForList(listName, account_id, params,IStrategy.STRATEGY_R);
			
			count = dbAgent.count(account_id, listName, sqlInfo);
		}
		catch (Exception e) {
			processException(e);
		}
		
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
				if(null != sqlInfo){
		      	  permLog.warn("method: count region: "+listName+" params: "+sqlInfo.getParamKey()+" time: "+usedTime);
				}
			}
		}
     	return count;
	}
	
	public int count(String list_name, Object[] params) throws DaoException {
		return count(null, list_name, params);
	}
	
	public int count(Object account_id, String list_name, Object param)
			throws DaoException {
		if (null == param) {
			return count(account_id, list_name, new Object[] {});
		} else {
			return count(account_id, list_name, new Object[] { param });
		}
	}
	public int count(String list_name, Object param) throws DaoException {
		return count(null, list_name, param);
	}
    
    /**
     *  ======================= get ======================================
     */      

    public Object get(Object account_id, Class clazz, Serializable id) throws DaoException {
		DalAssert.assertClassTypeNotNull(clazz);
		DalAssert.assertObjectNotNull(id);
		long t1 = System.currentTimeMillis();
		
		Object obj = null;
		
		try{
			RoutingService.getInstance().setRoutingStrategyForObject(clazz, account_id, id, IStrategy.STRATEGY_R);
			
			obj = dbAgent.get(account_id, clazz, id);
			//数据的合法性 验证 ，Just For More safety ^_^
			DalAssert.assertObjectIllegal(account_id, obj);
		}
		catch(Exception e){
			processException(e);
		}
		
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: get region: "+clazz.getName()+" params: "+id+" time: "+usedTime);
			}
		}
		
        return obj;
    }
    public Object get(Class clazz, Serializable id) throws DaoException {
         return get(null,clazz,id);
    }

    /**
     *  =======================  getIdList  ======================================
     */  
    
	public List getIdList(Object account_id, String listName, Object[] params,Integer start, Integer count) throws DaoException {	
		DalAssert.assertObjectNotNull(listName);
		DalAssert.assertObjectNotNull(params);
		DalAssert.assertObjectNotNull(start);
		DalAssert.assertObjectNotNull(count);
        long t1 = System.currentTimeMillis();
        
		List idList = new ArrayList();
		SqlInfo sqlInfo = null;
	
		try {
			sqlInfo = SqlUtil.getListSql(listName, params);
			if(null != sqlInfo){
				RoutingService.getInstance().setRoutingStrategyForList(listName, account_id, params, IStrategy.STRATEGY_R);
				idList = dbAgent.getIdList(account_id, listName, sqlInfo, start, count);
			}
		} 
		catch (Exception e) {
			processException(e);
		} 
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
				if(permLog.isWarnEnabled()){
				    if(null != sqlInfo.getParams()){
			     	 permLog.warn("method: getIdList region: "+listName+" params: "+sqlInfo.getParamKey()+" time: "+usedTime +" start :"+start+" count :"+count+"\r\n"+" sql: "+sqlInfo.getSql());
				    }
				}
	    }
		
		return idList;
	}

	public List getIdList(Object account_id, String list_name, Object[] params) throws DaoException {
		int totalCount = count(account_id, list_name, params);
		if (totalCount <= 0) {
			return new ArrayList();
		}
		return getIdList(account_id, list_name, params, 0, totalCount);
	}

	public List getIdList(String list_name, Object[] params)throws DaoException {
		return getIdList(null, list_name, params);
	}


	public List getIdList(Object account_id, String list_name, Object param) throws DaoException {
		if (null == param) {
			return getIdList(account_id, list_name, new Object[] {});
		} else {
			return getIdList(account_id, list_name, new Object[] { param });
		}
	}
	
	public List getIdList(String list_name, Object param)throws DaoException {
		return getIdList(null, list_name, param);
	}
	
	public List getIdList(String list_name, Object[] params, Integer start,Integer count) throws DaoException {
		return getIdList(null, list_name, params, start, count);
	}
	
    public List getIdList(Object account_id, String list_name, Object param,Integer start, Integer count) throws DaoException {
		if (null == param) {
			return getIdList(account_id, list_name, new Object[] {}, start,count);
		} else {
			return getIdList(account_id, list_name, new Object[] { param },start, count);
		}
	}

	public List getIdList(String list_name, Object param, Integer start,Integer count) throws DaoException {
		return getIdList(null, list_name, param, start, count);
	}
	
	public List getIdList(Object accountId, String mapName,	List<Object[]> paramsList) throws DaoException {
        return getMappings(accountId, mapName, paramsList);
	}
	public List getIdList(String mapName, List<Object[]> paramsList)	throws DaoException {
        return getIdList(null, mapName, paramsList);
	}
    
    /**
     *  =======================  getList  ======================================
     */     
	
    public List getList(Object account_id, Class clazz, List ids) throws DaoException {
		DalAssert.assertClassTypeNotNull(clazz);
		DalAssert.assertObjectNotNull(ids);
		long t1 = System.currentTimeMillis();
		
		List objList = new ArrayList();
		
		try{
			ObjectItem objItem =DaoHelper.getObjectItemByObjectName(clazz.getName());
			String baseSql = objItem.getEntitySql();
			if(null == baseSql || StringUtils.isEmpty(baseSql)){
				return objList;
			}
			List<SqlInfo>  sqlInfoLs = SqlUtil.getEntityUnionSql(ids, baseSql);
			if(null == sqlInfoLs || 0 == sqlInfoLs.size()){
				return objList;
			}
			
			RoutingService.getInstance().setRoutingStrategyForObject(clazz, account_id, ids.get(0), IStrategy.STRATEGY_R);
			
			for(SqlInfo sqlInfo : sqlInfoLs){
				List entityLs = dbAgent.getEntityList(account_id, clazz, sqlInfo,IStrategy.STRATEGY_R);
			    objList.addAll(entityLs);
			}
			return objList;	
		}
		catch(Exception e){
			processException(e);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: getList region: "+clazz.getName()+" size: "+ids.size()+" time: "+usedTime);
			}
	    }
        return objList;
    }
    
    public List getList(Class clazz, List ids) throws DaoException {
        return getList(null, clazz, ids);
    }

    /**
     *  =======================  getMapping  ======================================
     */     
     
	public Object getMapping(Object account_id, String mappingName,Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(mappingName);
		DalAssert.assertObjectNotNull(params);
		
		long t1 = System.currentTimeMillis();
		Object obj = null;
		
		try{
			SqlInfo sqlInfo = SqlUtil.getMappingSqlInfo(mappingName, params,false);

            RoutingService.getInstance().setRoutingStrategyForMap(mappingName, account_id, params,IStrategy.STRATEGY_R);
			
			obj = dbAgent.getMapping(account_id, mappingName, sqlInfo);
		}
		catch(Exception e){
			processException(e);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: getMapping region: "+mappingName+" size: "+params.length+" time: "+usedTime);
			}
		}
		
		return obj;
	}

	public Object getMapping(String mappingName, Object[] keys)
			throws DaoException {
		return getMapping(null, mappingName, keys);
	}

	public Object getMapping(Object account_id, String mappingName, Object key)throws DaoException {
		if (null == key) {
			return getMapping(account_id, mappingName, new Object[] {});
		} else {
			return getMapping(account_id, mappingName, new Object[] { key });
		}
	}

	public Object getMapping(String mappingName, Object key)throws DaoException {
		return getMapping(null, mappingName, key);
	}
    
   
	public Object getExtendMapping(Object account_id, String mappingName,Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(mappingName);
		DalAssert.assertObjectNotNull(params);
		
		long t1 = System.currentTimeMillis();
		Object obj = null;
		
		try{
			SqlInfo sqlInfo = SqlUtil.getMappingSqlInfo(mappingName, params,true);

            RoutingService.getInstance().setRoutingStrategyForMap(mappingName, account_id, params,IStrategy.STRATEGY_R);
			
			obj = dbAgent.getMapping(account_id, mappingName, sqlInfo);
		}
		catch(Exception e){
			processException(e);
		}
		//用于记录该方法的超时情况
		long  usedTime = System.currentTimeMillis() - t1;
		if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: getMapping region: "+mappingName+" size: "+params.length+" time: "+usedTime);
			}
		}
		
		return obj;
	}

	public Object getExtendMapping(String mappingName, Object[] keys)
			throws DaoException {
		return getMapping(null, mappingName, keys);
	}

	public Object getExtendMapping(Object account_id, String mappingName, Object key)throws DaoException {
		if (null == key) {
			return getMapping(account_id, mappingName, new Object[] {});
		} else {
			return getMapping(account_id, mappingName, new Object[] { key });
		}
	}

	public Object getExtendMapping(String mappingName, Object key)throws DaoException {
		return getMapping(null, mappingName, key);
	}
    /**
     *  =======================  getMappings  ======================================
     */    

    public List getMappings(Object accountId, String mapName, List<Object[]> paramsList) throws DaoException {
	   DalAssert.assertObjectNotNull(mapName);
	   DalAssert.assertObjectNotNull(paramsList);	   
	   long t1 = System.currentTimeMillis();
	   
	   List resList = new ArrayList();   
	   try{

		   RoutingService.getInstance().setRoutingStrategyForMap(mapName, accountId,paramsList.get(0),IStrategy.STRATEGY_R);
			
		   for(Object[] params  : paramsList){
				SqlInfo sqlInfo = SqlUtil.getMappingSqlInfo(mapName, params,false);
				Object obj = dbAgent.getMapping(accountId, mapName, sqlInfo);
				if(null != obj){
					resList.add(obj);
				}
			}
			return resList;	   
	   }
	   catch(Exception e){
		   processException(e);
	   }
		//用于记录该方法的超时情况
	   long  usedTime = System.currentTimeMillis() - t1;
	   if(usedTime >= singleUsedTime){
			if(permLog.isWarnEnabled()){
		    	permLog.warn("method: getMappings region: "+mapName+" size: "+paramsList.size()+" time: "+usedTime);
			}
		}
		
		return resList;
    }
    
    public List getMapList(List accountIdList, String mapname, List<Object[]> paramsList) throws DaoException {
 	   DalAssert.assertObjectNotNull(accountIdList);
	   DalAssert.assertObjectNotNull(mapname);
	   DalAssert.assertObjectNotNull(paramsList);
	  
	   List resList = new ArrayList();
	   try{ 
		   Map<String ,List<Object[]>> dbParamsMap = new HashMap<String,List<Object[]>>();
		   Map<String, Object> dbAccountMap = new HashMap<String,Object>();
		   for(int index = 0;index <accountIdList.size();index ++){
				Object accountId = accountIdList.get(index);
				
				RoutingService.getInstance().setRoutingStrategyForMap(mapname,accountId, paramsList.get(0),IStrategy.STRATEGY_R);
				
				String dbName = ContextHolder.getDataSource();
				if(null == (dbAccountMap.get(dbName))){
					Object[] params = paramsList.get(index);
					//@todo 此处需要一个新的处理逻辑
					if(null == params){
						continue;
					}
					else{
						dbAccountMap.put(dbName, accountId);
						List<Object[]> newParamList = new ArrayList<Object[]>();
						newParamList.add(params);
						dbParamsMap.put(dbName, newParamList);
					}
				}
				else{
					Object[] params = paramsList.get(index);
					//@todo 此处需要一个新的处理逻辑
					if(null == params){
						continue;
					}
					else{
						dbParamsMap.get(dbName).add(params);	
					}
				}
			}//for
			Iterator<Entry<String, Object>> dbIter = dbAccountMap.entrySet().iterator();
			while(dbIter.hasNext()){
				Entry<String, Object> entry = dbIter.next();// edit by songkun
				String dbName = entry.getKey();
				List<Object[]> bufList = null;
				List<SqlInfo> sqlInfoLs = SqlUtil.getMapAggreSqlInfos(mapname, dbParamsMap.get(dbName));
				RoutingService.getInstance().setRoutingStrategyForMap(mapname, dbAccountMap.get(dbName), IStrategy.STRATEGY_R);
				for(SqlInfo sqlInfo :sqlInfoLs){
				  bufList = DBAgentHibernateImpl.getInstance().getObjectList(null,null, sqlInfo);
				  resList.addAll(bufList);
				}
			}
	   }
	   catch(Exception e){
		   processException(e);
	   }
	   
       return resList;
    }

    
	private void processException(Exception e) throws DaoException {
		e.printStackTrace(System.out);
		if (e instanceof MappingException) {
			throw new DaoException(DaoException.POJO_NOTFOUND_EXCEPTION, e);
		} else if (e instanceof NullPointerException) {
			throw new DaoException(DaoException.NULLPOINTER_EXCEPTION, e);
		} else if (e instanceof SQLException) {
			throw new DaoException(DaoException.SQL_EXCEPTION, e);
		} else if (e instanceof HibernateException) {
			throw new DaoException(DaoException.Hibernate_Exception, e);
		}else if (e instanceof DaoException) {
			throw (DaoException) e;
		} else {
			throw new DaoException(e);
		}
	}
	public List getAggrIdList(Object account_id, String list_name,
			List<Object[]> paramsList, int start, int count)
			throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}


}
