/**
 * 
 */
package com.ginkgocap.ywxt.framework.dao.id;

import java.text.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:23:56
 * @Copyright Copyright©2015 www.gintong.com
 */
public class TimeIdHelper {

	
	private static final int TIME_BIT_MOVE = 22;
	private static final int IDC_BIT_MOVE =  18;
	
	private static final int IDC_MAX = ~(Integer.MAX_VALUE << (TIME_BIT_MOVE - IDC_BIT_MOVE ));
	
	public static final long TIME_BIT = Long.MAX_VALUE << TIME_BIT_MOVE;

	public static final long IDC_ID_BIT = Long.valueOf(IDC_MAX) << IDC_BIT_MOVE;

	public static final long SEQ_BIT = ~(Long.MAX_VALUE << IDC_BIT_MOVE);

	private static final int DEFAULT_IDC = 6;

	private static int DEFAULT_ID_INTEVAL = 5;


	public static boolean isUuidAfterUpdate(long id) {
		return TimeIdHelper.isValidId(id) && id > 3342818919841793l;   
	}

	/**
	 * is valid id
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isValidId(long id) {
		return (id > 3000000000000000L) && (id < 4500000000000000L);
	}

	/**
	 * get unix time from id (Accurate to seconds)
	 * 
	 * @param id
	 * @return
	 */
	public static long getTimeFromId(long id) {
		return getTimeNumberFromId(id) + 515483463;
	}

	/**
	 * get time number from id
	 * 
	 * @param id
	 * @return
	 */
	public static long getTimeNumberFromId(long id) {
		return id >> TIME_BIT_MOVE;
	}

	/**
	 * get idc from id
	 * 
	 * @param id
	 * @return
	 */
	public static long getIdcIdFromId(long id) {
		return (id & IDC_ID_BIT) >> IDC_BIT_MOVE;
	}

	/**
	 * get seq from id
	 * 
	 * @param id
	 * @return
	 */
	public static long getSeqFromId(long id) {
		return id & SEQ_BIT;
	}

	/**
	 * get date time from id
	 * 
	 * @param id
	 * @return
	 */
	public static Date getDateFromId(long id) {
		return new Date(getTimeFromId(id) * 1000);
	}

	/**
	 * get id by date
	 * 
	 * @param date
	 * @return
	 */
	public static long getIdByDate(Date date, AtomicLong sid) {
		long uuid = date.getTime() / 1000;

		uuid -= 515483463;
		uuid <<= 22;
		uuid += DEFAULT_IDC << IDC_BIT_MOVE;

		long sidValue = sid.addAndGet(DEFAULT_ID_INTEVAL);
		uuid += sidValue % (1 << IDC_BIT_MOVE);
		return uuid;
	}
	
	
	
	/**
	 * get id by date
	 * 
	 * @param date
	 * @return
	 */
	public static long getIdByDate(Date date, AtomicLong sid, int idc) {
		if (idc <0 || idc > IDC_MAX ) {
			throw new RuntimeException("idc value must in 0 ~ " + IDC_MAX);
		}

		long uuid = date.getTime() / 1000;

		uuid -= 515483463;
		uuid <<= TIME_BIT_MOVE;
		uuid += idc << IDC_BIT_MOVE;

		long sidValue = sid.addAndGet(DEFAULT_ID_INTEVAL);
		uuid += sidValue % (1 << IDC_BIT_MOVE);
		return uuid;
	}
	
	
	/**
	 * get id by currentTimeMillis
	 * 
	 * @param date
	 * @return
	 */
	public static long getIdByDate(Long  currentTimeMillis, AtomicLong sid, int idc) {
		if (idc <0 || idc > IDC_MAX ) {
			throw new RuntimeException("idc value must in 0 ~ " + IDC_MAX);
		}
		
		long uuid = currentTimeMillis / 1000;

		uuid -= 515483463;
		uuid <<= TIME_BIT_MOVE;
		uuid += idc << IDC_BIT_MOVE;
		int step = (int)(Math.random() * 10)+1;
		long sidValue = sid.addAndGet(step);
		//long sidValue = sid.addAndGet(DEFAULT_ID_INTEVAL);
		uuid += sidValue % (1 << IDC_BIT_MOVE);
		return uuid;
	}
	

	public static void main(String[] args) {
		// long id = 3374709054121351l;
		// long id = 3379782484330149l;
		// long id = 3363475030378149l; //10.1 3363475030378149
		// long id = 3374709054211749l; //11.1 3374709054211749
		// long id = 3100365840449541l;
		// System.out.println(getTimeFromId(id) * 1000);
		// System.out.println(new Date(UuidHelper.getTimeFromId(id) * 1000));
		// SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		// System.out.println(format.format(UuidHelper.getTimeFromId(id) *
		// 1000));
		
		AtomicLong sid = new AtomicLong(0);
		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < 100; i++) {
			long id = getIdByDate(new Date(), sid, 0);
			ids.add(id);
			//System.out.println(getDateFromId(id));
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
		for (Long id : ids) {
			Date date = getDateFromId(id);
			long idSeq = getSeqFromId(id);
			long idc = getIdcIdFromId(id);
			System.out.println("id " + id + " " + dateFormat.format(date) + " idc: " + idc + " idSeq: " + idSeq);
		}
	}
	
	
	
	
	
}
