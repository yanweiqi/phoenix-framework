/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.group;

import java.io.Serializable;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:16
 * @Copyright Copyright©2015 www.gintong.com
 */
public class GroupItem implements Serializable {
    private String name;
    private String master;
    private String slave;
    private String weigh;

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
     * @return the master
     */
    public String getMaster() {
        return master;
    }

    /**
     * @param master
     *            the master to set
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * @return the salve
     */
    public String getSlave() {
        return slave;
    }

    /**
     * @param salve
     *            the salve to set
     */
    public void setSlave(String slave) {
        this.slave = slave;
    }

    /**
     * @return the weigh
     */
    public String getWeigh() {
        return weigh;
    }

    /**
     * @param weigh
     *            the weigh to set
     */
    public void setWeigh(String weigh) {
        this.weigh = weigh;
    }

    /**
     * toString method: creates a String representation of the object
     * 
     * @return the String representation
     * @author e.sale
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("GroupItem[");
        buffer.append("master = ").append(master);
        buffer.append("\n name = ").append(name);
        buffer.append("\n slave = ").append(slave);
        buffer.append("\n weigh = ").append(weigh);
        buffer.append("]");
        return buffer.toString();
    }
}
