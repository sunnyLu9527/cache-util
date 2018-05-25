package com.htt.app.cache.enums;

/**
 * Created by luming on 2017/7/19.
 */
public enum CacheSource {

    PLEDGE_SERVICE("PledgeService"),
    LOAN_SERVICE("LoanService"),
    USER_SERVICE("UserService"),
    WEB_SERVICE("WebService"),
    MY_SERVICE("MyService"),
    WARRANT_SERVICE("WarrantService"),
    MATERIAL_SERVICE("MaterialService"),
    DEBT_SERVICE("DebtService"),
    CASHLOAN_SERVICE("CashLoanService"),
    PRODUCT_SERVICE("ProductService");

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
