/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.dao;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.config.DaoConfig;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.method.ItemMethod;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:45
 * @Copyright Copyright©2015 www.gintong.com
 */
public class MapItem implements ItemMethod {
	/**
	 * <map name="VctlUser_Map_mobile_status_userId"
	 * sqlitem="select user_id from vctl_user where mobile_no=? and status=?"
	 * keyProperty="mobileNo,status" valueProperty="userId" />
	 */
	private static Logger logger = Logger.getLogger(MapItem.class);

	private static final long serialVersionUID = 492940360625423139L;
	private String name;
	private String sqlitem;
	private String sqlcountitem;
	private String sqlType;
	private String keyProperty;
	private String valueProperty;

	private String objectName;

	private Method[] keyMethod;
	private Method valueMethod;
	private String columnName;
	private boolean update = false;

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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

	}

	/**
	 * @return the sqlitem
	 */
	public String getSqlitem() {
		return sqlitem;
	}

	/**
	 * @param sqlitem
	 *            the sqlitem to set
	 */
	public void setSqlitem(String sqlitem) {
		this.sqlitem = sqlitem;
	}

	/**
	 * @return the sqlType
	 */
	public String getSqlType() {
		return sqlType;
	}

	/**
	 * @param sqlType
	 *            the sqlType to set
	 */
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	/**
	 * @return the keyProperty
	 */
	public String getKeyProperty() {
		return keyProperty;
	}

	/**
	 * @param keyProperty
	 *            the keyProperty to set
	 */
	public void setKeyProperty(String keyProperty) {
		this.keyProperty = keyProperty;
	}

	/**
	 * @return the valueProperty
	 */
	public String getValueProperty() {
		return valueProperty;
	}

	/**
	 * @param valueProperty
	 *            the valueProperty to set
	 */
	public void setValueProperty(String valueProperty) {
		this.valueProperty = valueProperty;
	}

	/**
	 * @return the objectName
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * @param objectName
	 *            the objectName to set
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
		if (StringUtils.isNotEmpty(objectName) && StringUtils.isNotEmpty(keyProperty) && StringUtils.isNotEmpty(valueProperty)) {
			try {
				String[] keys = StringUtils.splitPreserveAllTokens(this.keyProperty, DaoConfig.PROPERTY_SEPARATOR);
				this.keyMethod = new Method[keys.length];
				for (int i = 0; i < keys.length; i++) {
					this.keyMethod[i] = MethodUtils.getAccessibleMethod(Class.forName(objectName), "get" + WordUtils.capitalize(keys[i]), new Class[] {});
				}
				valueMethod = MethodUtils.getAccessibleMethod(Class.forName(objectName), "get" + WordUtils.capitalize(this.valueProperty), new Class[] {});
				if (StringUtils.isEmpty(this.columnName) && StringUtils.isNotEmpty(this.sqlitem)) {
					String tempCol = StringUtils.substringBetween(this.sqlitem, "select", "from");
					this.columnName = StringUtils.trimToEmpty(tempCol);
				}
			} catch (ClassNotFoundException e) {
				if (logger.isDebugEnabled()) {
					e.printStackTrace(System.err);
				} else {
					logger.error("class no't find " + this.objectName);
				}
			}
		}
	}

	/**
	 * @return the keyMethod
	 */
	public Method[] getKeyMethod() {
		return keyMethod;
	}

	/**
	 * @return the valueMethod
	 */
	public Method getValueMethod() {
		return valueMethod;
	}

	public String getSqlcountitem() {
		if (StringUtils.isEmpty(sqlcountitem) && StringUtils.isNotEmpty(this.sqlitem)) {
			String sTemp = this.sqlitem.replace("  ", " ");
			this.sqlcountitem = sTemp.replaceAll("select (.+?) from", "select count($1) from");
		}
		return sqlcountitem;
	}

	/**
	 * 
	 * @return
	 * @author
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("MapItem[");
		buffer.append("columnName = ").append(columnName);
		if (keyMethod == null) {
			buffer.append(" keyMethod = ").append("null");
		} else {
			buffer.append(" keyMethod = ").append(Arrays.asList(keyMethod).toString());
		}
		buffer.append(" keyProperty = ").append(keyProperty);
		buffer.append(" name = ").append(name);
		buffer.append(" objectName = ").append(objectName);
		buffer.append(" sqlcountitem = ").append(sqlcountitem);
		buffer.append(" sqlitem = ").append(sqlitem);
		buffer.append(" sqlType = ").append(sqlType);
		buffer.append(" update = ").append(update);
		buffer.append(" valueMethod = ").append(valueMethod);
		buffer.append(" valueProperty = ").append(valueProperty);
		buffer.append("]");
		return buffer.toString();
	}
}
