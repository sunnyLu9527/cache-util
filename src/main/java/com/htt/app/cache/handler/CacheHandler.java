package com.htt.app.cache.handler;

import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.utils.FastJsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.List;
import java.util.Map;

/**
 * 抽象缓存类
 * @author sunnyLu
 */
public abstract class CacheHandler {

    protected abstract CacheHandler setHandler(CacheHandler handler);

    protected abstract Object readCache(ProceedingJoinPoint jp, AopCacheable cacheRead) throws Throwable;

    public Object deserialize(String jsonString, Class returnType, Class modelType) {
        // 序列化结果应该是List对象
        if (returnType.isAssignableFrom(List.class)) {
            return FastJsonUtils.JsonToList(jsonString,modelType);
        } else if (returnType.isAssignableFrom(Map.class)){
            return FastJsonUtils.JsonToMap(jsonString,modelType);
        } else {
            // 序列化
            return FastJsonUtils.JsonToEntity(jsonString,returnType);
        }

    }

    public String genEcacheKey(String clazzName, String methodName, Object[] args,CacheSource source) {
        StringBuilder sb = new StringBuilder(source.getDes()).append(clazzName).append(".").append(methodName);
        for (Object obj : args) {//TODO 如果是对象的话，提供field支持
            if (obj != null)
                sb.append(".").append(obj.toString());
        }

        return sb.toString();
    }

}
