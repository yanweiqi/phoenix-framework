package com.ginkgocap.ywxt.framework.dal.dao.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.MaxIdHolder;
import com.ginkgocap.ywxt.framework.dal.cache.NullObjectContent;
import com.ginkgocap.ywxt.framework.dal.dao.Dao;
import com.ginkgocap.ywxt.framework.dal.dao.DaoRecord;
import com.ginkgocap.ywxt.framework.dal.dao.Listener;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ListItem;
import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;
import com.ginkgocap.ywxt.framework.dal.dao.helper.LogHelper;
import com.ginkgocap.ywxt.framework.dal.dao.listener.DefaultListener;
import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;
import com.ginkgocap.ywxt.framework.dal.dao.util.CacheHelper;
import com.ginkgocap.ywxt.framework.dal.dao.util.Constants;
import com.ginkgocap.ywxt.framework.dal.dao.util.DalAssert;
import com.ginkgocap.ywxt.framework.dal.dao.util.ObjectUtil;
import com.ginkgocap.ywxt.framework.dal.dao.util.SqlUtil;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:44
 * @Copyright Copyright©2015 www.gintong.com
 */
public class CompositeDaoImpl implements Dao {

	private static final int singleUsedTime = 15;
	private static final int lotsUsedTime = 50;
	private Log permLog = LogFactory.getLog("DalPerformance");
	private Log log = LogFactory.getLog(CompositeDaoImpl.class);
	private Dao dao = new HibernateDaoImpl();
	private Listener listener = new DefaultListener();
	DaoRecord record = null;

	public CompositeDaoImpl() {
		record = DaoRecord.getInstance();
		listener = new DefaultListener();
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	/**
	 * ======================== Save ===========================================
	 */
	public Serializable save(Object account_id, Object object) throws DaoException {
		DalAssert.assertObjectNotNull(object);
		DalAssert.assertObjectIllegal(account_id, object);
		Serializable id = null;
		try {
			listener.onSaveBegin(account_id, object);
			id = dao.save(account_id, object);
			if (null == id) {
				LogHelper.savedError(log, object);
				return id;
			}
			if (id.toString().equals("0")) {
				Class c = null;
				if (object != null) {
					c = object.getClass();
				}
				log.error("error:save object id =" + id + ",class=" + c);
			}
			listener.onSaveCompleted((Serializable) account_id, object.getClass(), id, object);
		} catch (Exception e) {
			listener.onSaveError(account_id, id, object);
			processException(e);
		} finally {
			try {
				listener.onSaveEnd(account_id, id, object);
			} catch (Exception e) {
				processException(e);
			}
		}
		return id;
	}

	public Serializable save(Object object) throws DaoException {
		return save(null, object);
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
		return save(null, objects);
	}

	/**
	 * ======================= Batch Save ======================================
	 */

	public List batchSave(Object account_id, List objList) throws DaoException {
		DalAssert.assertObjectNotNull(objList);
		List resList = new ArrayList();
		Map<Serializable, Object> idObjMap = null;
		try {
			listener.onBatchSaveBegin(account_id, objList);
			idObjMap = ((HibernateDaoImpl) dao).batchSaveReturnMap(account_id, objList);
			// Iterator<Serializable> iter = idObjMap.keySet().iterator(); //
			// edit by songkun
			Iterator<Entry<Serializable, Object>> iter = idObjMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Serializable, Object> entry = iter.next();
				Serializable id = entry.getKey();
				resList.add(idObjMap.get(id));
			}
			listener.onBatchSaveCompleted((Serializable) account_id, objList.get(0).getClass(), idObjMap);
		} catch (Exception e) {
			// resList = new ArrayList();// edit by songkun
			processException(e);
		} finally {
			listener.onBatchSaveEnd(account_id, objList);
		}
		return resList;
	}

	public List batchSave(List objList) throws DaoException {
		return batchSave(null, objList);
	}

	/**
	 * ======================= update ======================================
	 */

	public boolean update(Object account_id, Object object) throws DaoException {
		DalAssert.assertObjectNotNull(object);
		DalAssert.assertObjectIllegal(account_id, object);

		long t1 = System.currentTimeMillis();
		boolean bUpdated = true;
		boolean bLock = false;
		Object oldObject = null;
		Serializable objectId = null;

		try {
			objectId = ObjectUtil.getObjectId(object);
			if (null == objectId) {
				LogHelper.failedGetObjectId(log, object);
				return bUpdated;
			}
			oldObject = get(account_id, object.getClass(), objectId);
			if (null == oldObject) {
				LogHelper.failedGetObjectById(log, account_id, object.getClass(), objectId);
				return bUpdated;
			}
			bLock = true;
			listener.onUpdateBegin(account_id, objectId, object, oldObject);
			bUpdated = dao.update(account_id, object);
			if (!bUpdated) {
				LogHelper.updateError(log, object);
				return bUpdated;
			}
			listener.onUpdateCompleted(account_id, objectId, object, oldObject);
		} catch (Exception e) {
			listener.onUpdateError(account_id, objectId, object, oldObject);
			processException(e);
		} finally {
			try {
				if (bLock) {
					listener.onUpdateEnd(account_id, objectId, object, oldObject);
				}
			} catch (Exception e) {
				processException(e);
			}
		}

		long usedTime = System.currentTimeMillis() - t1;
		if (usedTime >= singleUsedTime) {
			if (permLog.isWarnEnabled()) {
				permLog.warn("method: update region: " + object.getClass().getName() + " params: " + objectId + " time: " + usedTime);
			}
		}
		return bUpdated;
	}

