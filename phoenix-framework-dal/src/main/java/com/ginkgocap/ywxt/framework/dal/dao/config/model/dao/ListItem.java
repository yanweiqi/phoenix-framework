/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.dao;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.dao.config.DaoConfig;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.SqlStringHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.method.ItemMethod;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:38
 * @Copyright Copyright©2015 www.gintong.com
 */
public class ListItem implements ItemMethod {
	private static Logger logger = Logger.getLogger(ListItem.class);
	/**
     * 
     */
	private static final long serialVersionUID = 492940360625423139L;
	private String name;
	private String sqlitem;
	private String sqlcountitem;

	private String sqldelitem;

	private String keyProperty;
	private String valueProperty;
	// 康杨加的
	private String orderByProperty;

	private String strategyColumn;

	private String objectName;

	private String[] orderbyFields;

	// 康杨加的
	private Method[] orderByMethod;
	private Method[] keyMethod;
	private Method valueMethod;

	private String columnName;

	private int limitLen = 300;
	private int initLen = 100;

	// private Integer type;

	private boolean update = false;

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
		if (StringUtils.isNotEmpty(objectName)) {
			// 处理KeyProperty的逻辑
			if (StringUtils.isNotEmpty(keyProperty)) {
				String[] keys = StringUtils.splitPreserveAllTokens(this.keyProperty, DaoConfig.PROPERTY_SEPARATOR);
				this.keyMethod = new Method[keys.length];
				try {
					for (int i = 0; i < keys.length; i++) {
						String key = StringUtils.trim(keys[i]);
						Method method = MethodUtils.getAccessibleMethod(Class.forName(objectName), "get" + WordUtils.capitalize(key), new Class[] {});
						method = method != null ? method : MethodUtils.getAccessibleMethod(Class.forName(objectName), "is" + WordUtils.capitalize(key), new Class[] {});
						this.keyMethod[i] = method;
					}
				} catch (ClassNotFoundException e) {
					if (logger.isDebugEnabled()) {
						e.printStackTrace(System.err);
					} else {
						logger.error("cann't find " + this.objectName);
					}
				}
			} else {
				logger.info("keyProperty is null");
			}
			// 处理Value Property的逻辑
			if (StringUtils.isNotEmpty(valueProperty)) {
				try {
					String value = StringUtils.trim(this.valueProperty);
					valueMethod = MethodUtils.getAccessibleMethod(Class.forName(objectName), "get" + WordUtils.capitalize(value), new Class[] {});
					if (StringUtils.isEmpty(this.columnName) && StringUtils.isNotEmpty(this.sqlitem)) {
						String tempCol = StringUtils.substringBetween(this.sqlitem, "select", "from");
						this.columnName = StringUtils.trimToEmpty(tempCol);
					}
				} catch (ClassNotFoundException e) {
					if (logger.isDebugEnabled()) {
						e.printStackTrace(System.err);
					} else {
						logger.error("cann't find " + this.objectName);
					}
				}
			} else {
				logger.info("valueProperty is null");
			}
			// 康杨加的 --- 处理Order by Property的逻辑
			if (StringUtils.isNotEmpty(orderByProperty)) {
				String[] keys = StringUtils.splitPreserveAllTokens(this.orderByProperty, DaoConfig.PROPERTY_SEPARATOR);
				this.orderByMethod = new Method[keys.length];
				try {
					for (int i = 0; i < keys.length; i++) {
						String key = StringUtils.trim(keys[i]);
						Method method = MethodUtils.getAccessibleMethod(Class.forName(objectName), "get" + WordUtils.capitalize(key), new Class[] {});
						method = method != null ? method : MethodUtils.getAccessibleMethod(Class.forName(objectName), "is" + WordUtils.capitalize(key), new Class[] {});
						this.orderByMethod[i] = method;
					}
				} catch (ClassNotFoundException e) {
					if (logger.isDebugEnabled()) {
						e.printStackTrace(System.err);
					} else {
						logger.error("cann't find " + this.objectName);
					}
				}
			} else {
				logger.info("keyProperty is null");
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
			// Fix 大小写的SQL
			this.sqlcountitem = SqlStringHelper.getCountSql(this.sqlitem);
		}
		return sqlcountitem;
	}

