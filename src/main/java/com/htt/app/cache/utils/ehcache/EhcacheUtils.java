package com.htt.app.cache.utils.ehcache;


import com.alibaba.fastjson.JSON;
import com.htt.app.cache.utils.PropertiesUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.List;

/**
 * ehcache工具类
 */
public class EhcacheUtils {
    private static final String path = "/ehcache.xml";

    private URL url;

    private CacheManager manager;

    private static EhcacheUtils ehCache;

    private EhcacheUtils(String path) {
        url = getClass().getResource(path);
        manager = CacheManager.create(url);

        //自动化构建manager，从zookeeper配置ehcache
        String json = PropertiesUtils.getProperty("ehcache-service");
        if (!StringUtils.isEmpty(json)) {
            List<EhcacheCacheConfig> list = JSON.parseArray(json, EhcacheCacheConfig.class);
            if (list!=null && !list.isEmpty()) {
                for (EhcacheCacheConfig row : list) {
                    manager.addCache(row.toCache());
                }
            }
        }
    }

    public static EhcacheUtils getInstance() {
        if (ehCache== null) {
            ehCache= new EhcacheUtils(path);
        }
        return ehCache;
    }

    public void put(String cacheName, String key, Object value) {
        Cache cache = get(cacheName);
        if (cache == null) return;
        Element element = new Element(key, value);
        cache.put(element);
    }

    public String get(String cacheName, String key) {
        Cache cache = get(cacheName);
        if (cache == null) return null;
        Element element = cache.get(key);
        return element == null ? null : element.getObjectValue().toString();
    }

    public Cache get(String cacheName) {
        return manager.getCache(cacheName);
    }

    public Boolean isExists(String cacheName,String key){
        Cache cache = get(cacheName);
        if (cache == null) return false;
        Element element = cache.getQuiet(key);
        return element != null && !element.isExpired();

        //由于没有对Element的状态进行断言，因此Element可能已过期，但此方法仍然返回true。
        //return cache.isElementInMemory(key) || cache.isElementOnDisk(key);
    }

    public void remove(String cacheName, String key) {
        Cache cache = get(cacheName);
        if (cache == null) return;
        cache.remove(key);
    }
}
