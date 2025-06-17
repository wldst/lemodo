package com.wldst.ruder.module.event;

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;

public class DataCache {
    private static Logger log = LoggerFactory.getLogger(DataCache.class);

    public static void set(String key, String value) {
	MapDbConnection.set(key, value);
    }

    public static ConcurrentNavigableMap<String, String> getMap() {
	ConcurrentNavigableMap<String, String> map = MapDbConnection.getMap();
	return map;
    }

    public static DB getConnection() {
	return MapDbConnection.getConection();
    }

    public static void commit() {
	MapDbConnection.db.commit();
    }

    public static void rollback() {
	MapDbConnection.db.rollback();
    }

    /**
     * * mapdb连接类
     */

    private static final class MapDbConnection {
	/**
	 * * db存储文件名
	 */

	private static final String DB_FILE_NAME = "dataDb";
	/**
	 * * 存储设备信息
	 */

	private static ConcurrentNavigableMap<String, String> DEVICE_MSG_MAP;
	/**
	 * * 序列化key值
	 */

	private final static String MSG_KEY = "msgKey";
	private static DB db;
	static {
	    // 初始化连接
	    init();
	    db.commit();
	}

	private static void init() {
	    log.debug("开始连接mapdb......");
	    // 开启事务,开启jvm关闭时同时关闭db,开启mmap
	    db = DBMaker.fileDB(new File(DB_FILE_NAME)).fileMmapEnableIfSupported().fileMmapPreclearDisable()
		    .transactionEnable().closeOnJvmShutdown().make();
	    DEVICE_MSG_MAP = db.treeMap(MSG_KEY).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING)
		    .createOrOpen();
	    if (db != null) {
		log.debug("连接mapdb成功......");
	    }
	}

	/**
	 * * 获取连接
	 */
	private synchronized static DB getConection() {
	    if (db.isClosed() || db == null) {
		init();
		if (db.isClosed() || db == null) {
		    throw new NullPointerException("mapdb connection faild");
		}
	    }
	    return db;
	}

	/**
	 * * 获取map * @return
	 */

	private static ConcurrentNavigableMap<String, String> getMap() {
	    getConection();
	    return DEVICE_MSG_MAP;
	}

	/**
	 * * 存值 * @param key * @param value
	 */

	private synchronized static boolean set(String key, String value) {
	    try {
		DEVICE_MSG_MAP.put(key, value);
	    } catch (Exception e) {
		db.rollback();
		log.error("set map faild", e);
		return false;
	    }
	    return true;
	}
    }
}
