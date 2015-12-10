/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:24
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DbStrategyItem implements Serializable {
	private static Logger logger = Logger.getLogger(DbStrategyItem.class);

	/**
     * 
     */
	private static final long serialVersionUID = -6388822807404747566L;

	private String name;
	private String clasz;
	private String objectName;
	private Map<String, PatternItem> patternItemMap = new HashMap<String, PatternItem>();
	private List<PatternItem> patternItemList = new ArrayList<PatternItem>();
	private boolean useCRC32 = false;

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
	 * @return the clasz
	 */
	public String getClasz() {
		return clasz;
	}

	/**
	 * @param clasz
	 *            the clasz to set
	 */
	public void setClasz(String clasz) {
		this.clasz = clasz;
	}

	public void addPatternItem(PatternItem patternItem) {
		if (patternItem != null && StringUtils.isNotEmpty(patternItem.getValue())) {
			if (!patternItemMap.containsKey(patternItem.getValue())) {
				patternItemMap.put(patternItem.getValue(), patternItem);
				patternItemList.add(patternItem);
			} else {
				logger.error("same <" + patternItem.getValue() + "> value PatternItem have exist!");
			}
		} else {
			logger.error(" PatternItem must has value and value isn't empty");
		}
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
	}

	public boolean isUseCRC32() {
		return useCRC32;
	}

	public void setUseCRC32(boolean useCRC32) {
		this.useCRC32 = useCRC32;
	}

	@Override
	public String toString() {
		return "DbStrategyItem [name=" + name + ", clasz=" + clasz + ", objectName=" + objectName + ", patternItemMap=" + patternItemMap + ", patternItemList=" + patternItemList
				+ ", useCRC32=" + useCRC32 + "]";
	}

	public Map<String, PatternItem> getPatternItemMap() {
		return patternItemMap;
	}

	public List<PatternItem> getPatternItemList() {
		return patternItemList;
	}

}