	public String getSqldelitem() {
		if (StringUtils.isEmpty(sqldelitem) && StringUtils.isNotEmpty(this.sqlitem)) {
			// Fix 大小写的SQL
			this.sqldelitem = SqlStringHelper.getDelSql(this.sqlitem);
		}
		return sqldelitem;
	}

	public String[] getOrderbyFields() {
		if (ArrayUtils.isEmpty(orderbyFields) && StringUtils.isNotEmpty(this.sqlitem)) {
			// Fix 大小写的SQL
			this.orderbyFields = SqlStringHelper.getOrderbyFields(this.sqlitem);
		}
		return orderbyFields;
	}

	public void setSqldelitem(String sqldelitem) {
		this.sqldelitem = sqldelitem;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	/**
	 * 
	 * @return
	 * @author
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ListItem[");
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
		buffer.append(" sqldelitem = ").append(sqldelitem);
		buffer.append(" sqlitem = ").append(sqlitem);
		buffer.append(" update = ").append(update);
		buffer.append(" valueMethod = ").append(valueMethod);
		buffer.append(" valueProperty = ").append(valueProperty);
		buffer.append("]");
		return buffer.toString();
	}

	public static String reverseSql(String sql) {
		String tempSql = sql + " ";
		Pattern patternDesc = Pattern.compile("order by (\\w+?) desc");
		Matcher matcher = patternDesc.matcher(tempSql);
		if (matcher.find()) {
			return matcher.replaceFirst("order by $1 asc");
		}

		Pattern patternAsc = Pattern.compile("order by (\\w+?) asc");
		matcher = patternAsc.matcher(tempSql);
		if (matcher.find()) {
			return matcher.replaceFirst("order by $1 desc");
		}

		patternAsc = Pattern.compile("order by (\\w+?) ");
		matcher = patternAsc.matcher(tempSql);
		if (matcher.find()) {
			System.out.println(matcher.group(1));
			return matcher.replaceFirst("order by $1 desc");
		}

		patternAsc = Pattern.compile("select (.+?) from");
		matcher = patternAsc.matcher(tempSql);
		if (matcher.find()) {
			return sql + " order by " + matcher.group(1) + " desc";
		} else {
			return sql;
		}
	}

	public static void main(String[] args) {
		Pattern PatternUploadLiTag = Pattern.compile("<li>.+?href=[\"|']{1}(.+?)[\"|']{1}.+?src=[\"|']{1}(.+?)[\"|']{1}.+?</li>");

		String cc = "<li><a href='/app/album/#/app/album/list/photo.do?u=12&pid=17514'><img class='photo-130' src='http://10.10.82.52/alumb/2009/03/10/14/34/11fef17356b_1.jpg'/></a></li><li><a href='http://sns.sohu.com/app/album/#/app/album/list/photo.do?u=12&pid=17515'><img class='photo-130' src='http://10.10.82.52/alumb/2009/03/10/14/34/11fef17356c_1.jpg'/></a></li>";
		Matcher matcher = PatternUploadLiTag.matcher(cc);
		while (matcher.find()) {
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		}

	}

	public String getStrategyColumn() {
		return strategyColumn;
	}

	public void setStrategyColumn(String strategyColumn) {
		this.strategyColumn = strategyColumn;
	}

	public String getOrderByProperty() {
		return orderByProperty;
	}

	public void setOrderByProperty(String orderByProperty) {
		this.orderByProperty = orderByProperty;
	}

	public Method[] getOrderByMethod() {
		return orderByMethod;
	}

	public void setOrderByMethod(Method[] orderByMethod) {
		this.orderByMethod = orderByMethod;
	}

	public int getLimitLen() {
		return limitLen;
	}

	public void setLimitLen(int limitLen) {
		this.limitLen = limitLen;
	}

	public int getInitLen() {
		return initLen;
	}

	public void setInitLen(int initLen) {
		this.initLen = initLen;
	}

}
