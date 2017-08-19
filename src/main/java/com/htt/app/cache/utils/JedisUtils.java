package com.htt.app.cache.utils;


import com.htt.app.cache.enums.CacheSource;
import com.htt.framework.util.PropertiesUtils;
import com.htt.framework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * jedis缓存工具类
 * Created by sunnyLu on 2017/5/27.
 */
public class JedisUtils {

    public final static int DATA_BASE = 2;
    private final static String HTT = "HTT_";
    public final static Integer ONE_DAY_CACHE=3600*24;
    public final static Integer THREE_DAY_CACHE=3600*24*3;

    static final Map<Integer, JedisPool> pools = new HashMap();
    static String host = PropertiesUtils.getProperty("redis.host");
    static String sPort = PropertiesUtils.getProperty("redis.port");
    static int port = 6379;
    static String password;
    static String sTimeout;
    static int timeout;
    static JedisPoolConfig jedisPoolConfig;

    public JedisUtils() {
    }

    public static Jedis getJedis(int database) {
        JedisPool pool = (JedisPool)pools.get(Integer.valueOf(database));
        if(pool == null) {
            pool = new JedisPool(jedisPoolConfig, host, port, timeout, password, database);
            pools.put(Integer.valueOf(database), pool);
        }

        Jedis jedis = pool.getResource();
        return jedis;
    }

    public static void returnResource(int database, Jedis jedis) {
        JedisPool pool = (JedisPool)pools.get(Integer.valueOf(database));
        pool.returnResource(jedis);
    }

    static {
        if(!StringUtils.isEmpty(sPort) && StringUtils.isNumeric(sPort)) {
            port = StringUtils.stringToInteger(sPort);
        }

        sTimeout = PropertiesUtils.getProperty("redis.timeout");
        timeout = 2000;
        if(!StringUtils.isEmpty(sTimeout) && StringUtils.isNumeric(sTimeout)) {
            timeout = StringUtils.stringToInteger(sTimeout);
        }

        password = PropertiesUtils.getProperty("redis.password");
        if(StringUtils.isEmpty(password)) {
            password = null;
        }

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPoolConfig.setMinIdle(8);//设置最小空闲数
        jedisPoolConfig.setMaxWaitMillis(10000);
        jedisPoolConfig.setTestOnReturn(true);
        //在空闲时检查连接池有效性
        jedisPoolConfig.setTestWhileIdle(true);
        //两次逐出检查的时间间隔(毫秒)
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000);
        //每次逐出检查时 逐出的最大数目
        jedisPoolConfig.setNumTestsPerEvictionRun(10);
        //连接池中连接可空闲的最小时间，会把时间超过minEvictableIdleTimeMillis毫秒的连接断开
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60000);
    }

    public static void hsetexToJedis(String key,String field,String value,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.hset(HTT+key,field,value);
        jedis.expire(HTT + key,THREE_DAY_CACHE);
        returnJedis(dataBase,jedis);
    }

    public static void hsetexToJedis(String key,String field,String value,int expire,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.hset(HTT+key,field,value);
        jedis.expire(HTT + key,expire);
        returnJedis(dataBase,jedis);
    }
    public static void hsetToJedis(String key,String field,String value,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.hset(HTT+key,field,value);
        returnJedis(dataBase,jedis);
    }

    public static String getFromJedis(String key,String field,int dataBase){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            String value = jedis.hget(HTT + key, field);
            return value;
        } finally {
            returnJedis(dataBase,jedis);
        }
    }

    public static Map<String, Object> setSingleCache(Map<String, Object> map ,Integer dataBase,String key){
    	Jedis jedis = null;
    	try{
    		jedis= getJedis(dataBase);
    		String json = FastJsonUtils.parseJson(map);
    		jedis.set(key, json);
            jedis.expire(key, ONE_DAY_CACHE);
    	}finally{
    		if(jedis != null){
    			returnResource(dataBase, jedis);
    		}
    	}
        return map;
    }

    public static Map<String, Object> getSingleCache(Integer dataBase , String key){
		Jedis jedis = null;
		try{
			jedis = getJedis(dataBase);
			String json = jedis.get(key);
			if (StringUtils.isNotEmpty(json)){
				return FastJsonUtils.JsonToMap(json);
			}else{
				return null;
			}
		}finally{
			if (jedis != null){
				returnResource(dataBase, jedis);
			}
		}
	}
    public static Boolean isExists(String key,String field,int dataBase){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            Boolean result = jedis.hexists(HTT + key,field);
            return result;
        } finally {
            returnJedis(dataBase,jedis);
        }
    }

    public static void delKeys(int dataBase,String... keys){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            for (String key : keys){
                jedis.del(HTT+key);
            }
        } finally {
            returnJedis(dataBase,jedis);
        }
    }

    /**
     * 模糊匹配移除key
     * @param dataBase 库索引
     * @param keys
     * @param source 来源 例：pledge-service
     */
    public static void delPatternKeys(int dataBase,Class[] keys,CacheSource source){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            for (Class key : keys){
                Set<String> keySet = getKeysByPattern(jedis,key.getSimpleName(),source);
                if (keySet == null || keySet.size() == 0)
                    continue;
                jedis.del(keySet.toArray(new String[keySet.size()]));
            }
        } finally {
            returnJedis(dataBase,jedis);
        }
    }

    /**
     * 模糊匹配key
     * @param dataBase
     * @param pattern
     * @return
     */
    private static Set<String> getKeysByPattern(Jedis jedis,String pattern,CacheSource source){
        return jedis.keys("*"+source.getDes()+"*"+pattern+"*");
    }

    public static void returnJedis(int dataBase,Jedis jedis){
        if (jedis != null){
            returnResource(dataBase,jedis);
        }
    }
}