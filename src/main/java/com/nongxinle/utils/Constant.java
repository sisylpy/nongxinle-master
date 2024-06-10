package com.nongxinle.utils;

/**
 * 常量
 * 
 */
public class Constant {

	/**
	 * 菜单类型
	 * 
	 */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        private MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * 定时任务状态
     * 
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        private ScheduleStatus(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }


    // 服务商相关
    /**
     * 服务商CorpID
     */
    public static final String CorpID = "ww9778dea409045fe6";
    // 应用相关
    /**
     * 应用的唯一身份标识
     */
    public static final String SuiteID = "ww2cddb5d2d7b3ee5d";
    public static final String SuiteIDRX = "wwe01d611c376073f4";
    public static final String SuiteIDSX = "ww2eeda7e648715801";
    /**
     * 服务商身份的调用凭证
     */
    public static final String ProviderSecret = "vHinKVFde56Wnf4noMxaRboIyxXd6QXEq4eAaqDGAIRNoH0GJ6eIa7xz3QsK_gY4";

    /**
     * 应用的调用身份密钥
     */
    public static final String SuiteSecret = "lPCoj03327th5Froa3vMA2KmsO-T4zJ6WpMF0EzUe-c";
    public static final String SuiteSecretRx = "fYaHS_9zDKidYlu9v4WlsQBmgabwed3V5GPAGwQ832k";
    public static final String SuiteSecretSx = "kEEiUQvIl8zyeH0oQZeK_f97hPo_InVCqsOSMRWboK0";

    // 回调相关
    /**
     * 回调/通用开发参数Token, 两者解密算法一样，所以为方便设为一样
     */
    public static final String TOKEN = "6Hk";
    public static final String TOKENRX = "tMPGGtq";
    public static final String TOKENSX = "jPk2ATwy5yUfSRHIqr";
    public static final String LSCGTOKEN = "pei";

    /**
     * 回调/通用开发参数EncodingAESKey, 两者解密算法一样，所以为方便设为一样
     */
    public static final String EncodingAESKey = "j2HDS3Gmoep2GJGVn2fJtLxWXGNQMXO5AUf3CheyU6T";
    public static final String EncodingAESKeyRx = "xBDXXGyqKjOSWjG6D671NT4DbuehgeyyEkAYby2mxm6";
    public static final String EncodingAESKeySx = "BaTvU0Mrqm1adPgzitl2AGNroOIH4vXKGDMyqmJMg0o";
    public static final String LSCGEncodingAESKey = "Oqr4iCoPG8HT3hKCf2e3rrOa9POQeNIWr9G17flhKti";

    // 获取第三方应用凭证
    public final static String THIRD_BUS_WECHAT_SUITE_TOKEN = "https://qyapi.weixin.qq.com/cgi-bin/service/get_suite_token";


    public static final  String SUITE_TOKEN = "SUITE_TOKEN";
    public static final  String SUITE_TOKEN_RX = "SUITE_TOKEN_RX";
    public static final  String SUITE_TOKEN_SX = "SUITE_TOKEN_SX";


//    daijian
    public static final String TOKENDj = "PQz";
    public static final String SUITEIDDj = "dk79f21376ebff6142";
    public static final String EncodingAESKeyDj = "P6HTN9ORtVn167v1O3pykoOYOhf1s5Cb4OHCy3trn3s";
    public static final String CORPIDDjTRS = "ww8bb9f761c253a58d";
    public static final String MOBANID = "dk79f21376ebff6142";






}
