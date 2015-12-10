package com.ginkgocap.ywxt.framework.dal.dao.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.MaxIdHolder;
import com.ginkgocap.ywxt.framework.dal.cache.NullObjectContent;
import com.ginkgocap.ywxt.framework.dal.dao.DaoRecord;
import com.ginkgocap.ywxt.framework.dal.dao.Listener;
import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;
import com.ginkgocap.ywxt.framework.dal.dao.util.CacheHelper;
import com.ginkgocap.ywxt.framework.dal.dao.util.Constants;
import com.ginkgocap.ywxt.framework.dal.dao.util.NullObjectHelper;
import com.ginkgocap.ywxt.framework.dal.dao.util.ObjectUtil;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:02
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DefaultListener implements Listener {
    private long wait_time = 3 * 1000; // 3 秒
    private static Logger logger = Logger.getLogger(DefaultListener.class);
    private static final Integer LISTWAITTIME = 20;
    DaoRecord record = DaoRecord.getInstance();

    public void onSaveBegin(Object account_id, Object entity) throws Exception {
    }

    public void onSaveCompleted(Object account_id, Class cls, Serializable id, Object entity) throws Exception {
        // 重要逻辑一. 设定【对象】在加载时的【路由策略】
        record.NotifyEntitySaveComplete(entity.getClass().getName(), ObjectUtils.toString(id));

        // 重要逻辑二. 更新该对象所对应的缓存
        CacheHelper.save(cls, id, entity);

        // 重要逻辑三. 所有List的信息的维护
        List<LsCacheInfo> lsCacheInfos = ObjectUtil.getLsInfoList(entity);
        if (CollectionUtils.isNotEmpty(lsCacheInfos)) {
            for (LsCacheInfo lsInfo : lsCacheInfos) {
                Cache cache = CacheHelper.getListCache(lsInfo.getRegion());
                if (null != cache) {
                    // Logic 1: List Count的维护
                    if (waitCntListLoadIng(lsInfo)) {
                        cache.remove(lsInfo.getListCntKey());
                    } else {
                        cache.incr(lsInfo.getListCntKey(), 1);
                    }

                    if (lsInfo.isUpdate() || waitListLoadIng(lsInfo)) {
                        cache.remove(lsInfo.getKey());
                        cache.remove(lsInfo.getListCntKey());
                    } else {
                        cache.ladd(lsInfo.getKey(), id + "");
                    }

                    // 用于确定【List】在加载时的【路由策略】
                    record.NotifyListAddNewObject(lsInfo);
                }

            }
        }
            // 重要逻辑四: 设定【Map】在加载时的【路由策略】
            List<MapInfo> mapInfos = ObjectUtil.getMapInfoList(entity);
            if (CollectionUtils.isNotEmpty(mapInfos)) {
                for (MapInfo mapInfo : mapInfos) {
                    record.NotifyMappingAddNewObject(mapInfo);
                }
            }
            // 重要逻辑五：将对象所对应的Map从NullObject中移除
            NullObjectHelper.removeNullMapsOfObject(entity);
            // 重要逻辑六：将对象从NullObject中移除
            NullObjectContent.remove(Constants.NullObjectPrefix + cls.getName() + id);
            // 重要逻辑七：设置最大ID
            MaxIdHolder.setMaxId(Constants.MaxIdPrefix + cls.getName(), Long.parseLong(id + ""));

    }

    public void onSaveEnd(Object account_id, Serializable id, Object entity) throws Exception {
    }

    public void onSaveError(Object account_id, Serializable id, Object entity) {
    }

    public void onBatchSaveCompleted(Object account_id, Class cls, Map idObjMap) throws Exception {
        Long maxObjectId = 0l;
        List objList = new ArrayList();

        Iterator<Entry<Serializable, Object>> iter = idObjMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Entry<Serializable, Object> entry = iter.next();
            Serializable id = entry.getKey();
            Object obj = entry.getValue();
            objList.add(obj);
            // 重要逻辑一: 将对象保存在缓存中
            CacheHelper.save(cls, id, obj);
            // 重要逻辑二. 设定【对象】在加载时的【路由策略】
            record.NotifyEntitySaveComplete(cls.getName(), ObjectUtils.toString(id));
            // 重要逻辑三.将对象所对应的所有List信息，从缓存中删除，并设定相应【路由策略】
            List<LsCacheInfo> lsCacheInfos = ObjectUtil.getLsInfoList(obj);
            if (CollectionUtils.isNotEmpty(lsCacheInfos)) {
                for (LsCacheInfo lsInfo : lsCacheInfos) {
                    Cache cache = CacheHelper.getListCache(lsInfo.getRegion());
                    if (null != cache) {
                        cache.remove(lsInfo.getListCntKey());
                        cache.remove(lsInfo.getKey());
                    }
                    // 用于确定【List】在加载时的【路由策略】
                    record.NotifyListAddNewObject(lsInfo);
                }
            }
            // 重要逻辑四: 设定【Map】在加载时的【路由策略】
            List<MapInfo> mapInfos = ObjectUtil.getMapInfoList(obj);
            if (CollectionUtils.isNotEmpty(mapInfos)) {
                for (MapInfo mapInfo : mapInfos) {
                    record.NotifyMappingAddNewObject(mapInfo);
                }
            }
            // 重要逻辑五 ：将对象所对应的所有Map，从Map的NullObject中移除
            NullObjectHelper.removeNullMapsOfObject(obj);
            // 重要逻辑六: 将对象从 Object 所对应的 NullObject中移除
            NullObjectContent.remove(Constants.NullObjectPrefix + obj.getClass().getName() + id);
            if (new Long("" + id).longValue() > maxObjectId.longValue()) {
                maxObjectId = new Long("" + id);
            }
        }
        // 重要逻辑七：设置最大ID
        if (maxObjectId > 0) {
            MaxIdHolder.setMaxId(Constants.MaxIdPrefix + objList.get(0).getClass().getName(), maxObjectId);
        }
    }

    public void onUpdateBegin(Object account_id, Serializable id, Object obj, Object oldObj) throws Exception {
    }

    public void onUpdateCompleted(Object account_id, Serializable id, Object obj, Object oldObj) throws Exception {
        // 重要逻辑一. 设定【对象】在加载时的【路由策略】
        record.NotifyEntityUpdateCompleted(obj.getClass().getName(), id.toString());

        // 重要逻辑二. List的处理
        List<LsCacheInfo> allInfolist = ObjectUtil.getLsInfoList(obj);
        Map<String, LsCacheInfo> objInfoMap = new HashMap<String, LsCacheInfo>();
        for (LsCacheInfo lsInfo : allInfolist) {
            String region = lsInfo.getRegion();
            Cache cache = CacheHelper.getListCache(region);
            if (null != cache) {
                objInfoMap.put(region, lsInfo);
            }
        }

        List<LsCacheInfo> oldAllInfolist = ObjectUtil.getLsInfoList(oldObj);
        Map<String, LsCacheInfo> oldInfoMap = new HashMap<String, LsCacheInfo>();
        for (LsCacheInfo lsInfo : oldAllInfolist) {
            String region = lsInfo.getRegion();
            Cache cache = CacheHelper.getListCache(region);
            if (null != cache) {
                oldInfoMap.put(region, lsInfo);
            }
        }

//        Iterator<String> iter = oldInfoMap.keySet().iterator();
        Iterator<Entry<String, LsCacheInfo>> iter = oldInfoMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Entry<String, LsCacheInfo> entry = iter.next();
            String region = entry.getKey();
            Cache cache = CacheHelper.getListCache(region);

            LsCacheInfo oldLsInfo = entry.getValue();
            LsCacheInfo newLsInfo = objInfoMap.get(region);

            // Key
            String oldKey = oldLsInfo.getKey();
            String newKey = newLsInfo.getKey();

            // Order by Property
            String oldOrderByKey = oldLsInfo.getOrderByKey();
            String newOrderByKey = newLsInfo.getOrderByKey();
            // Id
            Object oldValueOfLs = ObjectUtil.getObjectValueOfList(oldObj, oldLsInfo);
            Object newValueOfLs = ObjectUtil.getObjectValueOfList(obj, newLsInfo);

            // 场景 一 ：Object所对应的List的Key值发生变化的情况,此处涉及 2 个 List
            if (!oldKey.equalsIgnoreCase(newKey) ||
                    // 场景 二 ：Object 存放在 List中的值发生了变化
                    !oldValueOfLs.equals(newValueOfLs) ||
                    // 场景 三 ：Object所对应的List 的Key值未发生变化，但用于排序的属性
                    // 发生了变化，此时List做失效处理,此处涉及 1 个List
                    (null != oldOrderByKey && oldOrderByKey.length() > 0 && !oldOrderByKey
                            .equalsIgnoreCase(newOrderByKey))) {

                waitCntListLoadIng(oldLsInfo);
                cache.remove(oldLsInfo.getListCntKey());

                waitListLoadIng(oldLsInfo);
                cache.remove(oldLsInfo.getKey());


                // 用于确定【List】在加载时的【路由策略】
                record.NotifyListAddNewObject(oldLsInfo);

                if (!oldKey.equalsIgnoreCase(newKey)) {
                    waitCntListLoadIng(newLsInfo);
                    cache.remove(newLsInfo.getListCntKey());

                    waitListLoadIng(newLsInfo);
                    cache.remove(newLsInfo.getKey());

                    // 用于确定【List】在加载时的【路由策略】
                    record.NotifyListAddNewObject(newLsInfo);
                }
            }
        }// while

        // 重要逻辑三.old Object所对应 Map的处理
        List<MapInfo> oldMaps = ObjectUtil.getMapInfoList(oldObj);
        for (MapInfo info : oldMaps) {
            Cache cache = CacheHelper.getListCache(info.getRegion());
            if (null == cache) {
                continue;
            }
            record.NotifyMappingAddNewObject(info);
            cache.remove(info.getKey());
        }

        // 重要逻辑四. Object所对应 Map的处理
        List<MapInfo> newMaps = ObjectUtil.getMapInfoList(obj);
        for (MapInfo info : newMaps) {
            Cache cache = CacheHelper.getListCache(info.getRegion());
            if (null == cache) {
                continue;
            }
            record.NotifyMappingAddNewObject(info);
            cache.put(info.getKey(), info.getValue());
        }

        // 重要逻辑五 : 将对象所对应的Map从NullObject中移除
        NullObjectHelper.removeNullMapsOfObject(obj);

        // 重要逻辑六：更新该对象所对应的缓存
        CacheHelper.update(obj.getClass().getName(), id + "", obj);
    }

    public void onUpdateEnd(Object account_id, Serializable id, Object obj, Object oldObj) throws Exception {
    }

    public void onUpdateError(Object account_id, Serializable id, Object obj, Object oldObj) {

    }

    public void onDeleteCompleted(Object account_id, Serializable id, Object obj) throws Exception {
        // 重要逻辑一. 设定【对象】在加载时的【路由策略】
        record.NotifyEntityDeleteComplated(obj.getClass().getName(), id.toString());

        // 重要逻辑二: 更新对象所对应的List
        List<LsCacheInfo> lsCacheInfos = ObjectUtil.getLsInfoList(obj);
        if (CollectionUtils.isNotEmpty(lsCacheInfos)) {
            for (LsCacheInfo lsInfo : lsCacheInfos) {
                Cache cache = CacheHelper.getListCache(lsInfo.getRegion());
                if (null != cache) {
                    waitCntListLoadIng(lsInfo);
                    cache.remove(lsInfo.getListCntKey());
                    waitListLoadIng(lsInfo);
                    cache.remove(lsInfo.getKey());
                    // 用于确定【List】在加载时的【路由策略】
                    record.NotifyListRemoveObject(lsInfo);
                }
            }
        }

        // 重要逻辑三: 更新对象所对应的Map
        List<MapInfo> oldMaps = ObjectUtil.getMapInfoList(obj);
        for (MapInfo info : oldMaps) {
            Cache cache = CacheHelper.getListCache(info.getRegion());
            if (null == cache) {
                continue;
            }
            cache.remove(info.getKey());
            record.NotifyMappingChangeComplated(info);
        }

        // 重要逻辑四：更新该对象所对应的缓存
        CacheHelper.delete(obj.getClass().getName(), id + "");
    }

    public void onDeleteBegin(Object account_id, Serializable id, Object obj) throws Exception {
    }

    public void onDeleteEnd(Object account_id, Serializable id, Object obj, Object oldObject) throws Exception {
    }

    public void onDeleteError(Object account_id, Serializable id, Object obj) {

    }

    public void onBatchSaveBegin(Object account_id, List objList) throws Exception {
        // TODO Auto-generated method stub

    }

    public void onBatchSaveEnd(Object account_id, List objList) {
        // TODO Auto-generated method stub

    }

    private boolean waitCntListLoadIng(LsCacheInfo lsCacheInfo) {
        long currentTime = System.currentTimeMillis();
        boolean waitSucess = false;
        while (record.isCntListLoading(lsCacheInfo)) {
            if (System.currentTimeMillis() - currentTime > wait_time) {
                logger.info("check loading is timeout");
                break;
            }
            waitSucess = true;
            try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        return waitSucess;
    }

    /**
     * 等待List转载完成，除非超时
     */
    private boolean waitListLoadIng(LsCacheInfo lsCacheInfo) {
        long currentTime = System.currentTimeMillis();
        boolean waitSucess = false;
        while (record.isListLoading(lsCacheInfo)) {
            if (System.currentTimeMillis() - currentTime > wait_time) {
                logger.info("check loading is timeout");
                break;
            }
            waitSucess = true;
        }
        return waitSucess;
    }

}
