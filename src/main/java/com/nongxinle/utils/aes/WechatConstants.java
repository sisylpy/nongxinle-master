package com.nongxinle.utils.aes;

public class WechatConstants {

    public static final String URL = "xxx";

    //  应用id
    public static final String AGENTID = "xxx";
    //    企业ID
    public static final String CORP_ID ="xxx";
    //  应用的凭证密钥
    public static final String CORP_SECRET = "xxx";
    //    通讯录凭证密钥
    public static final String CONTACTS_SECRET = "xxx";

    //    token获取地址
    public static final String GETTOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={corpid}&corpsecret={corpsecret}";

    //    获取部门成员
    public static final String USER_SIMPLELIST_URL  ="https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token={access_token}&department_id={department_id}&fetch_child={fetch_child}";

    //    获取部门列表
    public static final String DEPARTMENT_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token={access_token}&id={id}";

    //   获取用户id
    public static final String  GETUSERINFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token={access_token}&code={code}";

    //    获取用户详情
    public static final String USER_GET_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token={access_token}&userid={userid}";

    //获取JSAPI_TICKET
    public static final String GET_JSAPI_TICKET = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token={access_token}";

    //    推送消息
    public static final String SEND_MESSAGE_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token={access_token}";


}
