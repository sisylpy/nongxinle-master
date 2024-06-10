/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2019-06-21 12:19
 */

package com.nongxinle.utils;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import org.json.JSONObject;

import java.util.HashMap;

/**
 *@author lpy
 *@date 2019-06-21 12:19
 */


public class SpeechUtil {

    /**
     * 设置APPID/AK/SK，注册百度语音识别API即可获取
     */
    public static final String APP_ID = "15619861";
    public static final String API_KEY = "3cWN3Kbe2DrDbwMrNcLm2nol";
    public static final String SECRET_KEY = "pkosrmlqttLoSBGYajQXF5lbxGSHY1ST";



    /**
     * @param pcmBytes
     * @return
     * @Description 调用百度语音识别API
     * @author liuyang
     * @blog http://www.pqsky.me
     * @date 2018年1月30日
     */
    public static JSONObject speechBdApi(byte[] pcmBytes) {
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 调用接口
        JSONObject res = client.asr(pcmBytes, "pcm", 16000, null);
        return res;
    }

    public static  TtsResponse  getYuyin (String hanzi) {

        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

        // 设置可选参数
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put("spd", "2");
        options.put("pit", "5");
        options.put("per", "0");

        // 调用接口
        TtsResponse res = client.synthesis(hanzi, "zh", 1, options);
        return res;
    }




}
