package com.nongxinle.entity;


import java.util.Map;

/**
 * 订阅消息
 * @author  lisijia
 * @email   1151141449@qq.com
 * @date    2020/7/7 10:57
 */
public class SubscribeMessage {
    //接口调用凭证
    private String access_token;
    //接收者（用户）的 openid
    private String touser;
    //所需下发的订阅模板id
    private String template_id;
    //点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
    private String page;
    //模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }
    private Map<String, TemplateData> data;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Map<String, TemplateData> getData() {
        return data;
    }

    public void setData(Map<String, TemplateData> data) {
        this.data = data;
    }
}