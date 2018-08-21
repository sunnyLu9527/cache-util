package com.htt.app.cache.handler;

import com.htt.app.cache.annotation.AopCacheable;
import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.utils.FastJsonUtils;
import com.htt.app.cache.utils.JedisUtils;
import com.htt.app.cache.utils.ehcache.EhcacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 基于redis的缓存
 */
public class RedisCacheHandler extends CacheHandler {

    private CacheHandler cacheHandler;

    @Override
    public CacheHandler setHandler(CacheHandler handler) {
        this.cacheHandler = handler;
        return this;
    }

    @Override
    protected Object readCache(ProceedingJoinPoint jp, AopCacheable cacheRead) throws Throwable{
        Class[] keyClasses = cacheRead.keys();
        // 得到类名、方法名和参数
        String clazzName = jp.getTarget().getClass().getName();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        // 根据类名，方法名和参数生成field
        String field = genFiled(clazzName, methodName, args);
        // 生成key
        String key = genKey(keyClasses,methodName,cacheRead.source());
        String ecacheKey = genEcacheKey(clazzName,methodName,args,cacheRead.source());
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
            if (cacheRead.ehcacheEnable()){//放入ehcache
                EhcacheUtils.getInstance().put("eternalCache",ecacheKey,json);
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

}
