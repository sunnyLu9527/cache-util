package com.htt.app.cache.aspect;

import com.htt.app.cache.utils.FastJsonUtils;
import com.htt.app.cache.utils.JedisUtils;
import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.annotation.AopCacheRelease;
import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.exception.CacheException;
import com.htt.framework.util.PagingResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.List;
import java.util.Map;

/**
 * 缓存切面类
 * Created by sunnyLu on 2017/7/18.
 */
@Aspect
public class CacheAspect {

    @Pointcut(value = "@annotation(cacheRead)",argNames = "cacheRead")
    public void pointcut(AopCacheable cacheRead){}

    @Pointcut(value = "@annotation(cacheRelease)",argNames = "cacheRelease")
    public void pointcut2(AopCacheRelease cacheRelease){}


    @Around(value = "pointcut(cacheRead)")
    public Object readCache(ProceedingJoinPoint jp,AopCacheable cacheRead) throws Throwable{
        Class[] keyClasses = cacheRead.keys();
        if (cacheRead.source() == null){
            throw new CacheException("the annotation '"+cacheRead.getClass().getSimpleName()+"' must be contains the attribute source");
        } else if (keyClasses == null || keyClasses.length == 0){
            throw new CacheException("the annotation '"+cacheRead.getClass().getSimpleName()+"' must be contains the attribute keys");
        }

        // 得到类名、方法名和参数
        String clazzName = jp.getTarget().getClass().getName();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        // 根据类名，方法名和参数生成field
        String field = genFiled(clazzName, methodName, args);
        // 生成key
        String key = genKey(keyClasses,methodName,cacheRead.source());

        // result是方法的最终返回结果
        Object result = null;
        // 检查redis中是否有缓存
        if (!JedisUtils.isExists(key,field,JedisUtils.DATA_BASE)) {
            // 缓存未命中
            // 调用数据库查询方法
            result = jp.proceed(args);

            // 序列化查询结果
            String json = FastJsonUtils.parseJson(result);

            // 序列化结果放入缓存
            if (cacheRead.expires() > 0){
                JedisUtils.hsetexToJedis(key,field,json,cacheRead.expires(),JedisUtils.DATA_BASE);
            } else {
                JedisUtils.hsetToJedis(key, field, json, JedisUtils.DATA_BASE);
            }
        } else {
            // 缓存命中
            // 得到被代理方法的返回值类型
            Class returnType = ((MethodSignature) jp.getSignature()).getReturnType();

            // 反序列化从缓存中拿到的json
            String jsonString = JedisUtils.getFromJedis(key,field,JedisUtils.DATA_BASE);
            result = deserialize(jsonString, returnType, cacheRead.type());
        }

        return result;
    }

    private Object deserialize(String jsonString, Class returnType, Class modelType) {
        // 序列化结果应该是List对象
        if (returnType.isAssignableFrom(List.class)) {
            return FastJsonUtils.JsonToList(jsonString,modelType);
        } else if (returnType.isAssignableFrom(Map.class)){
            return FastJsonUtils.JsonToMap(jsonString,modelType);
        } else if (returnType.isAssignableFrom(PagingResult.class)){
            return FastJsonUtils.JsonToPagingResult(jsonString,modelType);
        } else {
            // 序列化
            return FastJsonUtils.JsonToEntity(jsonString,returnType);
        }

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

    /**
     * 根据类名、方法名和参数生成filed
     * @param clazzName
     * @param methodName
     * @param args 方法参数
     * @return
     */
    private String genFiled(String clazzName, String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder(clazzName).append(".").append(methodName);
        for (Object obj : args) {
            if (obj != null)
                sb.append(".").append(obj.toString());
        }

        return sb.toString();
    }

    /**
     * 根据类名；来源生成key
     * @param source
     * @return
     */
    private String genKey(Class[] keyClasses,String methodName,CacheSource source){
        StringBuilder sb = new StringBuilder(source.getDes()).append(".").append(methodName);
        for (Class clazz : keyClasses){
            sb.append(".").append(clazz.getSimpleName());
        }
        return sb.toString();
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
