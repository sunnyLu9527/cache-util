package com.htt.app.cache.utils;



import org.springframework.util.StringUtils;

import java.util.Map;

public class CacheUtils {

    static Map<String,Integer> expireMap;

    static {
        String json = PropertiesUtils.getProperty("rediscache-service");
        if (!StringUtils.isEmpty(json)){
            expireMap = FastJsonUtils.JsonToMap(json,Integer.class);
        }
    }

    /**
     *  获取zk上配置的过期时间
     * @param expireName
     * @return
     */
    public static int getExpire(String expireName){
        return expireMap == null || !expireMap.containsKey(expireName)? 0 : expireMap.get(expireName);
    }
}
