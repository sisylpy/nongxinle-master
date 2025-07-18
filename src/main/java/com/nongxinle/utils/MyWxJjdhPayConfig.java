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


public class MyWxJjdhPayConfig implements WXPayConfig {
    @Override
    public String getAppID() {
        return "wx1ea78d3f33234284";
    }

    @Override
    public String getMchID() {
        return "1694659996";
    }

    @Override
    public String getKey() {
        return "5d020040fed265d26050f6da30578295";
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
