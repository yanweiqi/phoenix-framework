package com.ginkgocap.ywxt.framework.dal.cache.config.model;

import java.io.Serializable;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:11:03
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DatasourceItem implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6548768493657025956L;
    private String name;      
    private int timeout;      
    private int port;         
    private String server;    
    private int maxActive;   
    private int maxIdle;      
    private int maxWait;      
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public int getMaxActive() {
        return maxActive;
    }
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
    public int getMaxIdle() {
        return maxIdle;
    }
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }
    public int getMaxWait() {
        return maxWait;
    }
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }
}
