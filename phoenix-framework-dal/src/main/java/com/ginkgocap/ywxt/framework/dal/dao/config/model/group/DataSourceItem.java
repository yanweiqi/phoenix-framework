/**
 * 
 */
package com.ginkgocap.ywxt.framework.dal.dao.config.model.group;

import java.io.Serializable;

import org.hibernate.cfg.Configuration;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:11
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DataSourceItem implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3370406254232535134L;
    private String name;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private String maxActive;
    private String maxIdle;
    private String maxWait;
    private String master;
    private String salve;
    
    private boolean testOnBorrow=false;;
    private boolean testOnReturn=false;
    private boolean testWhileIdle=false;
    
    
    
    
    public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	private Configuration hibernateCfg = new Configuration();

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
     * @return the driverClassName
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * @param driverClassName
     *            the driverClassName to set
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the maxActive
     */
    public String getMaxActive() {
        return maxActive;
    }

    /**
     * @param maxActive
     *            the maxActive to set
     */
    public void setMaxActive(String maxActive) {
        this.maxActive = maxActive;
    }

    /**
     * @return the maxIdle
     */
    public String getMaxIdle() {
        return maxIdle;
    }

    /**
     * @param maxIdle
     *            the maxIdle to set
     */
    public void setMaxIdle(String maxIdle) {
        this.maxIdle = maxIdle;
    }

    /**
     * @return the maxWait
     */
    public String getMaxWait() {
        return maxWait;
    }

    /**
     * @param maxWait
     *            the maxWait to set
     */
    public void setMaxWait(String maxWait) {
        this.maxWait = maxWait;
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
    public String getSalve() {
        return salve;
    }

    /**
     * @param salve
     *            the salve to set
     */
    public void setSalve(String salve) {
        this.salve = salve;
    }

    /**
     * toString method: creates a String representation of the object
     * 
     * @return the String representation
     * @author e.sale
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DataSourceItem[");
        buffer.append("driverClassName = ").append(driverClassName);
        buffer.append("\n master = ").append(master);
        buffer.append("\n maxActive = ").append(maxActive);
        buffer.append("\n maxIdle = ").append(maxIdle);
        buffer.append("\n maxWait = ").append(maxWait);
        buffer.append("\n name = ").append(name);
        buffer.append("\n password = ").append(password);
        buffer.append("\n salve = ").append(salve);
        buffer.append("\n url = ").append(url);
        buffer.append("\n username = ").append(username);
        buffer.append("]");
        return buffer.toString();
    }

    public Configuration getHibernateCfg() {
        return hibernateCfg;
    }

    public void setHibernateCfg(Configuration hibernateCfg) {
        this.hibernateCfg = hibernateCfg;
    }
}
