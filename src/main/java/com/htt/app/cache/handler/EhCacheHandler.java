package com.htt.app.cache.handler;

import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.enums.ExpiresPattern;
import com.htt.app.cache.utils.ehcache.EhcacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 基于Ehcache的本地缓存
 * 作为redis之上的二级缓存，目的是减轻redis端的压力
 * 由于会占用jvm的内存空间，不建议在请求量未达到一个量级的时候开启二级缓存
 */
public class EhCacheHandler extends CacheHandler {

    private CacheHandler cacheHandler;

    @Override
    public CacheHandler setHandler(CacheHandler handler) {
        this.cacheHandler = handler;
        return this;
    }

    @Override
    protected Object readCache(ProceedingJoinPoint jp, AopCacheable cacheRead) throws Throwable{
        // 得到类名、方法名和参数
        String clazzName = jp.getTarget().getClass().getName();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        String ecacheKey = genEcacheKey(clazzName,methodName,args,cacheRead.source());
        Object result = null;

        if (EhcacheUtils.getInstance().isExists(cacheRead.expiresPattern(),ecacheKey)){
            // 缓存命中
            // 得到被代理方法的返回值类型
            Class returnType = ((MethodSignature) jp.getSignature()).getReturnType();
            String jsonString = EhcacheUtils.getInstance().get(cacheRead.expiresPattern(),ecacheKey);
            if("null".equalsIgnoreCase(jsonString)){
                return null;
            }
            result = deserialize(jsonString, returnType, cacheRead.type());
            return result;
        }

        if (cacheHandler != null){
            result = cacheHandler.readCache(jp, cacheRead);
        }
        return result;
    }

}
