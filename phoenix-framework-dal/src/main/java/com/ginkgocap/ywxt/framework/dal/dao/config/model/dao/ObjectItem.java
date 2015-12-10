/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.dao;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.SqlStringHelper;

	
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:50
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ObjectItem implements Serializable {

    private static Logger logger = Logger.getLogger(ObjectItem.class);
    private static final long serialVersionUID = -6501567061333306898L;
    /**
     * 
     */
    private String name;
    private String listenerClass;
    private String strategyProperty;

    private Map<String, ListItem> listMap = new HashMap<String, ListItem>();
    private Map<String, MapItem> mapMap = new HashMap<String, MapItem>();   

    private DbStrategyItem dbStrategyItem;
    private Method strategyPropertyMethod;
    private Method idMethod;

    // 2009-02-20
    private String delProperty;
    private Method delPropertyMethod;
    private String delValue;
    private Object delValueObject;

    //2009-04-07
    private String entitySql;
    
    /**
     * @param dbStrategyItem
     *            the dbStrategyItem to set
     */
    public void setDbStrategyItem(DbStrategyItem dbStrategyItem) {
        if (dbStrategyItem != null) {
            dbStrategyItem.setObjectName(this.name); // 建立和对象的关系
        }
        this.dbStrategyItem = dbStrategyItem;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
        if (StringUtils.isNotEmpty(name)) {
            try {
                Class objectClass = Class.forName(name);
                Method[] methods = objectClass.getMethods();
                if (!ArrayUtils.isEmpty(methods)) {
                    for (int i = 0; i < methods.length; i++) {
                        Method method = methods[i];
                        Annotation annotation = method.getAnnotation(Id.class);
                        if (annotation != null) {
                            this.idMethod = method;
                            break;
                        }
                    }
                }
                if (this.idMethod == null) {
                    logger.error("don't found the id annotation for " + name);
                }
            } catch (ClassNotFoundException e) {
                logger.error(name + " class not found");
            }
        }
    }

    /**
     * @return the listenerClass
     */
    public String getListenerClass() {
        return listenerClass;
    }

    /**
     * @param listenerClass
     *            the listenerClass to set
     */
    public void setListenerClass(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    /**
     * @return the strategyProperty
     */
    public String getStrategyProperty() {
        return strategyProperty;
    }

    /**
     * @param strategyProperty
     *            the strategyProperty to set
     */
    public void setStrategyProperty(String strategyProperty) {
        this.strategyProperty = strategyProperty;
    }

    /**
     * @return the listMap
     */
    public Map<String, ListItem> getListMap() {
        return listMap;
    }

    /**
     * 
     * @param listItem
     */
    public void addListMap(ListItem listItem) {
        if (listItem != null && StringUtils.isNotEmpty(listItem.getName())) {
            if (!listMap.containsKey(listItem.getName())) {
                listItem.setObjectName(this.getName()); // 对象和List建立关系
                listMap.put(listItem.getName(), listItem);
            } else {
                logger.error("same <" + listItem.getName() + ">name ListItem exist!");
            }
        }
    }
   
   
    /**
     * 
     * @param listName
     * @return
     */
    public ListItem getListItem(String listName) {
        return this.listMap.get(listName);
    }

    /**
     * 
     * @param mapItem
     */
    public void addMapMap(MapItem mapItem) {
        if (mapItem != null && StringUtils.isNotEmpty(mapItem.getName())) {
            if (!mapMap.containsKey(mapItem.getName())) {
                mapItem.setObjectName(this.getName()); // 对象和Map建立关系
                mapMap.put(mapItem.getName(), mapItem);
            } else {
                logger.error("same <" + mapItem.getName() + ">name ListItem exist!");
            }
        }
    }

    /**
     * 
     * @param mapName
     * @return
     */
    public MapItem getMapItem(String mapName) {
        return this.mapMap.get(mapName);
    }

    /**
     * @return the dbStrategyItem
     */
    public DbStrategyItem getDbStrategyItem() {
        return dbStrategyItem;
    }

    /**
     * @return the strategyPropertyMethod
     */
    public Method getStrategyPropertyMethod() {
        return strategyPropertyMethod;
    }

    /**
     * @param strategyPropertyMethod
     *            the strategyPropertyMethod to set
     */
    public void setStrategyPropertyMethod(Method strategyPropertyMethod) {
        this.strategyPropertyMethod = strategyPropertyMethod;
    }

    /**
     * 
     * @return
     * @author
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ObjectItem[");
        buffer.append("]");
        return buffer.toString();
    }

    public Map<String, MapItem> getMapMap() {
        return mapMap;
    }

    public Method getIdMethod() {
        return idMethod;
    }

    public void setIdMethod(Method idMethod) {
        this.idMethod = idMethod;
    }

    public void setDelProperty(String delProperty) {
        this.delProperty = delProperty;
    }

    public void setDelValue(String delValue) {
        this.delValue = delValue;
    }

    public String getDelProperty() {
        return delProperty;
    }

    public String getDelValue() {
        return delValue;
    }

    public Method getDelPropertyMethod() {
        return delPropertyMethod;
    }

    public void setDelPropertyMethod(Method delPropertyMethod) {
        this.delPropertyMethod = delPropertyMethod;
    }

    public Object getDelValueObject() {
        return delValueObject;
    }

    public void setDelValueObject(Object delValueObject) {
        this.delValueObject = delValueObject;
    }

	public String getEntitySql() {
	    if (StringUtils.isBlank(entitySql)){
	        entitySql = SqlStringHelper.getLoadObjectSql(name);
	    }
		return entitySql;
	}

	public void setEntitySql(String entitySql) {
		this.entitySql = entitySql;
	}
}
