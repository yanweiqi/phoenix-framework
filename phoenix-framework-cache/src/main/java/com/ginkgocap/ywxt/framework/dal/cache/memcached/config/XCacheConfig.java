package com.ginkgocap.ywxt.framework.dal.cache.memcached.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ginkgocap.ywxt.framework.dal.cache.config.model.DatasourceItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.KeyPatternItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.QueueItem;
import com.ginkgocap.ywxt.framework.dal.cache.config.model.RegionItem;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.MemcachedClientProvider;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.RoutingXMemcachedClientProvider;
import com.ginkgocap.ywxt.framework.dal.cache.memcached.provider.XMemcachedClientProvider;

/**
 * 
 * @author allenshen
 * @date 2015年11月3日
 * @time 上午9:12:49
 * @Copyright Copyright©2015 www.gintong.com
 */
public final class XCacheConfig {
	private static Logger logger = Logger.getLogger(XCacheConfig.class);
	public static String CONFIG_FILE = "/memcached_client.xml";
	private static XCacheConfig config;

	private Map<String, DatasourceItem> memcachedItemMap = new HashMap<String, DatasourceItem>();
	private Map<String, QueueItem> queueItemMap = new HashMap<String, QueueItem>();
	private Map<String, RegionItem> regionItemMap = new HashMap<String, RegionItem>();
	private Map<String, MemcachedClient> memcachedClientMap = new HashMap<String, MemcachedClient>();
	private RegionItem defaultRegionItem = null;
	private RoutingXMemcachedClientProvider dynamicMemcachedClientProvider = new RoutingXMemcachedClientProvider();
	private Map<String, MemcachedClientProvider> memcachedClientProviderMap = new ConcurrentHashMap<String, MemcachedClientProvider>();

