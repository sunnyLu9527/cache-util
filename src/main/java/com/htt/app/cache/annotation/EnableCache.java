package com.htt.app.cache.annotation;

import com.htt.app.cache.aspect.CacheAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用aop+redis缓存支持
 * Created by sunnyLu on 2019/4/19.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CacheAspect.class)
public @interface EnableCache {
}
