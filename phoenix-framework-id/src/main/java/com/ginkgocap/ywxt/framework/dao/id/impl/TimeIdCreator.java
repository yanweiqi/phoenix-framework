package com.ginkgocap.ywxt.framework.dao.id.impl;

import com.ginkgocap.ywxt.framework.dao.id.*;
import com.ginkgocap.ywxt.framework.dao.id.exception.CreateIdException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:23:28
 * @Copyright Copyright©2015 www.gintong.com
 */
public class TimeIdCreator implements IdCreator {

	private ConcurrentMap<String, AtomicLong> automicLongMap = new ConcurrentHashMap<String, AtomicLong>();
	private Integer idc = null;

	@Override
	public Long nextId(String sKey) throws CreateIdException {

		if (isEmpty(sKey)) {
			throw new CreateIdException("sKey must have value");
		} else {
			String atomicKey = sKey;
			AtomicLong seqAtomic = automicLongMap.get(atomicKey);

			if (seqAtomic == null) {
				AtomicLong memAtomic = new AtomicLong(0);
				seqAtomic = automicLongMap.putIfAbsent(atomicKey, memAtomic);
				seqAtomic = seqAtomic == null ? memAtomic : seqAtomic;
			}

			return TimeIdHelper.getIdByDate(System.currentTimeMillis(), seqAtomic, idc);

		}
	}

	public TimeIdCreator(Integer idc) {
		this.idc = idc;
	}

}
