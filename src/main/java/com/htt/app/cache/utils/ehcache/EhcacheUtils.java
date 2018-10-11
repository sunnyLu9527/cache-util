package com.htt.app.cache.utils.ehcache;


import com.alibaba.fastjson.JSON;
import com.htt.framework.util.PropertiesUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.List;

public class EhcacheUtils {
    private static final String path = "/ehcache.xml";

    private URL url;

    private CacheManager manager;

    private static EhcacheUtils ehCache;

    private EhcacheUtils(String path) {
        url = getClass().getResource(path);
        manager = CacheManager.create(url);

        //从zookeeper配置ehcache
        String json = PropertiesUtils.getProperty("ehcache-service");
        if (StringUtils.isNotBlank(json)) {
            List<CacheConfig> list = JSON.parseArray(json, CacheConfig.class);
            if (list!=null && !list.isEmpty()) {
                for (CacheConfig row : list) {
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
        Element element = new Element(key, value);
        cache.put(element);
    }

    public String get(String cacheName, String key) {
        Cache cache = get(cacheName);
        Element element = cache.get(key);
        return element == null ? null : element.getObjectValue().toString();
    }

    public Cache get(String cacheName) {
        return manager.getCache(cacheName);
    }

    public Boolean isExists(String cacheName,String key){
        Cache cache = get(cacheName);
        Element element = cache.getQuiet(key);
        return element != null && !element.isExpired();

        //由于没有对Element的状态进行断言，因此Element可能已过期，但此方法仍然返回true。
        //return cache.isElementInMemory(key) || cache.isElementOnDisk(key);
    }

    public void remove(String cacheName, String key) {
        Cache cache = get(cacheName);
        cache.remove(key);
    }
}
