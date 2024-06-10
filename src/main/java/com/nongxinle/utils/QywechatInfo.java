package com.nongxinle.utils;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class QywechatInfo {

    /**
     * 签名
     */
    private String msgSignature;

    /**
     * 随机时间戳
     */
    private String timestamp;

    /**
     * 随机值
     */
    private String nonce;

    /**
     * 加密的xml字符串
     */
    private String sPostData;

    /**
     * 企业微信回调配置
     */
    private QywechatEnum qywechatEnum;

}