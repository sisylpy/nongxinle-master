package com.nongxinle.utils.aes;

public class QywechatUserBean {

    /**
     * 签名.
     */
    private String msgSignature;

    /**
     * 随机时间戳.
     */
    private String timestamp;

    /**
     * 随机值.
     */
    private String nonce;

    /**
     * 加密的xml字符串.
     */
    private String sPostData;

    /**
     * 企业微信回调配置.
     */
    private QywechatCallback qywechatCallback;

    public String getMsgSignature() {
        return msgSignature;
    }

    public void setMsgSignature(String msgSignature) {
        this.msgSignature = msgSignature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getsPostData() {
        return sPostData;
    }

    public void setsPostData(String sPostData) {
        this.sPostData = sPostData;
    }

    public QywechatCallback getQywechatCallback() {
        return qywechatCallback;
    }

    public void setQywechatCallback(QywechatCallback qywechatCallback) {
        this.qywechatCallback = qywechatCallback;
    }
}
