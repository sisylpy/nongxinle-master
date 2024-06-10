/**
 * com.nongxinle.entity class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 12:10
 */

package com.nongxinle.entity;

/**
 *@author lpy
 *@date 2020-03-11 12:10
 */

import lombok.Data;
import java.util.Map;

/*
 * 小程序推送所需数据
 * */
@Data
public class WxMssVo {
    private String touser;//用户openid
    private String template_id;//模版id
    private String page;//默认跳到小程序首页
    private String miniprogram_state;
    private String lang;
//    private Map<String, Map<String, String>>  data ;//推送文字
    private Map<String, String>  data ;//推送文字



}


