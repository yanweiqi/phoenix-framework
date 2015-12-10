package com.ginkgocap.ywxt.framework.dal.dao.route.strategy;

import com.ginkgocap.ywxt.framework.dal.dao.exception.StrategyException;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:20:43
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface IStrategy {
   public static int STRATEGY_R = 1;
    public static int STRATEGY_W = 2;
  
    public Object ObjectShardingStrategy(Class clazz, Object account, int rw) throws StrategyException;

    public Object ListShardingStrategy(String listName, Object account, int rw) throws StrategyException;

    public Object MapShardingStrategy(String listName, Object account, int rw) throws StrategyException;

    public boolean isReadWrite(Class clazz, Object account) throws StrategyException;
        
    public Object NextListShardingStrategy(String listName,int rw) throws StrategyException;
    
    
    
    
    
    public Object ObjectShardingStrategy(Class clazz, Object account, Object id , int rw) throws StrategyException;

    public Object ListShardingStrategy(String listName, Object account, Object[] params, int rw) throws StrategyException;

    public Object MapShardingStrategy(String listName, Object account, Object[] params, int rw) throws StrategyException;

  //  public boolean isReadWrite(Class clazz, Object account) throws StrategyException;
        
  //  public Object NextListShardingStrategy(String listName,int rw) throws StrategyException;
}
