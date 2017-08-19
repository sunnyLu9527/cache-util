package com.htt.app.cache.annotation;

import com.htt.app.cache.enums.CacheSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存释放
 * Created by sunnyLu on 2017/7/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopCacheRelease {
    Class[] keys();//对应redis中的key
    CacheSource source();//来源 例：pledge_service
}
