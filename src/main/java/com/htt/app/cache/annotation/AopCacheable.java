package com.htt.app.cache.annotation;

import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.enums.ExpiresPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存读取
 * Created by sunnyLu on 2017/7/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopCacheable {
    Class type();//反序列化的类型
    Class[] keys();//对应redis中的key
    int expires() default 0;//过期时间 单位：秒
    String expiresPattern() default ExpiresPattern.PATTERN_COMMON; //过期策略匹配ehcache/redis
    CacheSource source() default CacheSource.NONE;//来源 例：pledge_service
    boolean ehcacheEnable() default false; //是否启用ehcache本地缓存
}
