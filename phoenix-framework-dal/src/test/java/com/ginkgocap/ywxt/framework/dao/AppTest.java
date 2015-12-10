package com.ginkgocap.ywxt.framework.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

import com.ginkgocap.ywxt.framework.dal.dao.Dao;
import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;
import com.ginkgocap.ywxt.framework.dal.dao.impl.CompositeDaoImpl;
import com.ginkgocap.ywxt.framework.dao.id.IdCreator;
import com.ginkgocap.ywxt.framework.dao.id.IdCreatorFactory;
import com.ginkgocap.ywxt.framework.dao.id.exception.CreateIdException;

public class AppTest extends TestCase {
	private static org.apache.log4j.Logger logger = Logger.getLogger(AppTest.class);
	private static IdCreator idCreator  = IdCreatorFactory.getTimeIdCreator(1);
	public static void main1(String[] args) throws DaoException, InterruptedException {
		Dao dao = new CompositeDaoImpl();
		List<Long> ids_list = new ArrayList<Long>();
		//create object , get object
		for (int i = 0; i < 50; i++) {
			User user = new User();
			user.setCreateAt(System.currentTimeMillis());
			user.setUpdateAt(System.currentTimeMillis());
			user.setGender(Short.valueOf("0"));
			user.setPassport("passport" + System.currentTimeMillis());
			//1. save
			Long userId = (Long) dao.save(user);                   
			ids_list.add(userId);
			
			//2. get
			User dbUser = (User) dao.get(User.class, userId);
			assertEquals(dbUser, user);
		}

		
			//3. get Objects by ids
		List<User> users = dao.getList(User.class, ids_list);
		for (User user : users) {
			System.out.println(user);
			user.setPicUrl("1");
			//4. update user
			dao.update(user);
		}
			//check update case
		users = dao.getList(User.class, ids_list);
		for (User user : users) {
			System.out.println(user);
			assertEquals(user.getPicUrl(), "1");
		}
//			//5. delete
//		for (User user : users) {
//			Long userId = user.getUserId();
//			dao.delete(User.class, userId);
//			assertNull(dao.get(User.class, userId));
//		}
		
		
			//6. batch save
		//create object , get object
//		List<User> list_users = new ArrayList<User>();
//		User lastUser = null;
//		for (int i = 0; i < 50; i++) {
//			User user = new User();
//			user.setCreateAt(System.currentTimeMillis());
//			user.setUpdateAt(System.currentTimeMillis());
//			user.setGender(Short.valueOf("3"));
//			user.setPassport("passport" + System.currentTimeMillis());
//			Thread.sleep(50l);
//			lastUser= user;
//			list_users.add(user);
//		}
//		
//		logger.info("begin batch save");
//		dao.batchSave(list_users);
//		logger.info("      batch save ok");
		
		//7 get idList count
		List<Long> ids = dao.getIdList("User_List_userId_gender", Short.valueOf("3"), 0 , 15);
		ids = dao.getIdList("User_List_userId_gender", Short.valueOf("3"));
		System.out.println(ids);
		
		
		//8 map
		
//		System.out.println(dao.getMapping("VctlUser_Map_userId_passport", new Object[]{lastUser.getPassport()}));
		
	}
	public static void main(String[] args) throws DaoException, InterruptedException, CreateIdException {
		Dao dao = new CompositeDaoImpl();
		
		
		User dbUser = (User) dao.get(3887283380158904l, User.class, 3887283380158904l);

		
		List<Long> ids_list = new ArrayList<Long>();
		//create object , get object
		for (int i = 0; i < 60; i++) {
			Long userIdTemp = idCreator.nextId(User.class.getName());
			User user = new User();
			user.setCreateAt(System.currentTimeMillis());
			user.setUpdateAt(System.currentTimeMillis());
			user.setGender(Short.valueOf("0"));
			user.setPassport("passport" + System.currentTimeMillis());
			//1. save
			user.setUserId(userIdTemp);
			Long userId = (Long) dao.save(userIdTemp, user);
			
			
			System.out.println(userId + " " + crc32Value(userId) + " " + userIdTemp + " " + crc32Value(userIdTemp));
			ids_list.add(userId);
			
			//2. get
			dbUser = (User) dao.get(userIdTemp, User.class, userId);
			assertEquals(dbUser, user);
		}

		
			//3. get Objects by ids
//		List<User> users = dao.getList(User.class, ids_list);
//		for (User user : users) {
//			System.out.println(user);
//			user.setPicUrl("1");
//			//4. update user
//			dao.update(user);
//		}
//			//check update case
//		users = dao.getList(User.class, ids_list);
//		for (User user : users) {
//			System.out.println(user);
//			assertEquals(user.getPicUrl(), "1");
//		}
//			//5. delete
//		for (User user : users) {
//			Long userId = user.getUserId();
//			dao.delete(User.class, userId);
//			assertNull(dao.get(User.class, userId));
//		}
//		
//		
//			//6. batch save
//		//create object , get object
////		List<User> list_users = new ArrayList<User>();
////		User lastUser = null;
////		for (int i = 0; i < 50; i++) {
////			User user = new User();
////			user.setCreateAt(System.currentTimeMillis());
////			user.setUpdateAt(System.currentTimeMillis());
////			user.setGender(Short.valueOf("3"));
////			user.setPassport("passport" + System.currentTimeMillis());
////			Thread.sleep(50l);
////			lastUser= user;
////			list_users.add(user);
////		}
////		
////		logger.info("begin batch save");
////		dao.batchSave(list_users);
////		logger.info("      batch save ok");
//		
//		//7 get idList count
//		List<Long> ids = dao.getIdList("User_List_userId_gender", Short.valueOf("3"), 0 , 15);
//		ids = dao.getIdList("User_List_userId_gender", Short.valueOf("3"));
//		System.out.println(ids);
//		
//		
//		//8 map
//		
////		System.out.println(dao.getMapping("VctlUser_Map_userId_passport", new Object[]{lastUser.getPassport()}));
//		
	}
	
	
	public static long crc32Value(Long userId) {
		CRC32 crc32 = new CRC32();
		crc32.update(String.valueOf(userId).getBytes());
		return crc32.getValue();
	}
}
