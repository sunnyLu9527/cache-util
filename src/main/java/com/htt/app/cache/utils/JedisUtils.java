package com.htt.app.cache.utils;


import com.htt.app.cache.annotation.AopCacheRelease;
import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.exception.CacheException;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;

import java.util.*;

/**
 * jedis缓存工具类
 * Created by sunnyLu on 2017/5/27.
 */
public class JedisUtils {

    static Logger logger = LoggerFactory.getLogger(JedisUtils.class);
    public final static int DATA_BASE = 9;
    private final static String HTT = "CACHE_";
    public final static Integer ONE_DAY_CACHE=3600*24;
    public final static Integer THREE_DAY_CACHE=3600*24*3;

    static final Map<Integer, JedisPool> pools = new HashMap();
    static String host = System.getProperty("redis_host");
    static String sPort = System.getProperty("redis_port");
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

    static {
        if(!StringUtils.isEmpty(sPort)) {
            port = Integer.valueOf(sPort);
        }

        sTimeout = System.getProperty("redis_timeout");
        timeout = 2000;
        if(!StringUtils.isEmpty(sTimeout)) {
            timeout = Integer.valueOf(sTimeout);
        }

        password = System.getProperty("redis_password");
        if(StringUtils.isEmpty(password)) {
            password = null;
        }

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);//最大连接数
        jedisPoolConfig.setMaxIdle(100);//最大空闲连接数
        jedisPoolConfig.setTestOnBorrow(true);//在获取连接的时候检查有效性

        jedisPoolConfig.setMinIdle(8);//设置最小空闲数
        jedisPoolConfig.setMaxWaitMillis(10000);//设置连接最大等待时间
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
        returnJedis(jedis);
    }

    public static void hsetexToJedis(String key,String field,String value,int expire,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.hset(HTT+key,field,value);
        jedis.expire(HTT + key,expire);
        returnJedis(jedis);
    }
    public static void hsetToJedis(String key,String field,String value,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.hset(HTT+key,field,value);
        returnJedis(jedis);
    }

    public static void setToJedis(String key,String value,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.set(HTT + key, value);
        returnJedis(jedis);
    }

    public static void setexToJedis(String key,String value,int expire,int dataBase){
        Jedis jedis = getJedis(dataBase);
        jedis.set(HTT + key, value);
        jedis.expire(HTT + key,expire);
        returnJedis(jedis);
    }

    public static String getFromJedis(String key,String field,int dataBase){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            String value = jedis.hget(HTT + key, field);
            return value;
        } finally {
            returnJedis(jedis);
        }
    }

    public static String getFromJedis(String key,int dataBase){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            String value = jedis.get(HTT + key);
            return value;
        } finally {
            returnJedis(jedis);
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
            returnJedis(jedis);
    	}
        return map;
    }

    public static Map<String, Object> getSingleCache(Integer dataBase , String key){
		Jedis jedis = null;
		try{
			jedis = getJedis(dataBase);
			String json = jedis.get(key);
			if (!StringUtils.isEmpty(json)){
				return FastJsonUtils.JsonToMap(json,Object.class);
			}else{
				return null;
			}
		}finally{
            returnJedis(jedis);
		}
	}
    public static Boolean isExists(String key,String field,int dataBase){
        Jedis jedis = null;
        try {
            jedis = getJedis(dataBase);
            Boolean result = jedis.hexists(HTT + key,field);
            return result;
        } finally {
            returnJedis(jedis);
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
            returnJedis(jedis);
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
                List<String> keyList = getKeysByPattern(jedis,key.getSimpleName(),source);
                if (CollectionUtils.isEmpty(keyList))
                    continue;
                jedis.del(keyList.toArray(new String[keyList.size()]));
            }
        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 模糊匹配移除key
     * @param dataBase 库索引
     */
    public static void delPatternFields(JoinPoint joinPoint,int dataBase, AopCacheRelease cacheRelease){
        Jedis jedis = null;
        try {
            StringBuilder match = new StringBuilder();
            Object[] paramValues = joinPoint.getArgs();
            if(paramValues == null){
                return;
            }
            jedis = getJedis(dataBase);
            //获取到所有符合的keys
            List<String> keyList = new LinkedList<>();
            for (Class key : cacheRelease.keys()){
                keyList.addAll(getKeysByPattern(jedis,key.getSimpleName(),cacheRelease.source()));
            }
            if (CollectionUtils.isEmpty(keyList)){
                return;
            }

            //获取到匹配field的字符串
            String fieldPattern = cacheRelease.filedPattern();
            if (fieldPattern.indexOf("#") < 0){
                throw new CacheException("filedPattern is invalid...");
            }
            int index = Integer.valueOf(fieldPattern.substring(fieldPattern.indexOf("{")+1,fieldPattern.indexOf("}")));
            if (fieldPattern.indexOf(".") >= 0){//反射获取值
                String pojoMethod = fieldPattern.substring(fieldPattern.indexOf(".")+1,fieldPattern.length());
                try {
                    match = match.append("*.").append(ReflectionUtils.invokeMethod(paramValues[index],pojoMethod,new Object[0]));
                } catch (Exception e) {
                    logger.warn("Reflection Error:",e);
                }
            } else {
                match = match.append("*.").append(paramValues[index]);
            }
            match = match.append("*");//最后追加一个模糊匹配
            logger.warn("format the match pattern "+match);

            ScanParams scanParams = new ScanParams();
            scanParams.match(match.toString());
            scanParams.count(10);//这里有可能会错误匹配到其他的fields，但是讲道理概率不高，暂时返回十个

            for(String key : keyList){
                logger.warn("get key "+key);
                String cursor = ScanParams.SCAN_POINTER_START;
                List<Map.Entry<String, String>> entryList = new LinkedList<>();
                while (true){
                    ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(key,cursor,scanParams);
                    entryList.addAll(scanResult.getResult());
                    if (ScanParams.SCAN_POINTER_START.equals(scanResult.getStringCursor())){
                        break;
                    }
                    cursor = scanResult.getStringCursor();
                }
                if (CollectionUtils.isEmpty(entryList)){
                    continue;
                }
                String[] filedArray = new String[entryList.size()];
                for (int i = 0; i < entryList.size(); i++) {
                    filedArray[i] = entryList.get(i).getKey();
                    logger.warn(filedArray[i]+" need to be removed");
                }
                jedis.hdel(key,filedArray);
            }

        } finally {
            returnJedis(jedis);
        }
    }

    /**
     * 模糊匹配key
     * @param pattern
     * @return
     */
    private static List<String> getKeysByPattern(Jedis jedis,String pattern,CacheSource source){
        ScanParams params = new ScanParams();
        params.match("*"+source.getDes()+"*"+pattern+"*");
        params.count(100);
        String cursor = ScanParams.SCAN_POINTER_START;
        List<String> keyList = new LinkedList<>();
        while (true){
            ScanResult<String> result = jedis.scan(cursor,params);
            keyList.addAll(result.getResult());
            if (ScanParams.SCAN_POINTER_START.equals(result.getStringCursor())){
                return keyList;
            }
            cursor = result.getStringCursor();
        }
    }

    public static void returnJedis(Jedis jedis){
        if (jedis != null){
            jedis.close();
        }
    }
}