	public boolean update(Object object) throws DaoException {
		return update(null, object);
	}

	/**
	 * 更新对象列表
	 * 
	 * @param account_id
	 *            :用户的id,用于决定数据库操作时，路由的策略
	 * @param objects
	 *            :待更新的对象列表
	 */
	public boolean batchUpdate(Object account_id, List objects) throws DaoException {
		DalAssert.assertObjectNotNull(objects);
		DalAssert.assertListIllegal(account_id, objects);
		boolean bUpdated = true;
		try {
			for (Object obj : objects) {
				update(account_id, obj);
			}
		} catch (Exception e) {
			processException(e);
		}
		return bUpdated;
	}

	public boolean batchUpdate(List objects) throws DaoException {
		return batchUpdate(null, objects);
	}

	public boolean delete(Object account_id, Class clazz, Serializable id) throws DaoException {
		return realDelete(account_id, clazz, id, true);
	}

	public boolean delete(Class clazz, Serializable id) throws DaoException {
		return delete(null, clazz, id);
	}

	public boolean fakeDelete(Object account_id, Class clazz, Serializable id) throws DaoException {
		return realDelete(account_id, clazz, id, false);
	}

	public boolean fakeDelete(Class clazz, Serializable id) throws DaoException {
		return fakeDelete(null, clazz, id);
	}

	/**
	 * 删除逻辑：1. 先判断待删除数据是否存在，存在才删除，否则认为删除成功 2. 先删除数据库中记录，再更新缓存
	 * 根据对象的class类型和id来删除对象
	 * 
	 * @param account_id
	 *            : 执行数据库删除动作需要的策略id
	 * @param clazz
	 *            :待删除对象的Class类型
	 * @param id
	 *            : 待删除对象的id
	 */
	private boolean realDelete(Object account_id, Class clazz, Serializable id, boolean delFlag) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(id);

		long t1 = System.currentTimeMillis();

		boolean bDeleted = true;
		Object obj = null;

