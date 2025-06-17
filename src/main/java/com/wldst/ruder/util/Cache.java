package com.wldst.ruder.util;

import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: 缓存工具类
 * 1.部分方法未验证，如有问题请自行修改
 * 2.其他方法请自行添加
 *
 */
public class Cache {

    /**
     * 屏蔽工具类的无参构造 避免工具类被实例化
     */
    private Cache(){}

    /**
     * 缓存留存期 30min 1H 24H
     */
    public static final long CACHE_HOLD_TIME_30M = 30 * 60 * 1000L;
    public static final long CACHE_HOLD_TIME_1M = 60 * 1000L;
    public static final long CACHE_HOLD_TIME_1H = 2 * CACHE_HOLD_TIME_30M;
    public static final long CACHE_HOLD_TIME_24H = 24 * CACHE_HOLD_TIME_1H;
    public static final long CACHE_HOLD_TIME_FOREVER = -1L;

    /**
     * 缓存容量、最少使用容量
     */
    private static final int CACHE_MAX_CAP = 1000;
    private static final int CLEAN_LRU_CAP = 800;

    /**
     * 缓存当前大小
     */
    private static AtomicInteger CACHE_CURRENT_SIZE = new AtomicInteger(0);

    /**
     * 缓存对象
     */
    private static final Map<String,Node> CACHE_MAP = new ConcurrentHashMap<>(CACHE_MAX_CAP);

    /**
     * 最少使用记录
     */
    private static final List<String> LRU_LIST = new LinkedList<>();

    /**
     * 自动清理标志位
     */
    private static volatile boolean CLEAN_RUN_FLAG = false;

    /**
     * 默认30MIN
     * @param key
     * @param val
     */
    public static void put(String key,Object val){
        put(key,val,CACHE_HOLD_TIME_30M);
    }

    /**
     * 添加永久缓存
     * @param key
     * @param val
     */
    public static void putForever(String key,Object val){
        put(key,val,CACHE_HOLD_TIME_FOREVER);
    }

    /**
     * 添加缓存
     * @param key
     * @param val
     * @param ttlTime
     */
    public static void put(String key,Object val,long ttlTime){
        if (!StringUtils.hasLength(key) || null == val){
            return;
        }
        checkSize();
        updateCacheLru(key);
        CACHE_MAP.put(key,new Node(val,ttlTime));
    }

    /**
     * 获取缓存信息
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T get(String key,Class<T> clazz){
        if (!StringUtils.hasLength(key) || !CACHE_MAP.containsKey(key)){
            return null;
        }
        updateCacheLru(key);
        return (T) CACHE_MAP.get(key).getVal();
    }

    /**
     * 更新最近使用位置
     * @param key
     */
    private static void updateCacheLru(String key){
        synchronized (LRU_LIST){
            LRU_LIST.remove(key);
            LRU_LIST.add(0,key);
        }
    }

    /**
     * 删除,成功则容量-1
     * @param key
     */
    private static boolean remove(String key){
        Node node = CACHE_MAP.remove(key);
        if (null!=node){
            CACHE_CURRENT_SIZE.getAndDecrement();
            return true;
        }
        return false;
    }

    /**
     * 检查是否超过容量,先清理过期,在清理最少使用
     */
    private static void checkSize(){
        if (CACHE_CURRENT_SIZE.intValue() > CACHE_MAX_CAP){
            deleteTimeOut();
        }
        if (CACHE_CURRENT_SIZE.intValue() > CLEAN_LRU_CAP){
            deleteLru();
        }
    }

    /**
     * 删除最久未使用,尾部删除
     * 永久缓存不会被清除
     */
    private static void deleteLru(){
        synchronized (LRU_LIST){
            while (LRU_LIST.size() > CLEAN_LRU_CAP){
                int lastIndex = LRU_LIST.size() - 1;
                String key = LRU_LIST.get(lastIndex);
                if (!CACHE_MAP.get(key).isForever() && remove(key)){
                    LRU_LIST.remove(lastIndex);
                }
            }
        }
    }

    /**
     * 删除过期
     */
    private static void deleteTimeOut(){
        List<String> del = new LinkedList<>();
        for (Map.Entry<String,Node> entry:CACHE_MAP.entrySet()){
            if (entry.getValue().isExpired()){
                del.add(entry.getKey());
            }
        }
        for (String k:del){
            remove(k);
        }
    }

    /**
     * 缓存是否已存在,过期则删除返回False
     * @param key
     * @return
     */
    public static boolean contains(String key){
        if (CACHE_MAP.containsKey(key)){
            if (!CACHE_MAP.get(key).isExpired()){
                return true;
            }
            if (remove(key)){
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 清空缓存
     */
    public static void clear(){
        CACHE_MAP.clear();
        CACHE_CURRENT_SIZE.set(0);
        LRU_LIST.clear();
    }

    /**
     * 重置自动清理标志
     * @param flag
     */
    public static void setCleanRunFlag(boolean flag){
        CLEAN_RUN_FLAG = flag;
    }

    /**
     * 自动清理过期缓存
     */
    private static void startAutoClean(){

        if (!CLEAN_RUN_FLAG){
            setCleanRunFlag(true);
            ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledExecutor.scheduleAtFixedRate(()->{
                try {
                    Cache.setCleanRunFlag(true);
                    while (CLEAN_RUN_FLAG){
                        Cache.deleteTimeOut();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },10,Cache.CACHE_HOLD_TIME_1H, TimeUnit.SECONDS);
        }
    }

    /**
     * 缓存对象类
     */
    public static class Node{
        /**
         * 缓存值
         */
        private Object val;
        /**
         * 过期时间
         */
        private long ttlTime;

        public Node(Object val,long ttlTime){
            this.val = val;
            if (ttlTime<0){
                this.ttlTime = ttlTime;
            }else{
                this.ttlTime = System.currentTimeMillis() + ttlTime;
            }
        }

        public Object getVal(){
            return this.val;
        }

        public boolean isExpired(){
            if (this.ttlTime<0){
                return false;
            }
            return System.currentTimeMillis() > this.ttlTime;
        }

        public boolean isForever(){
            if (this.ttlTime<0){
                return true;
            }
            return false;
        }

    }


}


