package com.ginkgocap.ywxt.framework.dal.dao.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.ginkgocap.ywxt.framework.dal.dao.DBAgent;
import com.ginkgocap.ywxt.framework.dal.dao.config.helper.DaoHelper;
import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;
import com.ginkgocap.ywxt.framework.dal.dao.model.ScalarInfo;
import com.ginkgocap.ywxt.framework.dal.dao.model.SqlInfo;
/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:19:49
 * @Copyright Copyright©2015 www.gintong.com
 */
public class DBAgentHibernateImpl implements DBAgent {
	Log log = LogFactory.getLog(DBAgentHibernateImpl.class);

	private static DBAgent dbAgent;

	public static final int DEFAULT_SIZE = 5000;

	private SessionFactory sessionFactory;

	private HibernateTemplate hibernateTemplate;

	private DBAgentHibernateImpl() {
		sessionFactory = DaoHelper.getSessionFactory();
		hibernateTemplate = new HibernateTemplate(sessionFactory);
		// hibernateTemplate.setCheckWriteOperations(false);
	}

	public static DBAgent getInstance() {
		if (null == dbAgent) {
			synchronized (DBAgentHibernateImpl.class) {
				dbAgent = new DBAgentHibernateImpl();
			}
		}
		return dbAgent;
	}

	public Serializable save(Object account_id, final Object object) throws Exception {
		return hibernateTemplate.execute(new HibernateCallbackTransaction<Serializable>() {
			@Override
			public Serializable doInHibernateTransaction(Session session) throws HibernateException {
				return session.save(object);
			}
		});
	}

	public Map save(Object account_id, final List obs) throws Exception {
		Map map = hibernateTemplate.execute(new HibernateCallback<Map>() {
			@Override
			public Map<Serializable, Object> doInHibernate(Session session) throws HibernateException {
				Map<Serializable, Object> idObjMap = new HashMap<Serializable, Object>();
				Transaction trans = session.beginTransaction();
				try {
					Serializable id = null;
					for (Object obj : obs) {
						id = session.save(obj);
						idObjMap.put(id, obj);
					}
					session.flush();
					session.clear();
					trans.commit();
				} catch (Exception e) {
					trans.rollback();
					throw new HibernateException(e);
				}
				return idObjMap;
			}
		});

		return map;
	}

	public boolean update(Object account_id, final Object object) throws Exception {
		return hibernateTemplate.execute(new HibernateCallbackTransaction<Boolean>() {
			@Override
			public Boolean doInHibernateTransaction(Session session) throws HibernateException {
				session.update(object);
				return Boolean.TRUE;
			}
		});
	}

	public boolean delete(Object account_id, final Object obj) throws Exception {
		return hibernateTemplate.execute(new HibernateCallbackTransaction<Boolean>() {
			@Override
			public Boolean doInHibernateTransaction(Session session) throws HibernateException {
				session.delete(obj);
				return Boolean.TRUE;
			}
		});
	}

