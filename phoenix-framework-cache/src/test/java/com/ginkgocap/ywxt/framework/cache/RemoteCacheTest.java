package com.ginkgocap.ywxt.framework.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.ginkgocap.ywxt.framework.dal.cache.Cache;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.RemoteCacheFactoryImpl;

/**
 * Unit test for simple App.
 */
public class RemoteCacheTest extends TestCase {
	private static Logger logger = Logger.getLogger(RemoteCacheTest.class);

	private final static int COUNT_OBJECT = 10000;
	private final static int COUNT_INT = 10000;
	private final static int COUNT_INC = 10000;


	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public RemoteCacheTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		RemoteCacheFactoryImpl.getInstance();
		return new TestSuite(RemoteCacheTest.class);
	}

	public void testPutInt() throws Exception {
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("object");
		for (int i = 0; i < COUNT_INT; i++) {
			boolean b = cache.put(String.valueOf(i), i);
			assertTrue(b);
		}

	}

	public void testGetInt() throws Exception {
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("object");
		for (int i = 0; i < COUNT_INT; i++) {
			int cache_o = (Integer) cache.get(String.valueOf(i));
			assertEquals(i, cache_o);
		}
	}

	public void testPutObject() throws Exception {
		// put object
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity");
		int i = 0;
		long currenttime = System.currentTimeMillis();
		do {
			String iStr = String.valueOf(i);
			String name = "Name_" + iStr;
			Date birthday = new Date(currenttime);
			TestEntity entity = new TestEntity(NumberUtils.toLong(iStr), name, birthday, i);
			boolean b = cache.put(String.valueOf(i), entity);
			assertTrue(b);
			i++;
		} while (i < COUNT_OBJECT);
	}

	public void testGetObject() throws Exception {
		// put object

		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity");
		int i = 0;
		long currenttime = System.currentTimeMillis();
		// get object
		i = 0;
		do {
			String iStr = String.valueOf(i);
			TestEntity entityCache = (TestEntity) cache.get(iStr);
			;

			String name = "Name_" + iStr;
			Date birthday = new Date(currenttime);

			TestEntity entity = new TestEntity(NumberUtils.toLong(iStr), name, birthday, i);
			i++;
			assertEquals(entityCache, entity);
		} while (i < COUNT_OBJECT);
	}

	public void testGetObjects() throws Exception {
		// put object
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity");
		int i = 0;
		i = 0;
		String[] keys = new String[COUNT_OBJECT];
		do {
			keys[i] = String.valueOf(i);
			i++;
		} while (i < keys.length);

		Object[] result_arrayList = cache.get(keys);
		for (int j = 0; j < result_arrayList.length; j++) {
			String iStr = String.valueOf(j);

			String name = "Name_" + iStr;
			Date birthday = new Date(System.currentTimeMillis());
			TestEntity entity = new TestEntity(NumberUtils.toLong(iStr), name, birthday, j);

			assertEquals(result_arrayList[j], entity);
		}
	}
	
	
	public void testInc() throws Exception {
		// put object
		String key = "count";
		
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity");
		cache.delete(key);
		for (long i = 0; i < COUNT_INC; i++) {
			assertEquals(i, cache.incr(key, 1l));
		}
	}

	public void testDecr() throws Exception {
		// put object
		String key = "count";
		long desc = 1;
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity");
		cache.delete(key);
		cache.put(key, String.valueOf(COUNT_INC));
		for (long i = 1; i < COUNT_INC; i++) {
			assertEquals(cache.decr(key, desc), COUNT_INC - i);
		}
		long count = cache.incr(key, 0);
		assertEquals(1l, count);
	}
	
	
	/**
	 * test setList lrange 
	 * @throws Exception
	 */
	public void testList() throws Exception {
		String key = "list";
		long desc = 1;
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("List_My_Knowledge_ID");
		List<String> valueList = new ArrayList<String>();
		
		
		for (int i = 0; i < 900; i++) {
			valueList.add(String.valueOf(i));
		}
			
		cache.setList(key, valueList);
		
		
		List<String> lResult = cache.lrange(key, 2, 20);
		
		logger.info(lResult);
	}
	
	
	/**
	 * test ladd lrange 
	 * @throws Exception
	 */
	public void testladd() throws Exception {
		String key = "list";
		long desc = 1;
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("List_My_Knowledge_ID");
		
		cache.delete(key);		
		
		
		//key must exist, 
		cache.put(key, "100000000000000");
		for(int i = 0 ; i < 300; i++) {
			cache.ladd(key, String.valueOf(i));
		}
		
		List<String> lResult = cache.lrange(key, 2, 20);
		
		logger.info(lResult);
	}
	
	
	/**
	 * test radd lrange 
	 * @throws Exception
	 */
	public void testradd() throws Exception {
		String key = "list";
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("List_My_Knowledge_ID");
		
		cache.delete(key);		
		
		
		//key must exist, 
		cache.put(key, "100000000000000");
		for(int i = 0 ; i < 300; i++) {
			cache.radd(key, String.valueOf(i));
		}
		
		
		
		
		List<String> lResult = cache.lrange(key, 15, 50);
		logger.info("list size is : " + cache.lsize(key));
		logger.info(lResult);
	}
	
}
