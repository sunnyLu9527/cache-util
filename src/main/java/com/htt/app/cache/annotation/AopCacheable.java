package com.htt.app.cache.annotation;

import com.htt.app.cache.enums.CacheSource;
import com.htt.app.cache.enums.ExpiresPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存读取
 * redis中采用的是hset结构缓存数据，redis的设计决定了hset只能将过期时间设置到key中，所有的field共用一个过期时间
 * 所以如果须要设置过期时间请确保缓存的key是全局所有用户共用的，否则如果多个用户对同一个key操作会出现过期时间重置的情况
 * 对于针对不同用户的缓存，如果实时性要求不高，或者访问量不高的模块，可以适当设置过期时间；对于实时性要求高的数据，请在数据更新时配合使用缓存移除功能
 * @see AopCacheRelease
 *
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
