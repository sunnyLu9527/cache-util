package com.htt.app.cache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.htt.framework.util.PagingResult;

import java.util.*;

/**
 * fastjson序列化反序列化
 * Created by sunnyLu on 2017/2/14.
 */
public class FastJsonUtils {

    public static String parseJson(Object o){
        return JSON.toJSONString(o);
    }

    /**
     * 对单个javabean的解析
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> T JsonToEntity(String jsonString, Class<T> cls) {
        T t = null;
        try {
            t = JSON.parseObject(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T>  List<T> JsonToList(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            list = JSON.parseArray(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> PagingResult<T> JsonToPagingResult(String jsonString, Class<T> cls) {
        PagingResult<T> pagingResult = new PagingResult<T>();
        try {
            pagingResult = JSON.parseObject(jsonString, new
                    TypeReference<PagingResult<T>>() {
                    });
            //解决类型擦除，须要拼装
            List<T> list = JSON.parseArray(JSON.parseObject(jsonString).getString("rows"), cls);
            pagingResult.setRows(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pagingResult;
    }

    public static Map<String, Object> JsonToMap(String jsonString) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Object> JsonToLinkedHashMap(String jsonString) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        try {
            map = JSON.parseObject(jsonString, new TypeReference<LinkedHashMap<String, Object>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<Map<String, Object>> JsonToListMap(String jsonString) {
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        try {
            list = JSON.parseObject(jsonString, new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
