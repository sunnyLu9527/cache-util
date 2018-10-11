package com.htt.app.cache.utils;


import com.htt.framework.util.PropertiesUtils;

import java.util.Map;

public class RedisUtils {

    static Map<String,Integer> expireMap;

    static {
        String json = PropertiesUtils.getProperty("rediscache-service");
        expireMap = FastJsonUtils.JsonToMap(json,Integer.class);
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
