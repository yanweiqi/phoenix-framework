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
import com.ginkgocap.ywxt.framework.dao.id.IdCreator;
import com.ginkgocap.ywxt.framework.dao.id.IdCreatorFactory;

/**
 * Unit test for simple App.
 */
public class RemoteCacheDIDTest extends TestCase {
	private static Logger logger = Logger.getLogger(RemoteCacheDIDTest.class);
	private final static int COUNT_OBJECT = 100;
	private final static int COUNT_INT = 100;
	private final static int COUNT_INC = 100;

	private final static IdCreator  idCreator = IdCreatorFactory.getTimeIdCreator(1);
	
	private static List<Long> ids = new ArrayList<Long>();

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public RemoteCacheDIDTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		RemoteCacheFactoryImpl.getInstance();
		return new TestSuite(RemoteCacheDIDTest.class);
	}



	public void testPutObject() throws Exception {
		// put object
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity",true);
		int i = 0;
		long currenttime = System.currentTimeMillis();
		ids.clear();
		do {
			Long id = idCreator.nextId(cache.getRegion());
			String iStr = String.valueOf(i);
			String name = "Name_" + iStr;
			Date birthday = new Date(currenttime);
			TestEntity entity = new TestEntity(NumberUtils.toLong(iStr), name, birthday, i);
			boolean b = cache.put(String.valueOf(id), entity);
			assertTrue(b);
			i++;
			ids.add(id);
		} while (i < COUNT_OBJECT);
	}
	public void testGetObject() throws Exception {
		// put object

		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity",true);
		int i = 0;
		long currenttime = System.currentTimeMillis();
		// get object
		i = 0;
		do {
			String iStr = String.valueOf(i);
			TestEntity entityCache = (TestEntity) cache.get(String.valueOf(ids.get(i)));

			String name = "Name_" + iStr;
			Date birthday = new Date(currenttime);

			TestEntity entity = new TestEntity(NumberUtils.toLong(iStr), name, birthday, i);
			i++;
			assertEquals(entityCache, entity);
		} while (i < COUNT_OBJECT);
	}

	public void testGetObjects() throws Exception {
		// put object
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("com.ginkgocap.ywxt.framework.cache.TestEntity",true);
		int i = 0;
		i = 0;
		String[] keys = new String[COUNT_OBJECT];
		do {
			keys[i] = String.valueOf(ids.get(i));
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
	
	
	public void testList() throws Exception {
		// put object
		Cache cache = RemoteCacheFactoryImpl.getInstance().getCache("List_My_Knowledge_IDD",true);
		for (int i = 0 ; i < 10; i++) {
			String key = "L_" + ids.get(i);
			List<String> list_value = new ArrayList<String>();
			for (int j = 0; j < 100; j++) {
				list_value.add(String.valueOf(j));
			}
			cache.setList(key, list_value);
			logger.info("size : " + cache.lsize(key));
		}
	}
}
