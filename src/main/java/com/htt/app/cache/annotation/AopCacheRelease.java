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
    String filedPattern() default "";  //匹配须要移除的field，基本数据类型请使用#{0};对象类型请使用#{0}.getXXX TODO filedPattern只支持单个,移除存在误差，多个的情况太复杂，须要配合使用组合注解来实现精确移除指定的field,目前暂不支持
    CacheSource source() default CacheSource.NONE;//来源 例：Zeus
}
