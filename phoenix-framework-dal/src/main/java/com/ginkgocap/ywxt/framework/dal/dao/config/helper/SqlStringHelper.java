package com.ginkgocap.ywxt.framework.dal.dao.config.helper;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.common.reflection.ClassLoadingException;
import org.hibernate.annotations.common.util.ReflectHelper;
import org.hibernate.annotations.common.util.StandardClassLoaderDelegateImpl;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:17:14
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class SqlStringHelper {
    private Logger logger = Logger.getLogger(SqlStringHelper.class);

    /**
     * 给出 删除的 SQL 从Select语句中
     * 
     * @param selectSql
     * @return
     */
    public static String getDelSql(String selectSql) {
        String result = null;
        if (StringUtils.isNotEmpty(selectSql)) {
            String sTemp = selectSql.replace("  ", " ");
            // Fix 大小写的SQL
            result = sTemp.replaceAll("select (.+?) from", "delete from");
        }
        return result;
    }

    /**
     * 给出 统计总数的 SQL 从Sqlect语句中。
     * 
     * @param selectSql
     * @return
     */
    public static String getCountSql(String selectSql) {
        String result = null;
        if (StringUtils.isNotEmpty(selectSql)) {
            String sTemp = selectSql.replace("  ", " ");
            // Fix 大小写的SQL
            result = sTemp.replaceAll("select (.+?) from", "select count($1) from");
        }
        return result;
    }

    /**
     * 给出SQL的Orderby 字段
     * 
     * @param selectSql
     * @return
     */
    public static String[] getOrderbyFields(String selectSql) {
        String[] result = null;
        if (StringUtils.isNotEmpty(selectSql)) {
            String orderBy = null;
//            String sTemp = selectSql.replace("  ", " ");
            // Fix 大小写的SQL
            Pattern patternOrder = Pattern.compile("order by ([^desc|^asc|].+)");
            Matcher matcher = patternOrder.matcher(selectSql);
            if (matcher.find()) {
                orderBy = matcher.group(1);
            } else { // 如果没有找到，就找Select 中的字段，默认
                patternOrder = Pattern.compile("select (.+?) from");
                matcher = patternOrder.matcher(selectSql);
                if (matcher.find()) {
                    orderBy = matcher.group(1);
                }
            }
            result = StringUtils.split(orderBy, " ");
        }
        return result;
    }

    /**
     * 给出Aggregate SQL，就是把 Order by 字段 也放在 Select 字段中的SQL
     * 
     * @param selectSql
     * @return
     */

    public static String getAggregateSql(String selectSql) {
        String result = null;
        if (StringUtils.isNotEmpty(selectSql)) {
//            String sTemp = selectSql.replace("  ", " ");
            String orderByFields = StringUtils.join(getOrderbyFields(selectSql), " ");
            Pattern pattern = Pattern.compile("select (.+?) from");
            Matcher matcher = pattern.matcher(selectSql);
            StringBuffer sb = new StringBuffer();
            if (matcher.find()) {
                matcher.appendReplacement(sb, new StringBuilder().append("select ").append(matcher.group(1)).append(
                        " ,").append(orderByFields).append(" from").toString());
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    public static String getLoadObjectSql(String className) {
        try {
            String id = null;
            String table = null;
            Class clazz = StandardClassLoaderDelegateImpl.INSTANCE.classForName(className);
            javax.persistence.Table annotation = (javax.persistence.Table) clazz.getAnnotation(javax.persistence.Table.class);
            if (annotation != null) {
                table = annotation.name();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                javax.persistence.Id idAnnotation = method.getAnnotation(javax.persistence.Id.class);
                if (idAnnotation != null) {
                    Column column = method.getAnnotation(javax.persistence.Column.class);
                    if (column != null) {
                        id = column.name();
                    } else {
                        id = method.getName();
                        id = id.length() > 3 ? StringUtils.uncapitalize(method.getName().substring(3)) : null;
                    }
                    break;
                }
            }
            if (StringUtils.isNotBlank(table) && StringUtils.isNotBlank(id)) {
                return "select * from " + table + " where " + id + "=?";
            } else {
                return null;
            }
        } catch (ClassLoadingException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public static void main(String[] args) {
        String sql = "select id from photo order by username";
        System.out.println("AggregateSQL: \t" + getAggregateSql(sql));
        System.out.println("delDel: \t" + getDelSql(sql));
        System.out.println("countSQL: \t" + getCountSql(sql));

        //System.out.println(getLoadObjectSql(com.sohu.sns.dal.test.pojo.Photo.class.getName()));
    }



}
