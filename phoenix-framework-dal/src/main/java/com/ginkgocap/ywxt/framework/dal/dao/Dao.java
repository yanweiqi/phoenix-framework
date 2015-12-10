package com.ginkgocap.ywxt.framework.dal.dao;

import java.io.Serializable;
import java.util.List;

import com.ginkgocap.ywxt.framework.dal.dao.exception.DaoException;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:21:48
 * @Copyright Copyright©2015 www.gintong.com
 */
public interface Dao {
	/**
	 * 保存一个持久化对象
	 * 当保存一个持久化对象时，需要指定一个account_id（用户账号ID），这个account_id可以帮助我们把同一个用户的数据保存在一起
	 * （同一机器，同一数据库，等）。 在使用是注意，在实现改接口是注意检查数据的有效性，比如保存的对象是不是指定account_id的对象。
	 * 
	 * @param account_id
	 *            ：用户Account_id,
	 *            在DAL中用他来作为策略的依据参数，如果是NULL策略会默认处理，比如数据字典的数据，不存在account_id
	 *            ,策略就会选择默认的方式处理
	 * @param object
	 *            ：持久化对象
	 * @return Serializable： 返回保存的持久化对象的ID
	 * @throws Exception
	 */
	public Serializable save(Object account_id, Object object) throws DaoException;

	/**
	 * 对于不需要存储策略的数据，如数据字典，采用如下方式保存
	 * 
	 * @param object
	 *            ：带保存对象
	 * @return
	 * @throws DaoException
	 */
	public Serializable save(Object object) throws DaoException;

	/**
	 * 保存一组持久化对象
	 * 当批量保存持久化对象时，需要指定一个account_id（用户账号ID），这个account_id可以帮助我们把同一个用户的数据保存在一起
	 * （同一机器，同一数据库，等）。
	 * 在使用是注意，一组对象最好是同一个账号的同一类型数据，在实现改接口是注意检查数据的有效性，比如保存的对象是不是指定account_id的对象。
	 * 
	 * @param account_id
	 *            ：用户Account_id 如果account_id是null就会保存或者更新默认的库和CACHE
	 * @param objects
	 *            ：要保存的持久化对象列表
	 * @return List ：返回保存的持久化对象列表
	 * @throws DaoException
	 */
	public List save(Object account_id, List objects) throws DaoException;

	/**
	 * 对于不需要存储策略的数据，如数据字典，采用如下方式保存
	 * 
	 * @param objects
	 *            :对象数组
	 * @return
	 * @throws DaoException
	 */
	public List save(List objects) throws DaoException;

	/**
	 * 批量保存所使用的方法
	 * 
	 * @param account_id
	 * @param objList
	 * @return
	 * @throws DaoException
	 */
	public List batchSave(Object account_id, List objList) throws DaoException;

	public List batchSave(List objList) throws DaoException;

	/**
	 * 保存或者更新，如果要保存的对象已经在持久层中存在就更新，否则插入新的记录。
	 * 需要指定一个account_id（用户账号ID），这个account_id可以帮助我们把同一个用户的数据保存在一起
	 * （同一机器，同一数据库，等）。如果输入是null就会保存或者更新默认的库和CACHE
	 * 在使用是注意，在实现改接口是注意检查数据的有效性，比如保存或者更新的对象是不是指定account_id的对象。。
	 * 
	 * @param object
	 *            ：要保存或者更新的持久化对象
	 * @throws DaoException
	 */
	// public boolean saveOrUpdate(Long account_id, Object object) throws
	// DaoException;
	/**
	 * 从DB或者CACHE中装载一个对象，需要指定一个account_id（用户ID）以方便策略选择从那个库或者CACHE中装载数据，
	 * 如果输入是null就从默认的库或CACHE中转载 注意：检查装载的数据中的account_id和指定的account_id是否一致。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param clazz
	 *            ：要重载的持久化对象类别，
	 * @param id
	 *            ：持久化对象的标识
	 * @return Object ：如果存在返回持久化对象。否则发回NLL
	 * @throws DaoException
	 */
	public Object get(Object account_id, Class clazz, Serializable id) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式读取
	 * 
	 * @param clazz
	 * @param id
	 *            ：对象的主键
	 * @return
	 * @throws DaoException
	 */
	public Object get(Class clazz, Serializable id) throws DaoException;

	/**
	 * 通过给出指定持久化对象标识的列表查询持久化对象，需要指定一个account_id（用户ID）以方便策略选择从那个库或者CACHE中查询数据,
	 * 如果输入是null就从默认的库和CACHE中查询 注意：在实现改接口是要保证返回的持久化对象确实是指定账号的。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param clazz
	 *            ：要装载的持久化对象。
	 * @param ids
	 *            ：要装载的持久化对象标识列表
	 * @return List ：返回装载的持久化对象，对象的类型是参数clazz指定的Class
	 * @throws DaoException
	 */

