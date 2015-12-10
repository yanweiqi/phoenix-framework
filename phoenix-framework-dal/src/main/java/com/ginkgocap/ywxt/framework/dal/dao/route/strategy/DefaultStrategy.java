package com.ginkgocap.ywxt.framework.dal.dao.route.strategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.DaoRecord;
import com.ginkgocap.ywxt.framework.dal.dao.config.GroupConfig;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.GroupHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.DbStrategyItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.PatternItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.group.GroupItem;
import com.ginkgocap.ywxt.framework.dal.dao.exception.StrategyException;
import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.MapInfo;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:36
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DefaultStrategy implements IStrategy {
    private static Logger logger = Logger.getLogger(DefaultStrategy.class);
    private static ThreadLocal<Integer> groupIndex = new ThreadLocal<Integer>();
    private static ThreadLocal<CRC32> crc32Thread = new ThreadLocal<CRC32>();


    public Object ListShardingStrategy(String listName, Object account, int rw) throws StrategyException {
        // 找数据库策略，尽量找到，如果找不到就用默认的数据库策略
        DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByListName(listName);
        if (dbStrategyItem == null) {
            dbStrategyItem = DaoHelper.getDefaultDbStrategyItem();
        }

        if (dbStrategyItem != null) {
            return getReadDs(dbStrategyItem, account);
        } else {
            throw new StrategyException("don't find the DbStrategy!");
        }
    }

    public Object ObjectShardingStrategy(Class clazz, Object account, int rw) throws StrategyException {
        // 找数据库策略，尽量找到，如果找不到就用默认的数据库策略
        DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByClass(clazz);
        if (dbStrategyItem == null) {
            dbStrategyItem = DaoHelper.getDefaultDbStrategyItem();
        }

        if (dbStrategyItem != null) {
            switch (rw) {
            case IStrategy.STRATEGY_R:
                return getReadDs(dbStrategyItem, account);
            case IStrategy.STRATEGY_W:
                return getWriteDs(dbStrategyItem, account);
            default:
                break;
            }
        } else {
            throw new StrategyException("don't find the DbStrategy!");
        }
        return null;
    }

    /**
     * 给出读的Datasource名字
     * 
     * @param dbStrategyItem
     * @param account
     * @return
     */
    private String getReadDs(DbStrategyItem dbStrategyItem, Object account) throws StrategyException {
    	Object temp_account = NumberUtils.isDigits(ObjectUtils.toString(account)) && dbStrategyItem.isUseCRC32() ? getCRC32(account) : account;
        Map<String, PatternItem> patternMap = dbStrategyItem.getPatternItemMap();
        PatternItem patternItem = findPatternItem(patternMap, temp_account);
        return getReadDsByPatternItem(patternItem);
    }

    private String getReadDsByPatternItem(PatternItem patternItem) throws StrategyException {
        String ds = null;
        if (patternItem != null) {
            String groupName = patternItem.getGroup();
            GroupItem groupItem = GroupHelper.getGroupItem(groupName);
            if (groupItem != null) {
                String dsSalves = groupItem.getSlave();
                if (StringUtils.isEmpty(dsSalves)) { // 该找Master了，没有Savle
                    ds = groupItem.getMaster();
                } else {
                    String[] dss = StringUtils.splitPreserveAllTokens(dsSalves, GroupConfig.DASNAME_SEPARATOR);
                    if (!ArrayUtils.isEmpty(dss)) {
                        // logger.info("Please create poll Strategy");
                        ds = dss[RandomUtils.nextInt(dss.length)];
                    }
                }
            }
        }
        if (StringUtils.isEmpty(ds)) {
            throw new StrategyException("don't find the read datasources!");
        } else {
            return ds;
        }
    }

    /**
     * 给出写的Datasource名字
     * 
     * @param dbStrategyItem
     * @param account
     * @return
     */
    private String getWriteDs(DbStrategyItem dbStrategyItem, Object account) throws StrategyException {
    	Object temp_account = NumberUtils.isDigits(ObjectUtils.toString(account)) && dbStrategyItem.isUseCRC32() ? getCRC32(account) : account;
        Map<String, PatternItem> patternMap = dbStrategyItem.getPatternItemMap();
        PatternItem patternItem = findPatternItem(patternMap, temp_account);
        return getWriteDsByPatternItem(patternItem);
    }

    private String getWriteDsByPatternItem(PatternItem patternItem) throws StrategyException {
        // 2.从找到的patternItem中选择数据源，比如savle, master, 如果没有salve就用master
        String master = null;
        if (patternItem != null) {
            String groupName = patternItem.getGroup();
            GroupItem groupItem = GroupHelper.getGroupItem(groupName);
            if (groupItem != null) {
                master = groupItem.getMaster();
            }
        }
        if (StringUtils.isEmpty(master)) {
            throw new StrategyException("don't find the write datasources!");
        } else {
            return master;
        }
    }

    /**
     * 想找到想用的patternItem
     * 
     * @param patternMap
     * @param account
     * @return
     */
    private PatternItem findPatternItem(Map<String, PatternItem> patternMap, Object account) {
        PatternItem patternItem = null;
        if (MapUtils.isNotEmpty(patternMap)) {
            Collection<PatternItem> patternItems = patternMap.values();
            if (patternMap.size() == 1 || null == account) { // account
                // 是null找第一个
                patternItem = patternItems.iterator().next();
            } else {
                for (Iterator iterator = patternItems.iterator(); iterator.hasNext();) {
                    PatternItem item = (PatternItem) iterator.next();
                    if (item != null) {
                        String value = item.getValue();
                        if (StringUtils.isNotEmpty(value)) {
                            Pattern pattern = Pattern.compile(value);
                            if (pattern.matcher(ObjectUtils.toString(account)).find()) { // 找到了就退出
                                patternItem = item;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return patternItem;
    }

    public Object MapShardingStrategy(String mapName, Object account, int rw) throws StrategyException {
        // 找数据库策略，尽量找到，如果找不到就用默认的数据库策略
        DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByMapName(mapName);
        if (dbStrategyItem == null) {
            dbStrategyItem = DaoHelper.getDefaultDbStrategyItem();
        }
        String dsname = null;
        if (dbStrategyItem != null) {
            dsname = getReadDs(dbStrategyItem, account);
        } else {
            throw new StrategyException("don't find the DbStrategy!");
        }
        return dsname;
    }

    public boolean isReadWrite(Class clazz, Object account) throws StrategyException {
        DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByClass(clazz);
        if (dbStrategyItem == null) {
            dbStrategyItem = DaoHelper.getDefaultDbStrategyItem();
        }

        if (dbStrategyItem != null) {
            Map<String, PatternItem> patternMap = dbStrategyItem.getPatternItemMap();
            PatternItem patternItem = findPatternItem(patternMap, account);
            if (patternItem != null) {
                String groupName = patternItem.getGroup();
                GroupItem groupItem = GroupHelper.getGroupItem(groupName);
                if (groupItem != null && null != groupItem.getSlave()) {
                    return groupItem.getMaster() != groupItem.getSlave();
                }
            }
        }
        return false;
    }

    public Object NextListShardingStrategy(String listName, int rw) throws StrategyException {
        // 找数据库策略，尽量找到，如果找不到就用默认的数据库策略
        Integer gIndex = groupIndex.get();
        gIndex = gIndex == null ? 0 : gIndex;
        String ds = null;
        DbStrategyItem dbStrategyItem = DaoHelper.getDbStrategyItemByListName(listName);
        if (dbStrategyItem == null) {
            dbStrategyItem = DaoHelper.getDefaultDbStrategyItem();
        }

        if (dbStrategyItem != null) {
            List<PatternItem> patternList = dbStrategyItem.getPatternItemList();
            if (patternList != null && patternList.size() > gIndex) {
                PatternItem patternItem = patternList.get(gIndex);
                gIndex++;
                groupIndex.set(gIndex);
                ds = getReadDsByPatternItem(patternItem);
            } else {
                groupIndex.remove();
            }
        }
        return ds;

    }


    public Object ListShardingStrategy(String listName, Object account, Object[] params, int rw)
            throws StrategyException {
        int temp_rw = rw == IStrategy.STRATEGY_W ? rw : isMasterForList(listName, params) ? IStrategy.STRATEGY_W : rw;
        return this.ListShardingStrategy(listName, account, temp_rw);
    }


    public Object MapShardingStrategy(String mapingName, Object account, Object[] params, int rw)
            throws StrategyException {
        int temp_rw = rw == IStrategy.STRATEGY_W ? rw : isMasterForMap(mapingName, params) ? IStrategy.STRATEGY_W : rw;
        return this.MapShardingStrategy(mapingName, account, temp_rw);
    }


    public Object ObjectShardingStrategy(Class clazz, Object account, Object id, int rw) throws StrategyException {
        int temp_rw = rw == IStrategy.STRATEGY_W ? rw : isMasterForObject(clazz, id) ? IStrategy.STRATEGY_W : rw;
        return this.ObjectShardingStrategy(clazz, account, temp_rw);
    }

    private boolean isMasterForList(String listName, Object[] params) {
        DaoRecord record = DaoRecord.getInstance();
        LsCacheInfo lsInfo = new LsCacheInfo(listName,params);
        return record.hasNewObjectAddToList(lsInfo) || record.hasObjectRemoveFromList(lsInfo);
    }

    private boolean isMasterForObject(Class clazz, Object id) {
        DaoRecord record = DaoRecord.getInstance();
        String entityName = clazz.getName();
        String identity = ObjectUtils.toString(id);
        return record.isNowDeleted(entityName, identity) || record.isNowSaved(entityName, identity)
                || record.isNowUpdate(entityName, identity);
    }

    private boolean isMasterForMap(String mappingName, Object[] params) {
        DaoRecord record = DaoRecord.getInstance();
        MapInfo info = new MapInfo(mappingName,params);
        return record.isMappingChange(info); // 修改, 添加对象， 删除对象
    }
    
    private static Object getCRC32(Object account) {
    	CRC32 crc32 = crc32Thread.get();
    	if (crc32 == null) {
    		crc32 = new CRC32();
    		crc32Thread.set(crc32);
    	} else {
    		crc32.reset();
    	}
		crc32.update(account.toString().getBytes());
		return crc32.getValue();
    }
}
