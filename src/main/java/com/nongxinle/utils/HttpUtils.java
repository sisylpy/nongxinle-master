/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 16:20
 */

package com.nongxinle.utils;

import cn.gjing.http.HttpClient;
import cn.gjing.http.HttpMethod;
import com.alibaba.fastjson.JSONException;
//import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpUtils {

    public static String get(String url, Map<String, Object> param) {
        String result = HttpClient.builder(url, HttpMethod.GET, String.class)
                .param(param)
                .execute()
                .get();

        return result;
    }

    public static String get(String url) {
        String result = HttpClient.builder(url, HttpMethod.GET, String.class)
                .execute()
                .get();

        return result;
    }

    public static Map post(String url, Map<String, Object> param) {
        Map result = HttpClient.builder(url, HttpMethod.POST, Map.class)
                .body(param)
                .execute()
                .get();
        return result;
    }



    public static JSONObject doGet(String url) {
        System.out.println("doGet请求url："+url);
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        JSONObject jsonObj = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            //请求体内容
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("doGet结果："+content);
            jsonObj = new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    public static JSONObject doPost(String url,JSONObject jsonParms) throws IOException, JSONException {
        System.out.println("doPost请求url："+url);
        System.out.println("doPost请求Parm："+jsonParms);
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        StringEntity s = new StringEntity(jsonParms.toString(), "utf-8");
        s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json"));
        //设置参数到请求对象中
        httpPost.setEntity(s);
        //设置header信息
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        JSONObject jsonObj = null;
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            String content = EntityUtils.toString(entity, "UTF-8");
            System.out.println("doPost结果："+content);
            jsonObj = new JSONObject(content);
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();

        return jsonObj;
    }





}





