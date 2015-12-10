package com.ginkgocap.ywxt.framework.dal.dao.config.model.method;

import java.io.Serializable;
import java.lang.reflect.Method;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:18:23
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface ItemMethod extends Serializable {
    public Method[] getKeyMethod();
}
