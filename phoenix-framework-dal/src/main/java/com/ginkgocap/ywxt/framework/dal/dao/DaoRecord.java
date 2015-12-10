package com.ginkgocap.ywxt.framework.dal.dao;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;

/**
 * 记录数据操作的历史
 * 
 * @author allenshen
 * @date 2015年10月29日
 * @time 下午8:36:20
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DaoRecord {
	private static Logger logger = Logger.getLogger(DaoRecord.class);

	public static long useful_limt = 1 * 60 * 1000l; // 1 分钟

	// 锁定数据在缓存中的最长有效期是 30 s
	private static final long maxSpan = 30000l;

	/**
	 * ===========================
	 * Object============================================================
	 */
	// 保存完成的对象容器
	public static Map<String, Long> entity_save_completed = Collections.synchronizedMap(new LRUMap(2500));

	// 更新完成的对象容器
	public static Map<String, Long> entity_updated_completed = Collections.synchronizedMap(new LRUMap(2500));

	// 删除完成的对象容器
	public static Map<String, Long> entity_deleted_completed = Collections.synchronizedMap(new LRUMap(2500));

	/**
	 * =========================== List
	 * ============================================================
	 */
	// 保存有新对象的list
	public static Map<String, Long> list_add_new_object = Collections.synchronizedMap(new LRUMap(2500));
	// 保存List有就对象
	public static Map<String, Long> list_remove_old_object = Collections.synchronizedMap(new LRUMap(2500));

	/**
	 * =========================== Map
	 * ============================================================
	 */
	// 删除完成的对象容器
	public static Map<String, Long> mapping_changed_completed = Collections.synchronizedMap(new LRUMap(5000));
	// 保存有新对象的Mapping
	public static Map<String, Long> mapping_add_new_object = Collections.synchronizedMap(new LRUMap(2500));
	/**
	 * =========================== List Load
	 * ============================================================
	 */
	// 记录因对象的 C/R/D ,导致的发生变化的List
	public static ConcurrentHashMap<String, Long> lsChangeInfo = new ConcurrentHashMap<String, Long>();

	private static DaoRecord instance = null;

	public static DaoRecord getInstance() {
		if (instance == null) {
			synchronized (DaoRecord.class) {
				if (instance == null) {
					instance = new DaoRecord();
				}
				return instance;
			}
		} else {
			return instance;
		}
	}

	private DaoRecord() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * ===========================
	 * Object============================================================
	 */
	/**
	 * 记录某个对象，近期刚被保存过
	 */
	public void NotifyEntitySaveComplete(String entityName, String identity) {
		if (StringUtils.isBlank(entityName) || StringUtils.isBlank(identity)) {
			logger.error(" listName and listKey is null or empty");
			return;
		}
		String key = entityName + "_" + identity;
		this.entity_save_completed.put(key, System.currentTimeMillis());
	}

	/**
	 * 检查是不是刚刚保存的对象
	 */
	public boolean isNowSaved(String entityName, String identity) {
		if (StringUtils.isBlank(entityName) || StringUtils.isBlank(identity)) {
			logger.error(" listName and listKey is null or empty");
			return false;
		}
		String key = entityName + "_" + identity;
		Long saveTime = this.entity_save_completed.get(key);
		return saveTime != null && System.currentTimeMillis() - saveTime <= useful_limt ? true : false;
	}

	/**
	 * 记录某个对象，近期刚被更新过
	 */
	public void NotifyEntityUpdateCompleted(String entityName, String identity) {
		String key = entityName + "_" + identity;
		entity_updated_completed.put(key, System.currentTimeMillis());
	}

	/**
	 * 检查是不是刚刚更新对象
	 */
	public boolean isNowUpdate(String entityName, String identity) {
		if (StringUtils.isBlank(entityName) || StringUtils.isBlank(identity)) {
			logger.error(" entityName and identity is null or empty");
			return false;
		}
		String key = entityName + "_" + identity;
		Long updateTime = this.entity_updated_completed.get(key);
		return updateTime != null && System.currentTimeMillis() - updateTime <= useful_limt ? true : false;
	}

	/**
	 * 记录某个对象，近期刚被删除过
	 */
	public void NotifyEntityDeleteComplated(String entityName, String identity) {
		String key = entityName + "_" + identity;
		entity_deleted_completed.put(key, System.currentTimeMillis());
	}

	/**
	 * 检查是不是刚刚删除对象
	 */
	public boolean isNowDeleted(String entityName, String identity) {
		if (StringUtils.isBlank(entityName) || StringUtils.isBlank(identity)) {
			logger.error(" entityName and identity is null or empty");
			return false;
		}
		String key = entityName + "_" + identity;
		Long deletedTime = this.entity_deleted_completed.get(key);
		return deletedTime != null && System.currentTimeMillis() - deletedTime <= useful_limt ? true : false;
	}

	/**
	 * =========================== List
	 * ============================================================
	 */
	/**
	 * 通知List有新的对象添加
	 */
	public void NotifyListAddNewObject(LsCacheInfo lsInfo) {
		list_add_new_object.put(lsInfo.getKey(), System.currentTimeMillis());
	}

	/**
	 * 有新的对象刚刚添加到List中吗？
	 */
	public boolean hasNewObjectAddToList(LsCacheInfo lsInfo) {
		Long saveTime = list_add_new_object.get(lsInfo.getKey());
		return saveTime != null && System.currentTimeMillis() - saveTime >= useful_limt ? true : false;
	}

	/**
	 * 通知List有旧的对象删除
	 */
	public void NotifyListRemoveObject(LsCacheInfo lsInfo) {
		list_remove_old_object.put(lsInfo.getKey(), System.currentTimeMillis());
	}

	/**
	 * 有新的对象刚刚添加到List中吗？
	 */
	public boolean hasObjectRemoveFromList(LsCacheInfo lsInfo) {
		Long saveTime = this.list_remove_old_object.get(lsInfo.getKey());
		return saveTime != null && System.currentTimeMillis() - saveTime >= useful_limt ? true : false;
	}

	/**
	 * =========================== Map
	 * ============================================================
	 */
	// Mapping 修改, 添加对象， 删除对象， 修改对象都会引起Mapping的变动
	public void NotifyMappingChangeComplated(MapInfo info) {
		mapping_changed_completed.put(info.getKey(), System.currentTimeMillis());
	}

	public boolean isMappingChange(MapInfo info) {
		Long changeTime = this.mapping_changed_completed.get(info.getKey());
		return changeTime != null && System.currentTimeMillis() - changeTime <= useful_limt ? true : false;
	}

	/**
	 * 通知List有新的对象添加
	 * 
	 * @param mappingName
	 * @param params
	 */
	public void NotifyMappingAddNewObject(MapInfo mapInfo) {
		mapping_add_new_object.put(mapInfo.getKey(), System.currentTimeMillis());
	}

	/**
	 * 有新的对象刚刚添加到List中吗？
	 */
	public boolean hasNewObjectAddToMapping(MapInfo info) {
		Long saveTime = mapping_add_new_object.get(info.getKey());
		return saveTime != null && System.currentTimeMillis() - saveTime >= useful_limt ? true : false;
	}

	/**
	 * =========================== List Load
	 * ============================================================
	 */

	// 用于判断Count的加载
	public boolean isCntListLoading(LsCacheInfo lsInfo) {
		return lsChangeInfo.containsKey(lsInfo.getListCntKey());
	}

	public Long checkCntListLoading(LsCacheInfo lsInfo) throws Exception {
		Long lockedValue = null;
		Long currTime = Long.valueOf(System.currentTimeMillis());
		Long oldTime = lsChangeInfo.putIfAbsent(lsInfo.getListCntKey(), currTime);
		if (null == oldTime) {
			lockedValue = currTime;
		} else {
			if (currTime.longValue() - oldTime.longValue() >= maxSpan) {
				if (lsChangeInfo.replace(lsInfo.getListCntKey(), oldTime, currTime)) {
					lockedValue = currTime;
				}
			}
		}
		return lockedValue;
	}

	public boolean endCntListLoading(LsCacheInfo lsInfo, Object value) throws Exception {
		return lsChangeInfo.remove(lsInfo.getListCntKey(), value);
	}

	public boolean isListLoading(LsCacheInfo lsInfo) {
		return lsChangeInfo.containsKey(lsInfo.getKey());
	}

	public Long checkListLoading(LsCacheInfo lsInfo) throws Exception {
		Long lockedValue = null;
		Long currTime = Long.valueOf(System.currentTimeMillis());
		Long oldTime = lsChangeInfo.putIfAbsent(lsInfo.getKey(), currTime);
		if (null == oldTime) {
			lockedValue = currTime;
		} else {
			if (currTime.longValue() - oldTime.longValue() >= maxSpan) {
				if (lsChangeInfo.replace(lsInfo.getKey(), oldTime, currTime)) {
					lockedValue = currTime;
				}
			}
		}
		return lockedValue;
	}

	public boolean endListLoading(LsCacheInfo lsInfo, Object value) throws Exception {
		return lsChangeInfo.remove(lsInfo.getKey(), value);
	}

}
