package com.htt.app.cache.enums;

/**
 * Created by luming on 2017/7/19.
 */
public enum CacheSource {

    NONE(""),
    ZEUS("Zeus");

    /**
     * 创建一个新的实例 StatisticsOperate.
     *
     * @param des
     */
    private CacheSource(String des) {
        this.des = des;
    }

    private String des;


    public String getDes() {
        return des;
    }

}