		try {
			obj = get(account_id, clazz, id);
			if (null == obj) {
				LogHelper.failedGetObjectById(log, account_id, clazz, id);
				return bDeleted;
			}
			listener.onDeleteBegin(account_id, id, obj);
			if (delFlag) {
				bDeleted = dao.delete(account_id, clazz, id);
			} else {
				bDeleted = dao.fakeDelete(account_id, clazz, id);
			}
			if (!bDeleted) {
				LogHelper.deleteError(log, clazz, account_id, id, delFlag);
				return delFlag;
			}
			listener.onDeleteCompleted(account_id, id, obj);

		} catch (Exception e) {
			listener.onDeleteError(account_id, id, obj);
			processException(e);
		}
		long usedTime = System.currentTimeMillis() - t1;
		if (usedTime >= singleUsedTime) {
			if (permLog.isWarnEnabled()) {
				permLog.warn("method: realDelete region: " + clazz.getName() + " params: " + id + " time: " + usedTime);
			}
		}
		return bDeleted;

	}

	/**
	 * @param clazz
	 *            : 待删除对象的class类型
	 * @param ids
	 *            : 待删除对象的id列表
	 * @todo :deleteList的逻辑需要进一步确认，以避免脏数据 删除逻辑：1.先删除数据库中记录，再更新缓存 2.逐个更新缓存中的对象
	 */

	private boolean realDeleteList(Object account_id, Class clazz, List ids, boolean delFlag) throws DaoException {
		DalAssert.assertObjectNotNull(clazz);
		DalAssert.assertObjectNotNull(ids);
		boolean bDeleted = true;
		try {
			for (Object id : ids) {
				realDelete(account_id, clazz, (Serializable) id, delFlag);
			}
		} catch (Exception e) {
			bDeleted = false;
			processException(e);
		}
		return bDeleted;
	}

	public boolean deleteList(Object account_id, Class clazz, List ids) throws DaoException {
		return realDeleteList(account_id, clazz, ids, true);
	}

	public boolean deleteList(Class clazz, List ids) throws DaoException {
		return deleteList(null, clazz, ids);
	}

	public boolean fakeDeleteList(Object account_id, Class clazz, List ids) throws DaoException {
		return realDeleteList(account_id, clazz, ids, false);
	}

	public boolean fakeDeleteList(Class clazz, List ids) throws DaoException {
		return fakeDeleteList(null, clazz, ids);
	}

	/**
	 * @param account_id
	 *            :
	 * @param list_name
	 *            :
	 * @param params
	 *            :
	 */
	public boolean deleteList(Object account_id, String list_name, Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(list_name);
		DalAssert.assertObjectNotNull(params);
		boolean res = true;
		try {
			List idList = getIdList(account_id, list_name, params);
			if (null != idList && idList.size() > 0) {
				String clsName = DaoHelper.getObjectNameByListName(list_name);
				if (StringUtils.isEmpty(clsName)) {
					log.error("find class name is empty or null by list name" + StringUtils.defaultIfEmpty(list_name, ""));
					return true;
				}
				res = deleteList(account_id, Class.forName(clsName), idList);
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

	public boolean fakeDeleteList(Object account_id, String list_name, Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(list_name);
		DalAssert.assertObjectNotNull(params);
		boolean res = true;
		try {
			List idList = getIdList(account_id, list_name, params);
			if (null != idList && idList.size() > 0) {
				String clsName = DaoHelper.getObjectNameByListName(list_name);
				if (StringUtils.isEmpty(clsName)) {
					log.error("find class name is empty or null by " + StringUtils.defaultIfEmpty(list_name, ""));
					return true;
				}
				res = realDeleteList(account_id, Class.forName(clsName), idList, false);
			}
		} catch (Exception e) {
			processException(e);
		}
		return res;
	}

	public boolean fakeDeleteList(String list_name, Object[] params) throws DaoException {
		return fakeDeleteList(null, list_name, params);
	}

	public boolean fakeDeleteList(Object account_id, String list_name, Object param) throws DaoException {
		if (null == param) {
			return fakeDeleteList(account_id, list_name, new Object[] {});
		} else {
			return fakeDeleteList(account_id, list_name, new Object[] { param });
		}
	}

	public boolean fakeDeleteList(String list_name, Object param) throws DaoException {
		return fakeDeleteList(null, list_name, param);

	}

	/**
	 * 缓存 --》最大值 --》不存在的值 --》数据库 读取逻辑： 先读取缓存中的数据，不存在的则从数据库加载，并更新缓存
	 */
	public Object get(Object account_id, Class clazz, Serializable id) throws DaoException {
		DalAssert.assertClassTypeNotNull(clazz);
		DalAssert.assertObjectNotNull(id);

		long t1 = System.currentTimeMillis();
		Object obj = null;
		try {
			Cache cache = CacheHelper.getClassCache(clazz);
			if (null == cache) {
				obj = dao.get(account_id, clazz, id);
			} else {
				if (CacheHelper.isDelete(clazz.getName(), id + "")) {
					LogHelper.objectHasBeenDeleted(log, clazz, id);
					return null;
				}
				obj = CacheHelper.get(clazz.getName(), id + "");
				if (null == obj) {
					Long maxId = MaxIdHolder.getMaxId(Constants.MaxIdPrefix + clazz.getName());
					Long newId = new Long(id + "");
					if ((newId.longValue() <= maxId.longValue() || maxId.longValue() < 0l)) {
						if (!NullObjectContent.isNull(Constants.NullObjectPrefix + clazz.getName() + newId + "")) {
							obj = dao.get(account_id, clazz, id);
							if (null != obj) {
								CacheHelper.put(clazz.getName(), id + "", obj);
							} else {
								NullObjectContent.setNull(Constants.NullObjectPrefix + clazz.getName() + newId + "");
							}
						}
					}
				}// if
			}// else
				// 数据的合法性 验证 ，Just For More safety ^_^
				// DalAssert.assertObjectIllegal(account_id, obj);
		} catch (Exception e) {
			processException(e);
		}

		long usedTime = System.currentTimeMillis() - t1;
		if (usedTime >= singleUsedTime) {
			if (permLog.isWarnEnabled()) {
				permLog.warn("method: get region: " + clazz.getName() + " params: " + id + " time: " + usedTime);
			}
		}

		return obj;
	}

	public Object get(Class clazz, Serializable id) throws DaoException {
		return get(null, clazz, id);
	}

	/**
	 * 读取逻辑： 读取逻辑同get(....)方法相同
	 */
	public Object getMapping(Object account_id, String mappingName, Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(mappingName);
		DalAssert.assertObjectNotNull(params);

		long t1 = System.currentTimeMillis();
		Object obj = null;
		try {
			Cache cache = CacheHelper.getListCache(mappingName);
			if (null == cache) {
				return dao.getMapping(account_id, mappingName, params);
			} else {
				MapInfo info = new MapInfo(mappingName, params);
				obj = CacheHelper.get(info.getRegion(), info.getKey());
				if (null == obj) {
					if (!NullObjectContent.isNull(Constants.NullObjectPrefix + "_" + info.getRegion() + "_" + info.getKey() + "")) {
						obj = dao.getMapping(account_id, mappingName, params);
						if (null != obj) {
							CacheHelper.put(info.getRegion(), info.getKey(), obj);
						} else {
							NullObjectContent.setNull(Constants.NullObjectPrefix + "_" + info.getRegion() + "_" + info.getKey() + "");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			processException(e);
		}

		long usedTime = System.currentTimeMillis() - t1;
		if (usedTime >= singleUsedTime) {
			if (permLog.isWarnEnabled()) {
				permLog.warn("method: getRealMapping region: " + mappingName + " size: " + params.length + " time: " + usedTime);
			}
		}

		return obj;
	}

	public Object getMapping(String mappingName, Object[] keys) throws DaoException {
		return getMapping(null, mappingName, keys);
	}

	public Object getMapping(Object account_id, String mappingName, Object key) throws DaoException {
		if (null == key) {
			return getMapping(account_id, mappingName, new Object[] {});
		} else {
			return getMapping(account_id, mappingName, new Object[] { key });
		}
	}

	public Object getMapping(String mappingName, Object key) throws DaoException {
		return getMapping(null, mappingName, key);
	}

	/**
	 * 读取逻辑： 先读缓存中的信息，如果没有再从数据库加载
	 * 
	 * @param account_id
	 *            : 策略id,仅用于数据库相关操作
	 * @param params
	 *            : 查询list，所需的参数
	 */
	public int count(Object account_id, String listName, Object[] params) throws DaoException {
		DalAssert.assertObjectNotNull(listName);
		DalAssert.assertObjectNotNull(params);

		// long t1 = System.currentTimeMillis();
		long count = 0;
		Long lock = null;
		LsCacheInfo lsInfo = new LsCacheInfo(listName, params);

		try {
			Cache cache = CacheHelper.getListCache(listName);
			if (null == cache) {
				return dao.count(account_id, listName, params);
			} else {
				count = cache.incr(lsInfo.getListCntKey(), 0);
				if (count > 0) {
					return new Long(count).intValue();
				} else {
					for (int i = 0; i < 3; i++) {
						lock = record.checkCntListLoading(lsInfo);

						if (null != lock) {
							break;
						}
						log.info("get lock : " + (i + 1));
						Thread.sleep(100);

						count = cache.incr(lsInfo.getListCntKey(), 0);
						if (count >= 0) {
							return new Long(count).intValue();
						}
					}

					if (null == lock) {
						log.error("checkCntListLoading, lock is null, account_id:" + account_id + ", listName:" + listName + ", params:" + params);
						return 0;
					}

					count = dao.count(account_id, listName, params);
					cache.put(lsInfo.getListCntKey(), "0");
					cache.incr(lsInfo.getListCntKey(), count);
				}
			}
		} catch (Exception e) {
			processException(e);
		} finally {
			if (null != lock) {
				try {
					record.endCntListLoading(lsInfo, lock);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		}
		// return new Long(count).intValue();
		return Long.valueOf(count).intValue();// edit by songkun
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sohu.sns.dal.dao.Dao#count(java.lang.String, java.lang.Object[])
	 */
	public int count(String list_name, Object[] params) throws DaoException {
		return count(null, list_name, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sohu.sns.dal.dao.Dao#count(java.lang.Long, java.lang.String,
	 * java.lang.Object)
	 */
	public int count(Object account_id, String list_name, Object param) throws DaoException {
		if (null == param) {
			return count(account_id, list_name, new Object[] {});
		} else {
			return count(account_id, list_name, new Object[] { param });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sohu.sns.dal.dao.Dao#count(java.lang.String, java.lang.Object)
	 */
	public int count(String list_name, Object param) throws DaoException {
		return count(null, list_name, param);
	}

	// /**
	// * @param account_id :策略id, 仅用于从数据库中加载数据时
	// * @param start : start从 0 开始计数
	// * @param count :
	// */
	// public List getIdList(Object account_id, String listName, Object[]
	// params, Integer start, Integer count) throws DaoException {
	// long t1 = System.currentTimeMillis();
	//
	// DalAssert.assertObjectNotNull(listName);
	// DalAssert.assertObjectNotNull(params);
	// DalAssert.assertObjectNotNull(start);
	// DalAssert.assertObjectNotNull(count);
	//
	// List idList = null;
	// Cache cache = null;
	// String key = null;
	// Long lock = null;
	// LsCacheInfo lsInfo = new LsCacheInfo(listName, params);
	// Type type =
	// TypeFactory.basic(DaoHelper.getListItemValueClass(listName).getName());
	// try {
	// ListItem listItem = DaoHelper.getListItemByListName(listName);
	// cache = CacheHelper.getListCache(listName);
	// boolean bLoadUsingDB = true;
	// if (null != cache && start < listItem.getLimitLen() && (start + count) <
	// listItem.getLimitLen()) {
	// key = lsInfo.getKey();
	// idList = cache.lrange(key, start, start + count);
	// //情况一:Id List 目前不存在在缓存中的情况
	// if (null == idList) {
	// //情况一.1： 缓存中不存在List, 但加载的数据在 初始化 范围内
	// int listCnt = count(account_id, listName, params);
	// //重要逻辑：如果List不存在，就不需要通过DB再加载了
	// if (listCnt > 0) {
	// if (start < listItem.getInitLen() && (start + count) <
	// listItem.getInitLen()) {
	// lock = record.checkListLoading(lsInfo);
	// //情况一.1.1： 缓存中不存在List, 但加载的数据在 初始化 范围内,而且可以获得锁，得以加载数据的情况
	// if (null != lock) {
	// List totalList = dao.getIdList(account_id, listName, params, 0,
	// listItem.getInitLen());
	// if (null == totalList || totalList.size() == 0) {
	// log.warn("[id list is empty] listName:" + listName + "  key:" + key);
	// } else {
	// cache.setList(key, totalList);
	// }
	// if (start + count > (totalList.size() - 1)) {
	// idList = totalList.subList(start, totalList.size());
	// } else {
	// idList = totalList.subList(start, start + count);
	// }
	// bLoadUsingDB = false;
	// } //if(lock)
	// }
	// } else{
	// bLoadUsingDB = false;
	// }
	// }
	// //情况二：Id List
	// 已经存在在缓存中,但并没有取出足够数据的情况,此时通过DB加载数据，因为考虑到像[Feed]这样的应用，count(*) 是很费事的操作
	// else if (idList.size() < count) {
	// ;
	// }
	// //情况三：Id List 已经存在在缓存中，而且取出正确数据的情况,但是需要进行类型的转换
	// else {
	// if (type == Hibernate.LONG) {
	// List<Long> resList = new ArrayList<Long>();
	// for (Object idObj : idList) {
	// resList.add(Long.parseLong("" + idObj));
	// }
	// idList = resList;
	// } else if (type == Hibernate.INTEGER) {
	// List<Long> resList = new ArrayList<Long>();
	// for (Object idObj : idList) {
	// resList.add(Long.parseLong("" + idObj));
	// }
	// idList = resList;
	// }
	// bLoadUsingDB = false;
	// }
	// }
	// if (bLoadUsingDB) {
	// idList = dao.getIdList(account_id, listName, params, start, count);
	// }
	// } catch (Exception e) {
	// processException(e);
	// } finally {
	// if (null != lock) {
	// try {
	// record.endListLoading(lsInfo, lock);
	// } catch (Exception e) {
	// e.printStackTrace(System.out);
	// }
	// }
	// }
	// if (null == idList) {
	// idList = new ArrayList();
	// }
	// return idList;
	// }

	/**
	 * @param account_id
	 *            :策略id, 仅用于从数据库中加载数据时
	 * @param start
	 *            : start从 0 开始计数
	 * @param count
	 *            :
	 */
	public List getIdList(Object account_id, String listName, Object[] params, Integer start, Integer count) throws DaoException {
		// long t1 = System.currentTimeMillis();

		DalAssert.assertObjectNotNull(listName);
		DalAssert.assertObjectNotNull(params);
		DalAssert.assertObjectNotNull(start);
		DalAssert.assertObjectNotNull(count);

		List idList = null;
		Cache cache = null;
		String key = null;
		Long lock = null;
		Integer toIndex = start + count;

		LsCacheInfo lsInfo = new LsCacheInfo(listName, params);
		Type type = SqlUtil.getType(DaoHelper.getListItemValueClass(listName).getName());
		try {
			boolean bLoadUsingDB = true;

			ListItem listItem = DaoHelper.getListItemByListName(listName);
			cache = CacheHelper.getListCache(listName);

			if (cache != null) {
				key = lsInfo.getKey();
				idList = cache.lrange(key, start, start + count);
				if (CollectionUtils.isNotEmpty(idList) && ObjectUtils.equals(idList.size(), count)) {
					;
				} else {
					int iCount = count(account_id, listName, params);

					if (iCount == 0) {
						return new ArrayList();
					}
					if (start > iCount) {
						return new ArrayList();
					}

					if (toIndex >= iCount) {
						toIndex = iCount;
					}

					if (start >= toIndex) {
						return new ArrayList();
					}

					if (CollectionUtils.isEmpty(idList)) {
						if (cache.get(key) == null) {
							lock = record.checkListLoading(lsInfo);
							if (null != lock) {
								List totalList = dao.getIdList(account_id, listName, params, 0, listItem.getInitLen());
								if (CollectionUtils.isEmpty(totalList)) {
									log.warn("[id list is empty] listName:" + listName + "  key:" + key);
									return new ArrayList();
								} else {
									cache.setList(key, totalList);
									if (totalList.size() >= toIndex) {
										List subList = new ArrayList();
										subList.addAll(totalList.subList(start, toIndex));
										return subList;
									}
								}
							}
						}
					} else {
						if (toIndex > cache.lsize(key)) {
							idList = null;
						}
					}

				}
			}

			if (CollectionUtils.isNotEmpty(idList)) {
				if (type == LongType.INSTANCE) {
					List<Long> resList = new ArrayList<Long>();
					for (Object idObj : idList) {
						resList.add(Long.parseLong("" + idObj));
					}
					idList = resList;
				} else if (type == IntegerType.INSTANCE) {
					List<Long> resList = new ArrayList<Long>();
					for (Object idObj : idList) {
						resList.add(Long.parseLong("" + idObj));
					}
					idList = resList;
				}
			} else {
				idList = dao.getIdList(account_id, listName, params, start, count);
			}

		} catch (Exception e) {
			processException(e);
		} finally {
			if (null != lock) {
				try {
					record.endListLoading(lsInfo, lock);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		}
		if (null == idList) {
			idList = new ArrayList();
		}
		return idList;
	}

	public List getIdList(Object account_id, String list_name, Object[] params) throws DaoException {
		Integer totalCount = count(account_id, list_name, params);
		return getIdList(account_id, list_name, params, 0, totalCount);
	}

	public List getIdList(String list_name, Object[] params) throws DaoException {
		return getIdList(null, list_name, params);
	}

	public List getIdList(Object account_id, String list_name, Object param) throws DaoException {
		if (null == param) {
			return getIdList(account_id, list_name, new Object[] {});
		} else {
			return getIdList(account_id, list_name, new Object[] { param });
		}
	}

	public List getIdList(String list_name, Object param) throws DaoException {
		return getIdList(null, list_name, param);
	}

	public List getIdList(String list_name, Object[] params, Integer start, Integer count) throws DaoException {
		return getIdList(null, list_name, params, start, count);
	}

	public List getIdList(Object account_id, String list_name, Object param, Integer start, Integer count) throws DaoException {
		if (null == param) {
			return getIdList(account_id, list_name, new Object[] {}, start, count);
		} else {
			return getIdList(account_id, list_name, new Object[] { param }, start, count);
		}
	}

	public List getIdList(String list_name, Object param, Integer start, Integer count) throws DaoException {
		return getIdList(null, list_name, param, start, count);
	}

	public List getIdList(Object accountId, String mapName, List<Object[]> paramsList) throws DaoException {
		List list = getMappings(accountId, mapName, paramsList);
		for (int i = 0; i < list.size(); i++) {
			list.remove(i);
		}
		return list;
	}

	public List getIdList(String mapName, List<Object[]> paramsList) throws DaoException {
		return getIdList(null, mapName, paramsList);
	}

	public List<List> getAggrIdList(Object account_id, String list_name, List<Object[]> paramsList, int start, int count) throws DaoException {
		// long t1 = System.currentTimeMillis();
		List objList = new ArrayList();
		// if (null == paramsList && paramsList.size() < 1) {
		if (null == paramsList || paramsList.size() < 1) { // edit by songkun
			log.warn(" paramsList is empty");
			return objList;
		}
		try {
			Cache cache = CacheHelper.getListCache(list_name);
			if (null == cache) {
				objList = dao.getAggrIdList(account_id, list_name, paramsList, start, count);
			}
			// 二.用户配置了缓存的情况
			else {
				List<Object[]> unExistList = new ArrayList();
				// List<String> idStrList = new ArrayList<String>();
				List<Integer> indexLs = new ArrayList<Integer>();

				Object[] keyArray = new Object[paramsList.size()];
				Object[] startArray = new Object[paramsList.size()];
				Object[] countArray = new Object[paramsList.size()];

				for (int i = 0; i < paramsList.size(); i++) {
					LsCacheInfo lsInfo = new LsCacheInfo(list_name, paramsList.get(i));
					keyArray[i] = lsInfo.getKey();
					startArray[i] = start;
					countArray[i] = count;
				}

				Object[] obs = null;
				// obs = cache.lrange(keyArray,startArray,countArray);
				for (int i = 0; i < obs.length; i++) {
					Object object = obs[i];
					if (null == object) {
						indexLs.add(i);
						unExistList.add(paramsList.get(i));
					}
				}

				// 为存在缓存中的id的列表
				// 遍历查找未存储在缓存中的数据

				// 从数据库重新加载未在缓存中的对象
				if (unExistList.size() > 0) {
					for (int i = 0; i < unExistList.size(); i++) {
						List newIdList = dao.getIdList(account_id, list_name, unExistList.get(i), start, count);
						if (newIdList.size() > 0) {
							obs[indexLs.get(i)] = newIdList;
						}
					}
				}
				if (null != obs && obs.length > 0) {
					for (Object obj : obs) {
						objList.add(obj);
					}
					return objList;
				} else {
					return objList;
				}

			}// else
		} catch (Exception e) {
			processException(e);
		}
		return objList;
	}

	public List getList(Object account_id, Class clazz, List ids) throws DaoException {
		DalAssert.assertClassTypeNotNull(clazz);
		DalAssert.assertObjectNotNull(ids);

		long t1 = System.currentTimeMillis();
		List objList = new ArrayList();
		if (CollectionUtils.isEmpty(ids)) {
			log.warn(" id list is empty");
			return objList;
		}

		try {
			Cache cache = CacheHelper.getClassCache(clazz);
			if (null == cache) {
				objList = dao.getList(account_id, clazz, ids);
			} else {
				List<String> idStrList = new ArrayList<String>();
				List daoIds = new ArrayList();
				Map<Long, Integer> indexIdMap = new HashMap<Long, Integer>();
				for (Object id : ids) {
					idStrList.add("" + id);
				}

				Object[] obs = cache.get((String[]) idStrList.toArray(new String[idStrList.size()]));
				Map<String, Object> resultMap = new HashMap();
				for (int i = 0; i < obs.length; i++) {
					Object value = obs[i];
					String idStr = idStrList.get(i);
					resultMap.put(idStr, value);
					if (null == value && !NullObjectContent.isNull(Constants.NullObjectPrefix + clazz.getName() + idStr)) {
						daoIds.add(ids.get(i));
					}
				}
				if (CollectionUtils.isNotEmpty(daoIds)) {

					List newObjLs = dao.getList(account_id, clazz, daoIds);
					if (CollectionUtils.isNotEmpty(newObjLs)) {
						Cache objCache = CacheHelper.getClassCache(clazz);

						for (Object object : newObjLs) {
							if (object != null) {
								Long id = (Long) ObjectUtil.getObjectId(object);
								if (id != null) {
									resultMap.put(id.toString(), object);
									objCache.put(id.toString(), object);
								} else {
									log.error("don't find the id for " + object.getClass());

								}
							}
						}

					}
				}

				for (int i = 0; i < idStrList.size(); i++) {
					Object value = resultMap.get(idStrList.get(i));
					if (value != null) {
						objList.add(value);
					}
				}

			}// else

			long usedTime = System.currentTimeMillis() - t1;
			if (usedTime >= singleUsedTime) {
				if (permLog.isWarnEnabled()) {
					permLog.warn("method: getList region: " + clazz.getName() + " size: " + ids.size() + " time: " + usedTime);
				}
			}
		} catch (Exception e) {
			processException(e);
		}
		return objList;
	}

	// public List getList(Object account_id, Class clazz, List ids) throws
	// DaoException {
	// DalAssert.assertClassTypeNotNull(clazz);
	// DalAssert.assertObjectNotNull(ids);
	//
	// long t1 = System.currentTimeMillis();
	// List objList = new ArrayList();
	// if (CollectionUtils.isEmpty(ids)) {
	// log.warn(" id list is empty");
	// return objList;
	// }
	//
	// try {
	// Cache cache = CacheHelper.getClassCache(clazz);
	// if (null == cache) {
	// objList = dao.getList(account_id, clazz, ids);
	// }
	// else {
	// List<String> idStrList = new ArrayList<String>();
	// List daoIds = new ArrayList();
	// Map<Long, Integer> indexIdMap = new HashMap<Long, Integer>();
	// for (Object id : ids) {
	// idStrList.add("" + id);
	// }
	//
	//
	// Object[] obs = cache.get((String[]) idStrList.toArray(new
	// String[idStrList.size()]));
	// Map<String, Object> resultMap = new HashMap();
	// for (int i = 0; i < obs.length; i++) {
	// Object value = obs[i];
	// String idStr = idStrList.get(i);
	// resultMap.put(idStr, value);
	// if (null == value && !NullObjectContent.isNull(Constants.NullObjectPrefix
	// + clazz.getName() + idStr)) {
	// daoIds.add(ids.get(i));
	// }
	// }
	// if (CollectionUtils.isNotEmpty(daoIds)) {
	//
	// List newObjLs = dao.getList(account_id, clazz, daoIds);
	// if (CollectionUtils.isNotEmpty(newObjLs)) {
	// Cache objCache = CacheHelper.getClassCache(clazz);
	//
	//
	// for (Object object : newObjLs) {
	// if (object != null) {
	// Long id = (Long) ObjectUtil.getObjectId(object);
	// if (id != null) {
	// resultMap.put(id.toString(), object);
	// objCache.put(id.toString(), object);
	// } else {
	// log.error("don't find the id for " + object.getClass());
	//
	// }
	// }
	// }
	//
	// }
	// }
	//
	// for (int i = 0; i < idStrList.size(); i++) {
	// objList.add(resultMap.get(idStrList.get(i)));
	// }
	//
	//
	// }// else
	//
	// long usedTime = System.currentTimeMillis() - t1;
	// if (usedTime >= singleUsedTime) {
	// if (permLog.isWarnEnabled()) {
	// permLog.warn("method: getList region: " + clazz.getName() + " size: " +
	// ids.size() + " time: " + usedTime);
	// }
	// }
	// } catch (Exception e) {
	// processException(e);
	// }
	// return objList;
	// }
	//
	public List getList(Class clazz, List ids) throws DaoException {
		return  getList(null, clazz, ids);
	}

	public List getMappings(Object accountId, String mapName, List<Object[]> paramsList) throws DaoException {
		DalAssert.assertObjectNotNull(mapName);
		DalAssert.assertObjectNotNull(paramsList);

		long t1 = System.currentTimeMillis();
		try {
			Cache cache = CacheHelper.getListCache(mapName);
			// 缓存不存在情况下处理逻辑
			if (null == cache) {
				return dao.getMappings(accountId, mapName, paramsList);
			}
			// 用户配置了缓存情况下的处理逻辑
			else {
				List<Object[]> unExistList = new ArrayList<Object[]>();
				// 批量查询的map，在缓存中Key值得数组
				List<String> mapKeyList = new ArrayList<String>();
				List<Integer> indexLs = new ArrayList<Integer>();
				for (Object[] obs : paramsList) {
					MapInfo mapInfo = new MapInfo(mapName, obs);
					mapKeyList.add(mapInfo.getKey());
				}
				// 从缓存中批量取出，目前存放在缓存中的 ,Map
				Object[] obs = cache.get((String[]) mapKeyList.toArray(new String[mapKeyList.size()]));
				// 情况一 ：缓存中一个也没有
				if (null == obs || 1 == obs.length) {
					for (int j = 0; j < paramsList.size(); j++) {
						indexLs.add(j);
						unExistList.add(paramsList.get(j));
					}
				} else {
					for (int i = 0; i < obs.length; i++) {
						Object object = obs[i];
						if (null == object) {
							indexLs.add(i);
							unExistList.add(paramsList.get(i));
						}
					}
				}
				// 为存在缓存中的id的列表
				// 遍历查找未存储在缓存中的数据
				// 从数据库重新加载未在缓存中的对象
				if (unExistList.size() > 0) {
					List newObjLs = dao.getMappings(accountId, mapName, unExistList);
					if (newObjLs.size() > 0) {
						if (newObjLs.size() == unExistList.size()) {
							for (int j = 0; j < newObjLs.size(); j++) {
								Object object = newObjLs.get(j);
								if (null != object) {
									Object[] params = unExistList.get(j);
									MapInfo mapInfo = new MapInfo(mapName, params);
									cache.put(mapInfo.getKey(), object);
									obs[indexLs.get(j)] = object;
								}
							}
						}
						// Attention :如果未从数据库中加载到全部的Object,则不做任何处理
						else {
							LogHelper.failedLoadMapsLsByParams(log, mapName, paramsList);
						}
					}
				}
				// 从数组转化到List,如果直接用Array.toList()会有问题
				List objList = new ArrayList();
				if (null != obs && obs.length > 0) {
					for (Object obj : obs) {
						objList.add(obj);
					}
				}

				long usedTime = System.currentTimeMillis() - t1;
				if (usedTime >= singleUsedTime) {
					if (permLog.isWarnEnabled()) {
						permLog.warn("method: getMappings region: " + mapName + " size: " + paramsList.size() + " time: " + usedTime);
					}
				}

				return objList;
			}// else
		}// try
		catch (Exception e) {
			processException(e);
			return null;
		}
	}

	public List getMapList(List accountIdList, String mapname, List<Object[]> paramsList) throws DaoException {
		DalAssert.assertObjectNotNull(accountIdList);
		DalAssert.assertObjectNotNull(mapname);
		DalAssert.assertObjectNotNull(paramsList);
		List resList = new ArrayList();
		try {
			Cache cache = CacheHelper.getListCache(mapname);
			// 缓存不存在情况下处理逻辑
			if (null == cache) {
				dao.getMapList(accountIdList, mapname, paramsList);
			} else {
				List<Object[]> unExistList = new ArrayList<Object[]>();
				// 批量查询的map，在缓存中Key值得数组
				List<String> mapKeyList = new ArrayList<String>();
				List<Integer> indexLs = new ArrayList<Integer>();
				for (Object[] obs : paramsList) {
					MapInfo mapInfo = new MapInfo(mapname, obs);
					mapKeyList.add(mapInfo.getKey());
				}
				// 从缓存中批量取出，目前存放在缓存中的 ,Map
				Object[] obs = cache.get((String[]) mapKeyList.toArray(new String[mapKeyList.size()]));
				// 情况一 ：缓存中一个也没有
				if (null == obs || 1 == obs.length) {
					for (int j = 0; j < paramsList.size(); j++) {
						indexLs.add(j);
						unExistList.add(paramsList.get(j));
					}
				} else {
					for (int i = 0; i < obs.length; i++) {
						Object object = obs[i];
						if (null == object) {
							indexLs.add(i);
							unExistList.add(paramsList.get(i));
						}
					}
				}
				// 为存在缓存中的id的列表
				// 遍历查找未存储在缓存中的数据
				// 从数据库重新加载未在缓存中的对象
				if (unExistList.size() > 0) {
					for (int i = 0; i < unExistList.size(); i++) {
						int oldIndex = indexLs.get(i);
						Object obj = ((HibernateDaoImpl) dao).getExtendMapping(accountIdList.get(oldIndex), mapname, unExistList.get(i));
						if (null != obj) {
							obs[oldIndex] = obj;
						}
					}
				}
				// 从数组转化到List,如果直接用Array.toList()会有问题
				List objList = new ArrayList();
				if (null != obs && obs.length > 0) {
					for (Object obj : obs) {
						objList.add(obj);
					}
				}
				return objList;
			}
		} catch (Exception e) {
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
		} else if (e instanceof DaoException) {
			throw (DaoException) e;
		} else {
			throw new DaoException(e);
		}
	}

}
