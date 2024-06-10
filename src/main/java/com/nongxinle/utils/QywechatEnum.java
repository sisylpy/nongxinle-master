package com.nongxinle.utils;


import lombok.Getter;
import lombok.Setter;

public enum QywechatEnum {

    JXPP("测试", Constant.SuiteID, Constant.TOKEN, Constant.EncodingAESKey),
    JXPPRX("测试", Constant.CorpID, Constant.TOKENRX, Constant.EncodingAESKeyRx),
    JXPPSX("测试", Constant.CorpID, Constant.TOKENSX, Constant.EncodingAESKeySx),
    DJ("测试", Constant.CorpID, Constant.TOKENDj, Constant.EncodingAESKeyDj),
    DJPOST("测试", Constant.SUITEIDDj, Constant.TOKENDj, Constant.EncodingAESKeyDj),
    DJAPP("测试", Constant.CORPIDDjTRS, Constant.TOKENDj, Constant.EncodingAESKeyDj),
    DJAPPPOST("测试", Constant.CORPIDDjTRS, Constant.TOKENDj, Constant.EncodingAESKeyDj);


    /**
     * 应用名
     */
    private String name;

    /**
     * 企业ID
     */
    private String corpid;

    /**
     * 回调url配置的token
     */
    private String token;

    /**
     * 随机加密串
     */
    private String encodingAESKey;


    QywechatEnum(final String name, final String corpid, final String token, final String encodingAESKey) {
        this.name = name;
        this.corpid = corpid;
        this.encodingAESKey = encodingAESKey;
        this.token = token;
    }

    public String getCorpid() {
        return corpid;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getEncodingAESKey() {
        return encodingAESKey;
    }

    public void  setCorpid(String corpid){
        this.corpid =  corpid;
    }

}