package com.htt.app.cache.core;

import com.htt.framework.util.PropertiesUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 *  spring-data-redis 配置类
 *  Created by sunnyLu on 2018/5/7.
 */
@Configuration
@EnableCaching
public class CachingConfig extends CachingConfigurerSupport {

    private final int REDIS_DATA_BASE = 2;
    private final long REDIS_CACHE_EXPIRE = 60*60;//过期时间1h

    private int database = REDIS_DATA_BASE;

    private long expire = REDIS_CACHE_EXPIRE;

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    private static JedisPoolConfig jedisPoolConfig;

    static {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMinIdle(8);
        jedisPoolConfig.setMaxWaitMillis(10000L);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000L);
        jedisPoolConfig.setNumTestsPerEvictionRun(10);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60000L);
    }


    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate){
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
        redisCacheManager.setDefaultExpiration(expire);
        return redisCacheManager;
    }

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> template = new RedisTemplate<String,Object>();
        template.setConnectionFactory(factory);
        //redisTemplate默认使用JdkSerializationRedisSerializer 序列化key 此处使用String来做序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        //redisTemplate默认使用JdkSerializationRedisSerializer 序列化value 此处使用json来做序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory(){
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPoolConfig(jedisPoolConfig);
        factory.setHostName(PropertiesUtils.getProperty("redis.host"));
        factory.setPort(Integer.valueOf(PropertiesUtils.getProperty("redis.port")));
        factory.setTimeout(Integer.valueOf(PropertiesUtils.getProperty("redis.timeout")));
        factory.setPassword(PropertiesUtils.getProperty("redis.password"));
        factory.setDatabase(database);
        factory.afterPropertiesSet();
        return factory;
    }
}
