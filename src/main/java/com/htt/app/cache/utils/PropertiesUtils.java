package com.htt.app.cache.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.Properties;

/**
 * TODO 目前无法从外部包引入properties，做成分布式配置中心
 */
public class PropertiesUtils {

    static Properties properties;

    static String path = "app.properties";

    static Logger logger = LogManager.getLogger(PropertiesUtils.class);

    static void init(String path){
        ResourceLoader loader = new DefaultResourceLoader();
        Resource res = loader.getResource("classpath*:"+path);
        InputStream in = null;
        try {
            in = res.getInputStream();
            properties = new Properties();
            properties.load(in);
        } catch (Exception e) {
            logger.error("properties init error:",e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                logger.error("properties init error:",e);
            }
        }
    }

    public static String getProperty(String key){
        if (properties == null)
            init(path);
        String value = null;
        try {
            value = properties.getProperty(key);
        } catch (Exception e) {
            logger.error("properties error:",e);
        }
        return value;
    }
}