	public int count(Object account_id, String listName, final SqlInfo sqlInfo) throws Exception {
		Integer count = hibernateTemplate.execute(new HibernateCallbackTransaction<Integer>() {
			public Integer doInHibernateTransaction(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sqlInfo.getSql());
				if (null != sqlInfo.getParams() && sqlInfo.getParams().length > 0) {
					int i = 0;
					for (Object id : sqlInfo.getParams()) {
						if (null != id) {
							query.setParameter(i++, id);
						}
					}
				}
				
				Object result = query.uniqueResult();
				return result == null ? 0: Integer.parseInt(query.uniqueResult().toString());
			}
		});
		return count;
		
	}

	public <T> T get(Object account_id, final Class<T> clazz, final Serializable id) throws DaoException {
		return (T) hibernateTemplate.execute(new HibernateCallbackTransaction<T>() {
			@Override
			public T doInHibernateTransaction(Session session) throws HibernateException {
				return session.get(clazz, id);
			}
		});
	}

	public List getIdList(Object account_id, String listName, final SqlInfo sqlInfo, final Integer start, final Integer count) throws Exception {
		List list = null;
		list = (List) hibernateTemplate.execute(new HibernateCallbackTransaction() {
			@Override
			public Object doInHibernateTransaction(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sqlInfo.getSql());
				if (null != sqlInfo.getScalarList() && sqlInfo.getScalarList().size() > 0) {
					for (ScalarInfo scaInfo : sqlInfo.getScalarList()) {
						if (null == scaInfo.getColumnType()) {
							query.addScalar(scaInfo.getColumnName());
						} else {
							query.addScalar(scaInfo.getColumnName(), scaInfo.getColumnType());
						}
					}
				}
				if (null != sqlInfo.getParams() && sqlInfo.getParams().length > 0) {
					int i = 0;
					for (Object id : sqlInfo.getParams()) {
						if (null != id) {
							query.setParameter(i++, id);
						}
					}
				}
				query.setFirstResult(start.intValue());
				query.setMaxResults(count.intValue());
				return query.list();
			}
		});

		return list == null ? new ArrayList() : list;
	}

	public Object getMapping(Object account_id, String listName, final SqlInfo sqlInfo) throws Exception {
		return hibernateTemplate.execute(new HibernateCallbackTransaction<Object>() {
			@Override
			public Object doInHibernateTransaction(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sqlInfo.getSql());
				if (null != sqlInfo.getScalarList() && sqlInfo.getScalarList().size() > 0) {
					for (ScalarInfo scaInfo : sqlInfo.getScalarList()) {
						if (null == scaInfo.getColumnType()) {
							query.addScalar(scaInfo.getColumnName());
						} else {
							query.addScalar(scaInfo.getColumnName(), scaInfo.getColumnType());
						}
					}
				}
				if (null != sqlInfo.getParams() && sqlInfo.getParams().length > 0) {
					int i = 0;
					for (Object id : sqlInfo.getParams()) {
						if (null != id) {
							query.setParameter(i++, id);
						}
					}
				}
				return query.uniqueResult();
			}
		});
	}

	public <T> List<T> getObjectList(Object account_id, String listName, final SqlInfo sqlInfo) throws Exception {
		return hibernateTemplate.execute(new HibernateCallbackTransaction<List<T>>() {
			@Override
			public List<T> doInHibernateTransaction(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sqlInfo.getSql());
				if (null != sqlInfo.getScalarList() && sqlInfo.getScalarList().size() > 0) {
					for (ScalarInfo scaInfo : sqlInfo.getScalarList()) {
						if (null == scaInfo.getColumnType()) {
							query.addScalar(scaInfo.getColumnName());
						} else {
							query.addScalar(scaInfo.getColumnName(), scaInfo.getColumnType());
						}
					}
				}
				if (null != sqlInfo.getParams() && sqlInfo.getParams().length > 0) {
					int i = 0;
					for (Object id : sqlInfo.getParams()) {
						if (null != id) {
							query.setParameter(i++, id);
						}
					}
				}
				List<T> result = query.list();
				return result == null ? new ArrayList<T>() : result;
			}
		});
	}

	public <T> List<T> getEntityList(Object account_id, final Class cls, final SqlInfo sqlInfo, int strategy) throws DaoException {
		return  hibernateTemplate.execute(new HibernateCallbackTransaction<List<T>>() {
			@Override
			public List<T> doInHibernateTransaction(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sqlInfo.getSql()).addEntity(cls);
				if (null != sqlInfo.getParams() && sqlInfo.getParams().length > 0) {
					int i = 0;
					for (Object id : sqlInfo.getParams()) {
						if (null != id) {
							query.setParameter(i++, id);
						}
					}
				}
				List<T> result =  query.list();
				return result == null ? new ArrayList<T>():result;
			}});
	}

	private void processException(Exception e) throws DaoException {
		if (e instanceof MappingException) {
			throw new DaoException(DaoException.POJO_NOTFOUND_EXCEPTION, e);
		} else if (e instanceof NullPointerException) {
			throw new DaoException(DaoException.NULLPOINTER_EXCEPTION, e);
		} else if (e instanceof SQLException) {
			throw new DaoException(DaoException.SQL_EXCEPTION, e);
		} else if (e instanceof HibernateException) {
			throw new DaoException(DaoException.Hibernate_Exception, e);
		} else if (e instanceof DaoException) {
			throw (DaoException) e;
		} else {
			throw new DaoException(e);
		}
	}

	public static abstract class HibernateCallbackTransaction<T> implements HibernateCallback<T> {
		@Override
		public T doInHibernate(Session session) throws HibernateException {
			Transaction tr = null;
			try {
				tr = session.beginTransaction();
				return doInHibernateTransaction(session);
			} catch (HibernateException e) {
				if (tr != null) {
					tr.rollback();
				}
				e.printStackTrace(System.err);
				throw e;
			} finally {
				if (tr != null) {
					tr.commit();
				}
			}
		}

		public abstract T doInHibernateTransaction(Session session) throws HibernateException;
	}

}
