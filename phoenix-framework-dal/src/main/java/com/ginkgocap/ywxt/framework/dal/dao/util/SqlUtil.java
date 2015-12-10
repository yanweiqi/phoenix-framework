package com.ginkgocap.ywxt.framework.dal.dao.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;

import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.ListItem;
import com.ginkgocap.ywxt.framework.dal.dao.config.model.dao.MapItem;
import com.ginkgocap.ywxt.framework.dal.dao.model.LsCacheInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.ScalarInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.SqlInfo;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:42
 * @Copyright Copyright©2015 www.gintong.com
 */
public class SqlUtil {
	private static Log log = LogFactory.getLog(SqlUtil.class);

	private static TypeResolver typeResolver = new TypeResolver();

	/**
	 * 
	 * @param mappingName
	 * @param params
	 * @param bExtend
	 *            :用于决定是否是扩展类型的Map ,对于扩展类型的Map，允许在一个Map中返回多个值
	 * @return
	 */
	public static SqlInfo getMappingSqlInfo(String mappingName, Object[] params, boolean bExtend) {
		SqlInfo sqlInfo = null;
		MapItem mapItem = DaoHelper.getMapItemByMapName(mappingName);
		String sql = mapItem.getSqlitem();
		if (!bExtend) {
			String column = DaoHelper.getMapItemValueColumnName(mappingName);
			Type type = getType(DaoHelper.getMapItemValueClass(mappingName).getName());
			ScalarInfo scalarInfo = new ScalarInfo(column, type);
			List<ScalarInfo> scaList = new ArrayList<ScalarInfo>();
			scaList.add(scalarInfo);
			sqlInfo = new SqlInfo(sql, params, scaList);
		} else {
			sqlInfo = new SqlInfo(sql, params, null);
		}

		return sqlInfo;
	}

	public static SqlInfo getListSql(String listName, Object[] params) {
		SqlInfo sqlInfo = null;
		String sql = null;
		final ListItem listItem = DaoHelper.getListItemByListName(listName);
		sql = listItem.getSqlitem();

		String iDColumn = listItem.getColumnName();
		if (iDColumn.toLowerCase().contains("distinct ".toLowerCase())) {
			iDColumn = iDColumn.replaceAll("distinct", "");
		}
		Type type = getType(DaoHelper.getListItemValueClass(listName).getName());

		ScalarInfo scalarInfo = new ScalarInfo(iDColumn.trim(), type);
		List<ScalarInfo> scaList = new ArrayList<ScalarInfo>();
		scaList.add(scalarInfo);

		sqlInfo = new SqlInfo(sql, params, scaList);
		// 取出要查询到的栏位的信息
		return sqlInfo;
	}

	public static SqlInfo getListSql(LsCacheInfo lsHelper) {
		if (null == lsHelper || null == lsHelper.getRegion()) {
			return null;
		} else {
			return getListSql(lsHelper.getRegion(), lsHelper.getParams());
		}
	}

	public static SqlInfo getListCountSql(String listName, Object[] params) {
		SqlInfo sqlInfo = null;
		String sql = null;
		final ListItem listItem = DaoHelper.getListItemByListName(listName);
		sql = listItem.getSqlcountitem();
		sqlInfo = new SqlInfo(sql, params, null);

		// 取出要查询到的栏位的信息
		return sqlInfo;
	}

	public static SqlInfo getListCountSql(LsCacheInfo lsHelper) {
		return getListCountSql(lsHelper.getRegion(), lsHelper.getParams());
	}

	// 用于得到聚合需要的执行的SqlInfo 的List
	public static List<SqlInfo> getMapAggreSqlInfos(String mapName, List<Object[]> paramsList) {
		final MapItem mapItem = DaoHelper.getMapItemByMapName(mapName);
		return getUnionSql(paramsList, mapItem.getSqlitem(), null);
	}

