/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ginkgocap.ywxt.framework.dal.dao.config.model.group.DataSourceItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.group.GroupItem;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:41
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class GroupConfig {
    private static String CONFIG_FILE = "/group.xml";
    private static Logger logger = Logger.getLogger(GroupConfig.class);
    private Map< Object, Object > dataSourceItemMap = new HashMap< Object, Object >();
    // private Map<String, List<String>> annotationClassMap = new
    // HashMap<String, List<String>>(); // 数据员对应的Class

    private Map< String, GroupItem > groupItemMap = new HashMap< String, GroupItem >();
    private Map< String, List< DataSource >> slaveDatasourceMap = new HashMap< String, List< DataSource >>();
    private static GroupConfig config;
    public static String DASNAME_SEPARATOR = ","; // Datasource 名字的分隔符

    private GroupConfig() {
        try {
            this.init(getClass().getResourceAsStream(CONFIG_FILE));
            this.datasourceAssign();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace(System.out);
            } else {
                logger.error("init config file " + CONFIG_FILE + " error: " + e.getMessage());
            }
        }
    }

    public static GroupConfig getInstance() {
        if (config == null) {
            synchronized (GroupConfig.class) {
                if (config == null) {
                    config = new GroupConfig();
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            config.close();
                        }
                    });
                }
            }
        }
        return config;
    }

    public void close() {
        if (MapUtils.isNotEmpty(dataSourceItemMap)) {
            Collection<Object> dataSources = dataSourceItemMap.values();
            for (Iterator iterator = dataSources.iterator(); iterator.hasNext();) {
                DataSource dataSource = (DataSource) iterator.next();
                if (dataSource != null) {
                    try {
                        ((BasicDataSource)dataSource).close();
                        logger.info("close datasource complete " + ((BasicDataSource)dataSource).getUrl());
                    } catch (SQLException e) {
                        ;
                    }
                }
                
            }
        }
    }
    private void init(InputStream groupConfigFile) {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("root", ArrayList.class);

        // 初始化Datasources
        digester.addObjectCreate("root/datasources", ArrayList.class.getName());
        digester.addSetNext("root/datasources", "add");
        // digester.addSetProperties("root/datasources");
        digester.addObjectCreate("root/datasources/ds", DataSourceItem.class.getName());
        digester.addSetProperties("root/datasources/ds");
        digester.addSetNext("root/datasources/ds", "add");

        // 初始还Group
        digester.addObjectCreate("root/groups", ArrayList.class.getName());
        digester.addSetNext("root/groups", "add");
        digester.addObjectCreate("root/groups/group", GroupItem.class.getName());
        digester.addSetProperties("root/groups/group");
        digester.addSetNext("root/groups/group", "add");

        try {
            Object root = digester.parse(groupConfigFile);
            if (root != null && root instanceof ArrayList) {
                List< List > list = (List) root;
                for (List list_item : list) {
                    if (CollectionUtils.isNotEmpty(list_item)) {
                        for (Iterator iterator = list_item.iterator(); iterator.hasNext();) {
                            Object entityObject = (Object) iterator.next();
                            if (entityObject != null) {
                                if (entityObject instanceof GroupItem) {
                                    addGroupItem((GroupItem) entityObject);
                                } else if (entityObject instanceof DataSourceItem) {
                                    addDataSourceItem((DataSourceItem) entityObject);
                                }
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace(System.err);
            } else {
                logger.error("init error: " + e.getMessage());
            }
        } catch (SAXException e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace(System.err);
            } else {
                logger.error("init parse fail : " + e.getMessage());
            }
        }

    }

    /**
     * 
     */
    private void datasourceAssign() {
        if (MapUtils.isNotEmpty(groupItemMap)) {
            Collection< GroupItem > groupitems = groupItemMap.values();
            for (Iterator iterator = groupitems.iterator(); iterator.hasNext();) {
                GroupItem groupItem = (GroupItem) iterator.next();
                List< DataSource > dataSources = new ArrayList< DataSource >();
                String dataSourceName = groupItem.getSlave();
                if (StringUtils.isEmpty(dataSourceName)) {
                    logger.info(StringUtils.defaultIfEmpty(groupItem.getName(), "")
                            + " slave name don't set! please check group.xml file");
                } else {
                    String[] names = StringUtils.splitPreserveAllTokens(dataSourceName, GroupConfig.DASNAME_SEPARATOR);
                    for (int i = 0; i < names.length; i++) {
                        if (StringUtils.isNotEmpty(names[i])) {
                            DataSource dataSource = getDataSource(names[i]);
                            if (dataSource != null) {
                                dataSources.add(dataSource);
                            } else {
                                logger.info(StringUtils.defaultIfEmpty(names[i], "") + " don't exist datasources");
                            }
                        }
                    }
                    this.slaveDatasourceMap.put(groupItem.getName(), dataSources);
                }
            }
        }
    }

    /**
     * 
     * @param dataSourceItem
     */
    private void addDataSourceItem(DataSourceItem dataSourceItem) {
        if (dataSourceItem != null && StringUtils.isNotEmpty(dataSourceItem.getName())) {
            if (!dataSourceItemMap.containsKey(dataSourceItem.getName())) {
                // create dataSource
                BasicDataSource ds = new BasicDataSource();
                ds.setDriverClassName(dataSourceItem.getDriverClassName());
                ds.setUsername(dataSourceItem.getUsername());
                ds.setPassword(dataSourceItem.getPassword());
                ds.setUrl(dataSourceItem.getUrl());
                // set pool property
                ds.setMaxActive(NumberUtils.toInt(dataSourceItem.getMaxActive(), GenericObjectPool.DEFAULT_MAX_ACTIVE));
                ds.setMaxIdle(NumberUtils.toInt(dataSourceItem.getMaxIdle(), GenericObjectPool.DEFAULT_MAX_IDLE));
                ds.setMaxWait(NumberUtils.toLong(dataSourceItem.getMaxWait(), GenericObjectPool.DEFAULT_MAX_WAIT));
                //上线去掉
                ds.setTestOnBorrow(dataSourceItem.isTestOnBorrow());
                ds.setTestOnReturn(dataSourceItem.isTestOnReturn());
                ds.setTestWhileIdle(dataSourceItem.isTestWhileIdle());
                ds.setValidationQuery("select 1");
                //结束
                dataSourceItemMap.put(dataSourceItem.getName(), ds);
                if (logger.isDebugEnabled()) {
                    logger.debug("datasource " + dataSourceItem.getName() + " created ok");
                }
            } else {
                logger.error("same <" + dataSourceItem.getName() + "> name datasource exist!");
            }
        } else {
            logger.error("DataSourceItem must has value and name");
        }
    }

    /**
     * 
     * @param groupItem
     */
    private void addGroupItem(GroupItem groupItem) {
        if (groupItem != null && StringUtils.isNotEmpty(groupItem.getName())) {
            if (!groupItemMap.containsKey(groupItem.getName())) {
                groupItemMap.put(groupItem.getName(), groupItem);
                if (logger.isDebugEnabled()) {
                    logger.debug(groupItem.getName() + " parser ok.");
                }
            } else {
                logger.error("same <" + groupItem.getName() + ">name GroupItem exist!");
            }
        } else {
            logger.error("groupitem must has value and has name");
        }
    }

    /**
     * 
     * @param dataSourceName
     * @return
     */
    public DataSource getDataSource(String dataSourceName) {
        if (StringUtils.isNotEmpty(dataSourceName)) {
            return (DataSource) this.dataSourceItemMap.get(dataSourceName);
        } else {
            return null;
        }
    }

    /**
     * 
     * @param groupItemName
     * @return
     */
    public GroupItem getGroupItem(String groupItemName) {
        if (StringUtils.isNotEmpty(groupItemName)) {
            return this.groupItemMap.get(groupItemName);
        } else {
            return null;
        }
    }

    /**
     * 给出Salve数据库名的Datasource
     * 
     * @param groupName
     * @return
     */
    public List< DataSource > getSlaveDataSources(String groupName) {
        return this.slaveDatasourceMap.get(groupName);
    }

    public DataSource getMasterDataSource(String groupName) {
        GroupItem groupItem = getGroupItem(groupName);
        DataSource dataSource = null;
        if (groupItem != null) {
            String dataSourceName = groupItem.getMaster();
            if (StringUtils.isEmpty(dataSourceName)) {
                logger.info(StringUtils.defaultIfEmpty(groupItem.getName(), "")
                        + " master name don't set! please check group.xml file");
            } else {
                dataSource = getDataSource(dataSourceName);
            }
        } else {
            logger.info(StringUtils.defaultIfEmpty(groupName, "") + " do'nt exist! please check group.xml file");
        }
        return dataSource;
    }

    // /**
    // * 配置数据源Datasource对应的Class（List）
    // *
    // * @param groupName
    // * @param clazz
    // */
    // public void addAnnotationClass(String groupName, String clazz) {
    // GroupItem groupItem = groupItemMap.get(groupName);
    // if (null == groupItem) {
    // logger.error("don't found groupItem by name: " + groupName);
    // } else {
    // String masterDsName = groupItem.getMaster();
    // String[] salveDsName =
    // StringUtils.splitPreserveAllTokens(groupItem.getSalve(),
    // GroupConfig.DASNAME_SEPARATOR);
    // salveDsName = (String[]) ArrayUtils.add(salveDsName, masterDsName);
    // for (int i = 0; i < salveDsName.length; i++) {
    // if (StringUtils.isNotEmpty(salveDsName[i])) {
    // // annotationClassMap
    // if (dataSourceItemMap.containsKey(salveDsName[i])) { //
    // 确实存在这样的数据源DataSource
    // List<String> list = annotationClassMap.get(salveDsName[i]);
    // if (null == list) {
    // list = new ArrayList<String>();
    // annotationClassMap.put(salveDsName[i], list);
    // }
    // if (!list.contains(clazz)) { //不要放重
    // list.add(clazz);
    // }
    // } else {
    // logger.error(salveDsName[i] + " datasource don't exist!");
    // }
    //
    // }
    // }
    //
    // }
    //
    // }

    // public Map<String, List<String>> getAnnotationClassMap() {
    // return annotationClassMap;
    // }

    public Map< Object, Object > getDataSourceItemMap() {
        return dataSourceItemMap;
    }
}
