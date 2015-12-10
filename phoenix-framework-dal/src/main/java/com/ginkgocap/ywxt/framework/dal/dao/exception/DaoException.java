package com.ginkgocap.ywxt.framework.dal.dao.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:57
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DaoException extends Exception {
    
	private Log log = LogFactory.getLog(DaoException.class);
    //权限不匹配异常
    public static final int USERID_MATCH_ERROR = 100;
    public static final int MAPPING_NAME_EMPTY  =200;
    public static final int CLASS_TYPE_NULL = 300;
    public static final int OBJECT_NULL_EXCEPTION =301; 
    public static final int SessionFactory_NULL_EXCEPTION = 302;
    //空指针异常
    public static final int NULLPOINTER_EXCEPTION = 303;
    //在配置文件中设置的Map 元素为空的异常
    public static final int MAP_NULLEXCEPTION = 304;
    public static final int SQL_NULLEXCEPTION = 305;
    //配置文件出现问题的异常
    public static final int Strategy_Exception = 306;
    //============== Hibernate Exception =================    
    //处理的hibernate异常
    public static final int Hibernate_Exception = 400;
    //用于指pojo配置错误所引发的异常
    public static final int POJO_NOTFOUND_EXCEPTION = 401;
    //作为参数的两个List不匹配，用于类似getMapList()这样的需求时
    public static final int LIST_NOT_MATCH = 402;
    
    //============== SQL Exception =================
    public static final int SQL_EXCEPTION = 500;
    
    public  int error_num;
    public  String message;
     
    public DaoException(Exception e) {
        super(e);
    }
    public DaoException(int error_number,Exception e){
        super(e);
        error_num = error_number;
    }
    public DaoException(int error_number,String message){
        super(message);
    	log.error(message);
        error_num = error_number;
    }
    public DaoException(int error_number){
        error_num = error_number;
    }
    public int getErrorNum() {
        return error_num;
    }
    public void setErrorNum(int error_num) {
        this.error_num = error_num;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
}