	public static List<SqlInfo> getUnionSql(List<Object[]> paramsList, final String baseSql, List<Type> returnTypes) {
		List<SqlInfo> sqlInfoList = new ArrayList<SqlInfo>();
		final int unionCnt = 30;
		int leftCnts = paramsList.size() % unionCnt;
		int fullCnts = paramsList.size() / unionCnt;

		for (int i = 0; i < fullCnts; i++) {
			String sql = getAggrSql(baseSql, unionCnt);
			int start = i * unionCnt;
			int end = (i + 1) * unionCnt;
			Object[] realParams = getAggrParams(paramsList.subList(start, end));
			if (null == realParams) {
				continue;
			}
			SqlInfo sqlInfo = new SqlInfo(sql, realParams, null);
			sqlInfoList.add(sqlInfo);
		}
		// 所有不足30的组合成一个union all语句

		if (leftCnts > 0) {
			String sql = getAggrSql(baseSql, leftCnts);
			paramsList = paramsList.subList((fullCnts * unionCnt), paramsList.size());
			Object[] realParams = getAggrParams(paramsList);
			if (null != realParams) {
				SqlInfo sqlInfo = new SqlInfo(sql, realParams, null);
				sqlInfoList.add(sqlInfo);
			}
		}
		return sqlInfoList;
	}

	public static List<SqlInfo> getEntityUnionSql(List idList, final String baseSql) {
		List<SqlInfo> sqlInfoList = null;
		if (idList.size() > 0) {
			List<Object[]> paramsList = new ArrayList<Object[]>();
			for (Object id : idList) {
				if (null != id) {
					paramsList.add(new Object[] { id });
				}
			}
			if (paramsList.size() > 0) {
				return getUnionSql(paramsList, baseSql, null);
			}

		}
		return sqlInfoList;
	}

	public static String getAggrSql(String sql, int count) {
		StringBuffer allSql = new StringBuffer();
		for (int i = 0; i < count; i++) {
			if (i < count - 1) {
				allSql.append(sql).append(" union all ");
			} else {
				allSql.append(sql);
			}
		}
		return allSql.toString();
	}

	public static Object[] getAggrParams(List<Object[]> paramsList) {
		List aggrList = new ArrayList();
		for (Object[] params : paramsList) {
			for (Object param : params) {
				if (null == param) {
					return null;
				}
				aggrList.add(param);
			}
		}
		return aggrList.toArray();
	}

	public static Type getType(String className) {
		if (className != null) {
			return typeResolver.basic(className);
		}
		return null;
	}

	public static void main(String[] args) {
		List newList = new ArrayList();
		for (int i = 21; i > 0; i--) {
			newList.add(new Object[] { 1 });
		}
		List<SqlInfo> sqlInfoList = getUnionSql(newList, "select * from tables where id =?", null);
		for (SqlInfo sqlIfo : sqlInfoList) {
			System.out.println(" =========Sql ========\r\n");
			System.out.println(sqlIfo.getSql());
		}
	}

	//
	//
	// 正式上线时用的是这个数组
	// public static int[] numArray = new
	// int[]{500,400,300,200,100,90,80,70,60,50,40,30,20,10,9,8,7,6,5,4,3,2,1};

	// 这是初次的思路，实际运行时是肯定不可能的
	// /**
	// *
	// * @param paramsList
	// * @param baseSql
	// * @param returnTypes --- sql 语句查询结果的类型
	// * @return
	// */
	// public static List<SqlInfo> getUnionSql(List<Object[]> paramsList,final
	// String baseSql,List<Type> returnTypes) {
	// List<SqlInfo> sqlInfoList= new ArrayList<SqlInfo>();
	// for(int keyNum : numArray){
	// int lestSize = paramsList.size() / keyNum;
	// if( 0 == lestSize){
	// continue;
	// }
	// for(int i=0;i<lestSize;i++){
	// String sql = getAggrSql(baseSql, keyNum);
	// int start = i * keyNum;
	// int end = (i+1)*keyNum;
	// Object[] realParams = getAggrParams(paramsList.subList(start, end));
	// if(null == realParams){
	// continue;
	// }
	// SqlInfo sqlInfo = new SqlInfo(sql,realParams,null);
	// sqlInfoList.add(sqlInfo);
	// }
	// if(keyNum == paramsList.size() ){
	// break;
	// }
	// paramsList = paramsList.subList((keyNum-1),paramsList.size()-1);
	// }
	// return sqlInfoList;
	// }

}
