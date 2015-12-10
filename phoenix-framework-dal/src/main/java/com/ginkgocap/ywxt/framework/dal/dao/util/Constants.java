package com.ginkgocap.ywxt.framework.dal.dao.util;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:02
 * @Copyright Copyright©2015 www.gintong.com
 */
public class Constants {

	//List 每段Section 的 size 
    public static final Long SecCapacity = 30l;

    public static final int SeqIdCapacity = 100;
    public static final String MinId = "oldValue";
    public static final String MaxId = "newValue";
    public static final String NullObjectPrefix = "NULL_";
    public static final String MaxIdPrefix = "MaxId_";
   
    
    //====================== 目前系统支持的List的类型 ===================================
	public static final int COMMON_TYPE = 1;
	public static final int COMMON_ORDERED_TYPE =2;
	//跨库查询的List类型
	public static final int CROSSDB_TYPE =3;
    //====================== 目前系统支持的List的类型 ===================================	
	
	
	//跨库聚合的属性
	public static final int Cross_MaxDB_Count = 200;
	
	//======================  和List 加载锁定相关的属性
    public static final boolean UsingLock = true;
    public static final boolean NoLock    = false;
}