	public List getList(Object account_id, Class clazz, List ids) throws DaoException;

	public List getList(Class clazz, List ids) throws DaoException;

	/**
	 * 更新指定用户的持久化对象，参数account_id（用户ID）以方便策略选择更新哪个库或者CACHE中数据,
	 * 如果输入是null就从默认的库和CACHE中更新 注意：检查要更新对象是否是指定账号的。
	 * 
	 * @param object
	 *            ：要更新的持久化对象
	 * @return Object 持久化对象
	 * @throws DaoException
	 */
	public boolean update(Object account_id, Object object) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式更新
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean update(Object object) throws DaoException;

	/**
	 * 更新指定account_id（账号）的一批持久化对象，参数account_id（用户ID）以方便策略选择更新哪个库或者CACHE中数据,
	 * 如果输入是null就从默认的库和CACHE中更新
	 * 
	 * @param objects
	 *            ：要更新的持久化对象列表
	 * @return List 持久化对象列表
	 * @throws DaoException
	 */

	public boolean batchUpdate(Object account_id, List objects) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式更新
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean batchUpdate(List objects) throws DaoException;

	/**
	 * 删除一个指定account_id（用户）一个持久化对象，参数account_id（用户ID）以方便策略选择删除哪个库或者CACHE中数据，
	 * 如果输入是null就从默认的库和CACHE中删除 注意：在删除以前要保证数据确实是account_id用户的
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param clazz
	 *            : 要删除的持久化对象的类型
	 * @param id
	 *            ：要删除的持久化对象的标识
	 * @return boolean ： 如果删除持久化对象成功返回true，否则false
	 * @throws DaoException
	 */
	public boolean delete(Object account_id, Class clazz, Serializable id) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式删除
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean delete(Class clazz, Serializable id) throws DaoException;

	/**
	 * 删除一个指定account_id（用户）一个持久化对象，参数account_id（用户ID）以方便策略选择删除哪个库或者CACHE中数据，
	 * 如果输入是null就从默认的库和CACHE中删除 注意：在删除以前要保证数据确实是account_id用户的
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param clazz
	 *            : 要删除的持久化对象的类型
	 * @param id
	 *            ：要删除的持久化对象的标识
	 * @return boolean ： 如果删除持久化对象成功返回true，否则false
	 * @throws DaoException
	 */
	public boolean fakeDelete(Object account_id, Class clazz, Serializable id) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式删除
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean fakeDelete(Class clazz, Serializable id) throws DaoException;

	/**
	 * 删除一个指定account_id（用户）的多个持久化对象，参数account_id（用户ID）以方便策略选择删除哪个库或者CACHE中数据，
	 * 如果输入是null就从默认的库和CACHE中删除 注意：在删除以前要保证数据确实是account_id用户的
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param clazz
	 *            : 要删除的持久化对象的类型
	 * @param ids
	 *            ：要删除的持久化对象的标识列表
	 * @return boolean： 如果删除成功返回true，否则返回false
	 * @throws DaoException
	 */
	public boolean deleteList(Object account_id, Class clazz, List ids) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式批量删除
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean deleteList(Class clazz, List ids) throws DaoException;

	/**
	 * 删除一个指定account_id（用户）的多个持久化对象，参数account_id（用户ID）以方便策略选择删除哪个库或者CACHE中数据，
	 * 如果输入是null就从默认的库和CACHE中删除 注意：在删除以前要保证数据确实是account_id用户的
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param clazz
	 *            : 要删除的持久化对象的类型
	 * @param ids
	 *            ：要删除的持久化对象的标识列表
	 * @return boolean： 如果删除成功返回true，否则返回false
	 * @throws DaoException
	 */
	public boolean fakeDeleteList(Object account_id, Class clazz, List ids) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式批量删除
	 * 
	 * @param object
	 * @return
	 * @throws DaoException
	 */
	public boolean fakeDeleteList(Class clazz, List ids) throws DaoException;

	/**
	 * 根据list_name 自动转化为对应的delete 语句，执行删除。
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param list_name
	 *            ：待删除的List所对应的list_name
	 * @param params
	 *            : 执行list_name对应sql语句，所需要的参数
	 * @return
	 */
	public boolean deleteList(Object account_id, String list_name, Object[] params) throws DaoException;

	public boolean deleteList(String list_name, Object[] params) throws DaoException;

	public boolean deleteList(Object account_id, String list_name, Object param) throws DaoException;

	public boolean deleteList(String list_name, Object param) throws DaoException;