	private XCacheConfig() {
		try {
			this.init(getClass().getResourceAsStream(CONFIG_FILE));
			this.initDefaultRegion(getClass().getResourceAsStream(CONFIG_FILE));
			this.initDynamicMemcachedSource();
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				e.printStackTrace(System.out);
			} else {
				logger.error("init config file " + CONFIG_FILE + " error: " + e.getMessage());
			}
		}
	}

	private void initDynamicMemcachedSource() {
		RoutingXMemcachedClientProvider memcachedSource = (RoutingXMemcachedClientProvider) dynamicMemcachedClientProvider;
		memcachedSource.setResolvedMemcachedSources(memcachedClientMap);
	}

	public static XCacheConfig getInstance() {
		if (config == null) {
			synchronized (XCacheConfig.class) {
				if (config == null) {
					config = new XCacheConfig();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						public void run() {
							config.close();
						}
					});
				}
			}
		}

		return config;
	}

	private void init(InputStream groupConfigFile) {
		Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("root", ArrayList.class);

		// 初始化MemcachedItem配置
		digester.addObjectCreate("root/cache/", ArrayList.class.getName());
		digester.addObjectCreate("root/cache/datasource", DatasourceItem.class);
		digester.addSetProperties("root/cache/datasource");
		digester.addSetNext("root/cache/datasource", "add");

		digester.addSetNext("root/cache/", "add");

		//
		digester.addObjectCreate("root/regions/", ArrayList.class.getName());
		digester.addObjectCreate("root/regions/region", RegionItem.class);
		digester.addSetProperties("root/regions/region");
		digester.addSetNext("root/regions/region", "add");

		digester.addObjectCreate("root/regions/region/keyPattern", KeyPatternItem.class);
		digester.addSetProperties("root/regions/region/keyPattern");
		digester.addSetNext("root/regions/region/keyPattern", "addKeyPatternItem");

		digester.addSetNext("root/regions/", "add");

		digester.addObjectCreate("root/queues/", ArrayList.class.getName());
		digester.addObjectCreate("root/queues/queue/", QueueItem.class);
		digester.addSetProperties("root/queues/queue/");
		digester.addSetNext("root/queues/queue", "add");
		digester.addSetNext("root/queues/", "add");

		try {
			Object root = digester.parse(groupConfigFile);
			if (root != null && root instanceof ArrayList) {
				List<List> list = (List) root;
				if (CollectionUtils.isEmpty(list)) {
					return;
				}
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					List arrayList = (List) iterator.next();
					if (CollectionUtils.isNotEmpty(arrayList)) {
						for (Iterator itemIterator = arrayList.iterator(); itemIterator.hasNext();) {
							Object item = itemIterator.next();
							if (item != null) {
								if (item instanceof DatasourceItem) {
									addDatasourceItem((DatasourceItem) item);
									continue;
								}
								if (item instanceof RegionItem) {
									addRegionItem((RegionItem) item);
								}
								if (item instanceof QueueItem) {
									addQueueItem((QueueItem) item);
								}

							}

						}
					}

				}

			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				e.printStackTrace(System.err);
			} else {
				logger.error("init error: " + e.getMessage());
			}
		} catch (SAXException e) {
			if (logger.isDebugEnabled()) {
				e.printStackTrace(System.err);
			} else {
				logger.error("init parse fail : " + e.getMessage());
			}
		}
	}

	private void initDefaultRegion(InputStream groupConfigFile) {
		Digester digester = new Digester();
		digester.setValidating(false);
		// digester.addObjectCreate("root", ArrayList.class);
		//
		// // 初始化MemcachedItem配置
		// digester.addObjectCreate("root/cache/", ArrayList.class.getName());
		// digester.addObjectCreate("root/cache/datasource",
		// DatasourceItem.class);
		// digester.addSetProperties("root/cache/datasource");
		// digester.addSetNext("root/cache/datasource", "add");
		//
		// digester.addSetNext("root/cache/", "add");
		//
		// //
		// digester.addObjectCreate("root/regions/", ArrayList.class.getName());
		digester.addObjectCreate("root/default/region", RegionItem.class);
		digester.addSetProperties("root/default/region");

		digester.addObjectCreate("root/default/region/keyPattern", KeyPatternItem.class);
		digester.addSetProperties("root/default/region/keyPattern");
		digester.addSetNext("root/default/region/keyPattern", "addKeyPatternItem");

		// digester.addSetNext("root/regions/", "add");

		try {
			Object root = digester.parse(groupConfigFile);
			if (root instanceof RegionItem) {
				this.defaultRegionItem = (RegionItem) root;
			}

		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				e.printStackTrace(System.err);
			} else {
				logger.error("initDefaultRegion error: " + e.getMessage());
			}
		} catch (SAXException e) {
			if (logger.isDebugEnabled()) {
				e.printStackTrace(System.err);
			} else {
				logger.error("initDefaultRegion parse fail : " + e.getMessage());
			}
		}
	}

	/**
	 * 简单测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(XCacheConfig.getInstance().getMemcachedItem("photo1"));
		System.out.println(XCacheConfig.getInstance().getRegionItem("com.sohu.sns.dal.test.pojo.Photo"));
		System.out.println(XCacheConfig.getInstance().getQueueItem("default"));
	}

	/**
	 * 
	 * @param achememcachedItemdItem
	 */
	public void addDatasourceItem(DatasourceItem datasourceItem) {
		if (datasourceItem != null && StringUtils.isNotBlank(datasourceItem.getName())) {
			if (!memcachedItemMap.containsKey(datasourceItem.getName())) {
				memcachedItemMap.put(datasourceItem.getName(), datasourceItem);
				// init xmemcached client
				MemcachedClient client = buildeMemcachedClient(datasourceItem);
				memcachedClientMap.put(datasourceItem.getName(), client);
			} else {
				logger.error("same name <" + datasourceItem.getName() + "> memcacheditem have exist!");
			}

		}
	}

	/**
	 * 
	 * @param achememcachedItemdItem
	 */
	private void addRegionItem(RegionItem regionItem) {
		if (regionItem != null && StringUtils.isNotBlank(regionItem.getName())) {
			if (!regionItemMap.containsKey(regionItem.getName())) {
				regionItemMap.put(regionItem.getName(), regionItem);
			} else {
				logger.error("same name <" + regionItem.getName() + "> RegionItem have exist!");
			}

		}
	}

	private void addQueueItem(QueueItem queueItem) {
		if (queueItem != null && StringUtils.isNotBlank(queueItem.getName())) {
			if (!queueItemMap.containsKey(queueItem.getName())) {
				queueItemMap.put(queueItem.getName(), queueItem);
			} else {
				logger.error("same name <" + queueItem.getName() + "> QueueItem have exist!");
			}
		}
	}

	/**
	 * 
	 * @param groupName
	 * @return
	 */
	public RegionItem getRegionItem(String regionName) {
		return this.regionItemMap.get(regionName);
	}

	/**
	 * 
	 * @param memcachedItemName
	 * @return
	 */
	public DatasourceItem getMemcachedItem(String memcachedItemName) {
		return this.memcachedItemMap.get(memcachedItemName);
	}

	public QueueItem getQueueItem(String queueItemName) {
		return this.queueItemMap.get(queueItemName);
	}

	public MemcachedClientProvider getMemcachedProvider(String name) {
		MemcachedClientProvider provider = null;
		if(StringUtils.isNotBlank(name)) {
			provider = memcachedClientProviderMap.get(name);
		}
	
		if (provider == null) {
			MemcachedClient client = memcachedClientMap.get(name);
			if (client != null) {
				provider = new XMemcachedClientProvider(client);
				memcachedClientProviderMap.put(name, provider);
			} 
		}
	
		return provider;
	}

	public Map<String, MemcachedClient> getMemcachedSourceMap() {
		return memcachedClientMap;
	}

	public MemcachedClientProvider getDynamicMemcachedClientProvider() {
		return dynamicMemcachedClientProvider;
	}

	public void close() {
		if (MapUtils.isNotEmpty(memcachedClientMap)) {
			Collection<MemcachedClient> c = memcachedClientMap.values();
			for (Iterator iterator = c.iterator(); iterator.hasNext();) {
				MemcachedClient client = (MemcachedClient) iterator.next();
				if (client != null) {
					try {
						client.shutdown();
						logger.info("memcached client : " + client.getConnector().getLocalSocketAddress() + " close.");
					} catch (Exception e) {
						;
					}
				}

			}
		}
	}

	public RegionItem getDefaultRegionItem() {
		return defaultRegionItem;
	}

	private static MemcachedClient buildeMemcachedClient(DatasourceItem datasourceItem) {
		if (datasourceItem == null) {
			throw new IllegalArgumentException("DatasourceItem must have value");
		}
		if (StringUtils.isBlank(datasourceItem.getServer())) {
			throw new IllegalArgumentException(datasourceItem.getName() + " server property must have value");
		}

		if (datasourceItem.getPort() <= 0) {
			throw new IllegalArgumentException(datasourceItem.getName() + "port property must have value and greater than zero");
		}

		String hostPort = StringUtils.trim(datasourceItem.getServer()) + ":" + String.valueOf(datasourceItem.getPort());
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(hostPort));
		builder.setConnectionPoolSize(datasourceItem.getMaxActive() == 0 ? MemcachedClient.DEFAULT_CONNECTION_POOL_SIZE : datasourceItem.getMaxActive());
		builder.setConnectTimeout(datasourceItem.getTimeout() == 0 ? MemcachedClient.DEFAULT_CONNECT_TIMEOUT : datasourceItem.getTimeout());
		builder.setSanitizeKeys(true);
		// builder.setStateListeners(stateListeners);
		MemcachedClient client = null;

		try {
			client = builder.build();

		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		return client;
	}

}
