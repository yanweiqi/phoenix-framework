package com.ginkgocap.ywxt.framework.dal.dao.exception;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:03
 * @Copyright Copyright©2015 www.gintong.com
 */
public class StrategyException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 8188585329675336173L;

    public StrategyException(Exception e) {
        super(e);
    }

    public StrategyException(String message) {
        super(message);

    }
}