	/**
	 * 根据list_name 自动转化为对应的delete 语句，执行删除。
	 * 
	 * @param account_id
	 *            ：用户Account_id，
	 * @param list_name
	 *            ：待删除的List所对应的list_name
	 * @param params
	 *            : 执行list_name对应sql语句，所需要的参数
	 * @return
	 */
	public boolean fakeDeleteList(Object account_id, String list_name, Object[] params) throws DaoException;

	public boolean fakeDeleteList(String list_name, Object[] params) throws DaoException;

	public boolean fakeDeleteList(Object account_id, String list_name, Object param) throws DaoException;

	public boolean fakeDeleteList(String list_name, Object param) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表,需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中选择数据，
	 * 如果输入是null就从默认的库或者CACHE中选择。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param list_name
	 *            : list的名称
	 * @param param
	 *            : 列表中的关键字(标识)
	 * @param start
	 *            : 指定列表的中的开始位置
	 * @param count
	 *            : 装载的长度。如果存在的数据小于count就返回实际的count数据
	 * @return
	 * @throws DaoException
	 */
	public List getIdList(Object account_id, String list_name, Object param, Integer start, Integer count) throws DaoException;

	/**
	 * 对于保存在单库的数据，如数据字典，采用如下方式查询
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param list_name
	 *            : list的名称
	 * @param param
	 *            : 列表中的关键字(标识)
	 * @param start
	 *            : 指定列表的中的开始位置
	 * @param count
	 *            : 装载的长度。如果存在的数据小于count就返回实际的count数据
	 * @param forward
	 *            ：装载的反向，是向前还是向后
	 * @return
	 * @throws DaoException
	 */
	public List getIdList(String list_sql_name, Object param, Integer start, Integer count) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表,需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中选择数据，
	 * 如果输入是null就从默认的库或者CACHE中选择。
	 * 
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param list_name
	 *            : list的名称
	 * @param param
	 *            : 列表中的关键字(标识)
	 * @param forward
	 *            ：装载的反向，是向前还是向后
	 * @return
	 * @throws DaoException
	 */
	public List getIdList(Object account_id, String list_name, Object param) throws DaoException;

	public List getIdList(String list_name, Object param) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public List getIdList(Object account_id, String list_name, Object[] params, Integer start, Integer count) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public List getIdList(String list_name, Object[] params, Integer start, Integer count) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public List getIdList(Object account_id, String list_name, Object[] params) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public List getIdList(String list_name, Object[] params) throws DaoException;

	public List getIdList(Object accountId, String mapName, List<Object[]> paramsList) throws DaoException;

	public List getIdList(String mapName, List<Object[]> paramsList) throws DaoException;

	public List getAggrIdList(Object account_id, String list_name, List<Object[]> paramsList, int start, int count) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public int count(Object account_id, String list_name, Object param) throws DaoException;

	public int count(String list_name, Object param) throws DaoException;

	/**
	 * 查询指定param（key)的对象列表的长度。
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 *            需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据,如果输入是null就从默认的库或者CACHE中删除
	 * @param list_name
	 *            ：要查询的列表名称
	 * @param param
	 *            ：列表的标识ID
	 * @return 列表的长度
	 * @throws DaoException
	 */
	public int count(Object account_id, String list_name, Object[] params) throws DaoException;

	public int count(String list_name, Object[] params) throws DaoException;

	/**
	 * 通过一个持久化对象的属性值找一个持久化对象。需要指定account_id(用户ID)以方便策略选择从哪个库或者CACHE中查询数据，
	 * 如果输入是null就从默认的库或者CACHE中查询
	 * 
	 * @param account_id
	 *            ：用户Account_id
	 * @param mappingName
	 *            ：持久化对象属性的名称标识
	 * @param key
	 *            ：对象的属性值
	 * @return ：持久化对象
	 * @throws DaoException
	 */
	public Object getMapping(Object account_id, String mappingName, Object[] keys) throws DaoException;

	public Object getMapping(String mappingName, Object[] keys) throws DaoException;

	public Object getMapping(Object account_id, String mappingName, Object key) throws DaoException;

	public Object getMapping(String mappingName, Object key) throws DaoException;

	public List getMappings(Object accountId, String mapName, List<Object[]> paramsList) throws DaoException;

	//
	//
	//
	//
	// public Object[] getObjProperties(Object account_id, String mappingName,
	// Object[] keys) throws DaoException;
	//
	// public Object[] getObjProperties(String mappingName, Object[] keys)
	// throws DaoException;
	//
	// public Object getObjProperties(Object account_id, String mappingName,
	// Object key) throws DaoException;
	//
	// public Object getObjProperties(String mappingName, Object key) throws
	// DaoException;
	//

	public List getMapList(List accountIdList, String mapname, List<Object[]> paramsList) throws DaoException;

}
