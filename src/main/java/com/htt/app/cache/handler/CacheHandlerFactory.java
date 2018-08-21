package com.htt.app.cache.handler;

import com.htt.app.cache.annotation.AopCacheable;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * CacheHndler工厂构造类
 * @author sunnyLu
 */
public class CacheHandlerFactory extends CacheHandler {

    private CacheHandler cacheHandler;

    @Override
    protected CacheHandler setHandler(CacheHandler handler) {
        this.cacheHandler = handler;
        return this;
    }

    public CacheHandlerFactory getInstance(AopCacheable cacheRead){
        CacheHandler handler = null;
        if (cacheRead.ehcacheEnable()){
            handler = new EhCacheHandler().setHandler(new RedisCacheHandler());
        } else {
            handler = new RedisCacheHandler();
        }
        this.setHandler(handler);
        return this;
    }

    @Override
    public Object readCache(ProceedingJoinPoint jp, AopCacheable cacheRead) throws Throwable {
        return this.cacheHandler.readCache(jp, cacheRead);
    }
}
