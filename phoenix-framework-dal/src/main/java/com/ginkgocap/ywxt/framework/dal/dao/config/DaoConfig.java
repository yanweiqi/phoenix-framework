/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.xml.sax.SAXException;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.GroupHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.DbStrategyItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.IdCenterDsItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ListItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.MapItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ObjectItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.PatternItem;
import com.ginkgocap.ywxt.framework.dal.dao.datasource.DynamicDataSource;



/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:33
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class DaoConfig {
	
    private static String CONFIG_FILE = "/dao.xml";
    
    private static Logger logger = Logger.getLogger(DaoConfig.class);

    private Map<String, ObjectItem> objectMap = new HashMap<String, ObjectItem>();
    private Map<String, ListItem> listItemMap = new HashMap<String, ListItem>();
    private Map<String, MapItem> mapItemMap   = new HashMap<String, MapItem>();

    private DbStrategyItem defaultDbStrategyItem = null; // 却省的DB策略配置
    private AnnotationSessionFactoryBean sessionFactoryBean = null;
    private SessionFactory sessionFactory = null;;
    private static DaoConfig config;
    private IdCenterDsItem idCenterDsItem;

    public static String DASNAME_SEPARATOR = ","; // Datasource 名字的分隔符
    public static String PROPERTY_SEPARATOR = ","; // 属性的风格符号

    // static class SingletonHolder {
    // static DaoConfig instance = new DaoConfig();
    // }
    //
    // public static DaoConfig getInstance() {
    // return SingletonHolder.instance;
    // }
    //
    
    public static DaoConfig getInstance() {
        if (config == null) {
            synchronized (DaoConfig.class) {
                if (config == null) {
                    config = new DaoConfig();
                }
            }
        }
        return config;
    }

    private DaoConfig() {
        try {
            this.initObject(getClass().getResourceAsStream(CONFIG_FILE));
            this.initDefault(getClass().getResourceAsStream(CONFIG_FILE));
            this.initSessionFactory();
            // this.datasourceAssign();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace(System.out);
            } else {
                logger.error("init config file " + CONFIG_FILE + " error: " + e.getMessage());
            }
        }
    }

    /**
     * 解析Object的配置
     * 
     * @param groupConfigFile
     */
    private void initObject(InputStream groupConfigFile) {
        Digester digester = new Digester();
        digester.setValidating(false);

        // 对象
        digester.addObjectCreate("dal/route/", ArrayList.class);
        digester.addObjectCreate("dal/route/object", ObjectItem.class);
        digester.addSetNext("dal/route/object", "add");
        digester.addSetProperties("dal/route/object");

        // 对象的列表
        digester.addObjectCreate("dal/route/object/list", ListItem.class);
        digester.addSetProperties("dal/route/object/list");
        digester.addSetNext("dal/route/object/list", "addListMap");


        // 对象的Map
        digester.addObjectCreate("dal/route/object/map", MapItem.class);
        digester.addSetProperties("dal/route/object/map");
        digester.addSetNext("dal/route/object/map", "addMapMap");

        // Db策略
        digester.addObjectCreate("dal/route/object/dbStrategy", DbStrategyItem.class);
        digester.addSetProperties("dal/route/object/dbStrategy");
        digester.addSetNext("dal/route/object/dbStrategy", "setDbStrategyItem");

        // 策略对应的pattern
        digester.addObjectCreate("dal/route/object/dbStrategy/pattern", PatternItem.class);
        digester.addSetProperties("dal/route/object/dbStrategy/pattern");
        digester.addSetNext("dal/route/object/dbStrategy/pattern", "addPatternItem");

        try {
            Object objectList = digester.parse(groupConfigFile);
            if (objectList != null && objectList instanceof ArrayList) {
                List<ObjectItem> list = (List<ObjectItem>) objectList;
                for (ObjectItem objectItem : list) {
                    addObjectItem(objectItem);
                    listAggregate(objectItem);
                    mapAggregate(objectItem);
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
     * 解析Default的配置
     * 
     * @param groupConfigFile
     */
    private void initDefault(InputStream groupConfigFile) {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("dal/default/", ArrayList.class);
        // DbStrategy对象
        digester.addObjectCreate("dal/default/dbStrategy", DbStrategyItem.class);
        digester.addSetNext("dal/default/dbStrategy", "add");
        digester.addSetProperties("dal/default/dbStrategy");

        digester.addObjectCreate("dal/default/dbStrategy/pattern", PatternItem.class);
        digester.addSetProperties("dal/default/dbStrategy/pattern");
        digester.addSetNext("dal/default/dbStrategy/pattern", "addPatternItem");

        // Idcenter的数据源
        digester.addObjectCreate("dal/default/idCenterDS", IdCenterDsItem.class);
        digester.addSetNext("dal/default/idCenterDS", "add");
        digester.addSetProperties("dal/default/idCenterDS");

        try {
            Object objectList = digester.parse(groupConfigFile);
            if (objectList != null && objectList instanceof ArrayList) {
                List list = (List) objectList;
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    Object defaultObject = iterator.next();
                    if (defaultObject != null) {
                        if (defaultObject instanceof DbStrategyItem) { // Db策略
                            this.defaultDbStrategyItem = (DbStrategyItem) defaultObject;
                            continue;
                        }
                        if (defaultObject instanceof IdCenterDsItem) { // Db策略
                            this.idCenterDsItem = (IdCenterDsItem) defaultObject;
                            continue;
                        }

                        // 可以直接补充完全其它策略
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
     * 把所有的List放到一起
     * 
     * @param objectItem
     */
    private void listAggregate(ObjectItem objectItem) {
        if (objectItem != null && StringUtils.isNotEmpty(objectItem.getName())) {
            Map<String, ListItem> listMap = objectItem.getListMap();
            if (MapUtils.isNotEmpty(listMap)) {
                for (Iterator<ListItem> it = listMap.values().iterator(); it.hasNext();) {
                    ListItem item = it.next();
                    if (listItemMap.containsKey(item.getName())) {
                        logger.error("same <" + item.getName() + "> list exist!");
                    } else {
                        logger.info(item.toString());
                        listItemMap.put(item.getName(), item);
                    }
                }
            }
        }
    }



    /**
     * 把所有的List放到一起
     * 
     * @param objectItem
     */
    private void mapAggregate(ObjectItem objectItem) {
        if (objectItem != null && StringUtils.isNotEmpty(objectItem.getName())) {
            Map<String, MapItem> listMap = objectItem.getMapMap();
            if (MapUtils.isNotEmpty(listMap)) {
                for (Iterator<MapItem> it = listMap.values().iterator(); it.hasNext();) {
                    MapItem item = it.next();
                    if (mapItemMap.containsKey(item.getName())) {
                        logger.error("same <" + item.getName() + "> list exist!");
                    } else {
                        logger.info(item.toString());
                        mapItemMap.put(item.getName(), item);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void initSessionFactory() {
        // 初始化动态Datasource
        DynamicDataSource dds = new DynamicDataSource();
        dds.setTargetDataSources(GroupHelper.getDataSourceMap());
        // dds.setDefaultTargetDataSource(defaultTargetDataSource)
        dds.afterPropertiesSet();

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.generate_statistics", "false");
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", "false");
        hibernateProperties.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        hibernateProperties.setProperty("hibernate.statement_cache.size", "50");
        hibernateProperties.setProperty("hibernate.jdbc.fetch_size", "100");
        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "100");
        hibernateProperties.setProperty("hibernate.jdbc.use_scrollable_resultset", "true");
        hibernateProperties.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
        hibernateProperties.setProperty("hibernate.max_fetch_depth", "3");
        hibernateProperties.setProperty("hibernate.bytecode.use_reflection_optimizer", "true");
        hibernateProperties.setProperty("hibernate.query.substitutions", "true 1, false 0");
        hibernateProperties.setProperty("hibernate.current_session_context_class", "thread");
        hibernateProperties.setProperty("hibernate.temp.use_jdbc_metadata_defaults","false");
        hibernateProperties.setProperty(Environment.DIALECT, "org.hibernate.dialect.MySQLInnoDBDialect");
        if (logger.isDebugEnabled()) {
        	//hibernateProperties.setProperty(Environment.FORMAT_SQL, "true");
        	//hibernateProperties.setProperty(Environment.SHOW_SQL, "true");
        }

        // hcfg.setProperty(Environment.CONNECTION_PROVIDER,
        // DataSourceConnectonProvider.class.getName());

        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setHibernateProperties(hibernateProperties);
        sessionFactoryBean.setDataSource(dds);

        Set<String> objectClassNames = objectMap.keySet();
        Class[] clazzs = new Class[objectClassNames.size()];
        int i = 0;
        for (Iterator iterator = objectClassNames.iterator(); iterator.hasNext();) {
            String className = (String) iterator.next();
            if (StringUtils.isNotEmpty(className)) {
                try {
                    Class clazz = Class.forName(className);
                    clazzs[i] = clazz;
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace(System.err);
                    } else {
                        logger.error("Class.forName fail:" + className);
                    }
                }
            }
            i++;
        }
        sessionFactoryBean.setAnnotatedClasses(clazzs);
        try {
            sessionFactoryBean.afterPropertiesSet();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace(System.err);
            } else {
                logger.error("afterPropertiesSet fail: " + e.getMessage());
            }
        }
        sessionFactory = (SessionFactory) sessionFactoryBean.getObject();

        // sessionFactoryBean.setAnnotatedClasses(annotatedClasses)
    }

    /**
     * objectItem
     * 
     * @param objectItem
     */
    private void addObjectItem(ObjectItem objectItem) {
        if (objectItem != null && StringUtils.isNotEmpty(objectItem.getName())) {
            if (!objectMap.containsKey(objectItem.getName())) {
                // init strategyPropertyMethod
                String className = objectItem.getName();
                Class clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e1) {
                    if (logger.isDebugEnabled()) {
                        e1.printStackTrace(System.err);
                    } else {
                        logger.error("Class " + StringUtils.defaultIfEmpty(className, "") + " don't fond");
                    }
                }
                if (clazz != null) {
                    // Strategy Method
                    Method method = MethodUtils.getAccessibleMethod(clazz, "get"
                            + WordUtils.capitalize(objectItem.getStrategyProperty()), new Class[] {});
                    if (method == null) {
                        logger.error("Class " + className + " method get "
                                + StringUtils.defaultIfEmpty(objectItem.getStrategyProperty(), "") + " don't fond!");
                    } else {
                        objectItem.setStrategyPropertyMethod(method);
                    }

                    // delmark method
                    method = MethodUtils.getAccessibleMethod(clazz, "get"
                            + WordUtils.capitalize(objectItem.getDelProperty()), new Class[] {});

                    if (method == null) {
                        logger.error("Class " + className + " method get "
                                + StringUtils.defaultIfEmpty(objectItem.getDelProperty(), "") + " don't fond!");
                    } else {
                        Method setMethod = MethodUtils.getAccessibleMethod(clazz, "set"
                                + WordUtils.capitalize(objectItem.getDelProperty()), new Class[] { method
                                .getReturnType() });
                        if (setMethod != null) {
                            objectItem.setDelPropertyMethod(setMethod);
                            if (StringUtils.isNotEmpty(objectItem.getDelValue())) {
                                objectItem.setDelValueObject(ConvertUtils.convert(objectItem.getDelValue(), method
                                        .getReturnType()));
                            }
                        } else {
                            logger.error("Class " + className + " method set "
                                    + StringUtils.defaultIfEmpty(objectItem.getDelProperty(), "") + " don't fond!");
                        }
                    }
                }
                objectMap.put(objectItem.getName(), objectItem);

            } else {
                logger.error("same <" + objectItem.getName() + "> name ObjectItem exist!");
            }
        } else {
            logger.error("ObjectItem must has value and name");
        }
    }

    /**
     * 
     * @param objectName
     * @return
     */
    public ObjectItem getObjectItem(String objectName) {
        return objectMap.get(objectName);
    }

    /**
     * 
     * @param listName
     * @return
     */
    public ListItem getListItem(String listName) {
        return listItemMap.get(listName);
    }

    /**
     * 
     * @param mapName
     * @return
     */
    public MapItem getMapItem(String mapName) {
        return mapItemMap.get(mapName);
    }

    public static void main(String[] args) {
        DaoConfig.getInstance();
    }

    public DbStrategyItem getDefaultDbStrategyItem() {
        return defaultDbStrategyItem;
    }

    public IdCenterDsItem getIdCenterDsItem() {
        return idCenterDsItem;
    }

    public void destroy() {
        // close datasources
        if (sessionFactoryBean != null) {
            sessionFactoryBean.destroy();
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
