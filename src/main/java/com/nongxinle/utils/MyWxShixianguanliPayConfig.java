/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-05-22 09:11
 */

package com.nongxinle.utils;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.InputStream;

/**
 *@author lpy
 *@date 2020-05-22 09:11
 */


public class MyWxShixianguanliPayConfig implements WXPayConfig {
    @Override
    public String getAppID() {
        return "wxd18ed10d341c957b";
    }

    @Override
    public String getMchID() {
        return "1594384761";
    }

    @Override
    public String getKey() {
        return "sisy112578sisy112578sisy112578cf";
    }





    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 0;
    }
}
