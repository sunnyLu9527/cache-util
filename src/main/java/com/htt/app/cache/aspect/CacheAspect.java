package com.htt.app.cache.aspect;

import com.htt.app.cache.handler.CacheHandlerFactory;
import com.htt.app.cache.utils.JedisUtils;
import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.annotation.AopCacheRelease;
import com.htt.app.cache.exception.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * 缓存切面类
 * Created by sunnyLu on 2017/7/18.
 */
@Aspect  //申明一个切面
@EnableAspectJAutoProxy //启用@Aspect支持，默认使用jdk动态代理，基于接口，设为true则启用cglib的动态代理
public class CacheAspect {

    @Pointcut(value = "@annotation(cacheRead)",argNames = "cacheRead")
    public void pointcut(AopCacheable cacheRead){}

    @Pointcut(value = "@annotation(cacheRelease)",argNames = "cacheRelease")
    public void pointcut2(AopCacheRelease cacheRelease){}


    @Around(value = "pointcut(cacheRead)")
    public Object readCache(ProceedingJoinPoint jp,AopCacheable cacheRead) throws Throwable{
        //TODO redis缓存层异常 接口不能失败
        CacheHandlerFactory handlerFactory = new CacheHandlerFactory().getInstance(cacheRead);
        return handlerFactory.readCache(jp, cacheRead);
    }

    @AfterReturning(value = "pointcut2(cacheRelease)")
    public void releaseCache(AopCacheRelease cacheRelease){
        //得到key
        Class[] keys = cacheRelease.keys();
        if (keys == null || keys.length == 0){
            throw new CacheException("the annotation '"+cacheRelease.getClass().getSimpleName()+"' must be contains the attribute keys");
        } else if (cacheRelease.source() == null){
            throw new CacheException("the annotation '"+cacheRelease.getClass().getSimpleName()+"' must be contains the attribute source");
        }
        // 清除对应缓存
        JedisUtils.delPatternKeys(JedisUtils.DATA_BASE, keys,cacheRelease.source());
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

