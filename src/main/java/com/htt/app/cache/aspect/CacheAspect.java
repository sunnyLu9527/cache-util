package com.htt.app.cache.aspect;

import com.htt.app.cache.annotation.AopCacheGroup;
import com.htt.app.cache.handler.CacheHandlerFactory;
import com.htt.app.cache.utils.FastJsonUtils;
import com.htt.app.cache.utils.JedisUtils;
import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.annotation.AopCacheRelease;
import com.htt.app.cache.exception.CacheException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 缓存切面类
 * Created by sunnyLu on 2017/7/18.
 */
@Aspect  //申明一个切面
@EnableAspectJAutoProxy //启用@Aspect支持，默认使用jdk动态代理，基于接口，设为true则启用cglib的动态代理
public class CacheAspect {

    Logger logger = LoggerFactory.getLogger(CacheAspect.class);

    @Pointcut(value = "@annotation(cacheRead)",argNames = "cacheRead")
    public void pointcut(AopCacheable cacheRead){}

    @Pointcut(value = "@annotation(cacheRelease)",argNames = "cacheRelease")
    public void pointcut2(AopCacheRelease cacheRelease){}

    @Pointcut(value = "@annotation(cacheGroup)",argNames = "cacheGroup")
    public void pointcut3(AopCacheGroup cacheGroup){}


    @Around(value = "pointcut(cacheRead)")
    public Object readCache(ProceedingJoinPoint jp,AopCacheable cacheRead) throws Throwable{
        try {
            CacheHandlerFactory handlerFactory = new CacheHandlerFactory().getInstance(cacheRead);
            return handlerFactory.readCache(jp, cacheRead);
        } catch (Throwable throwable) {
            logger.error("Cache Error:",throwable);
            return jp.proceed(jp.getArgs());
        }
    }

    @AfterReturning(value = "pointcut2(cacheRelease)")
    public void releaseCache(JoinPoint joinPoint,AopCacheRelease cacheRelease){
        if (StringUtils.isEmpty(cacheRelease.filedPattern())){//移除所有匹配class的key
            JedisUtils.delPatternKeys(JedisUtils.DATA_BASE, cacheRelease.keys(),cacheRelease.source());
        } else { //移除所有匹配class的key中匹配的fields
            JedisUtils.delPatternFields(joinPoint,JedisUtils.DATA_BASE,cacheRelease);
        }
    }

    @AfterReturning(value = "pointcut3(cacheGroup)")
    public void releaseCacheGroup(JoinPoint joinPoint,AopCacheGroup cacheGroup){
        if (cacheGroup.releaseGroup().length <= 0) return;
        for (AopCacheRelease cacheRelease : cacheGroup.releaseGroup()){
            JedisUtils.delPatternFields(joinPoint,JedisUtils.DATA_BASE,cacheRelease);
        }
    }

//    @AfterReturning("pointcut(parameter)")
//    public void afterExecute(String parameter){
//    }
//
//    @AfterThrowing(pointcut = "pointcut()", throwing = "ex")
//    // 声明异常，StudentMgr类的update方法出现异常时执行
//    public void printException(Exception ex) {
//
//        System.out.println("执行update方法时发生错误" + ex.getMessage());
//
//    }
//
//    @After("pointcut()")
//    public void endTransaction() throws Throwable {
//        System.out.println("结束事务");
//    }
//
//    @Around("pointcut()")
//    // 声明环绕通知，拦截StudentMgr的所有以update开头的方法
//    public Object doSurround(ProceedingJoinPoint _pjp) throws Throwable {
//        return null;
//    }

}

