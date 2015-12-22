package com.ginkgocap.ywxt.framework.dal.dao.id.util;


import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

import com.ginkgocap.ywxt.framework.dao.id.IdCreator;
import com.ginkgocap.ywxt.framework.dao.id.IdCreatorFactory;
import com.ginkgocap.ywxt.framework.dao.id.exception.CreateIdException;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:36
 * @Copyright Copyright©2015 www.gintong.com
 */
@Component("timeIdGenerator")
public class TimeIdGenerator implements IdentifierGenerator {

    private static final Log log = LogFactory.getLog(TimeIdGenerator.class);


    public static AtomicInteger createCnt = new AtomicInteger(0);

    /**
     * @return 根据Sequence名字，返回唯一的Sequence id.类似于Oracle的sequence机制
     * @param session:
     *            这个参数没有用到
     * @param arg1:
     *            这个参数没有用到
     * 
     */
    public Serializable generate(SessionImplementor session, Object arg1) throws HibernateException {
    	IdCreator idCreator  = IdCreatorFactory.getTimeIdCreator(1);
        try {
			return idCreator.nextId(arg1.getClass().getName());
		} catch (CreateIdException e) {
			throw new HibernateException(e);
		}
    }
}
