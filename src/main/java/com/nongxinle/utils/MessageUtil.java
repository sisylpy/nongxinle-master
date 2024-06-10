package com.nongxinle.utils;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    /**
     * 解析微信发来的请求（XML）.
     *
     * @param msg 消息
     * @return map
     */
    public static Map<String, String> parseXml(final String msg) throws Exception {
        System.out.println("jinelllekekeeilelelelle" + msg);
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
            System.out.println("jinldkfdajfdkafkdasfi3ii3i38388383883883838838388383883883");
//1.将字符串转为Document
        Document document = DocumentHelper.parseText(msg);
        System.out.println("0000------------");
        //2.获取根元素的所有子节点
        // 得到xml根元素
        Element root = document.getRootElement();
        System.out.println("1111------------");
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();
        System.out.println("22222------------");
        //3.遍历所有子节点
        for (Element e : elementList){
            System.out.println("33333------------");
            map.put(e.getName(), e.getText());
        }
//        try (InputStream inputStream = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8.name()))) {
//            // 读取输入流
//            SAXReader reader = new SAXReader();
//            Document document = reader.read(inputStream);
//            System.out.println("reererkekrakekkrekekrekekeekkekekekekekkeke");
//            // 得到xml根元素
//            Element root = document.getRootElement();
//            // 得到根元素的所有子节点
//            List<Element> elementList = root.elements();
//
//            // 遍历所有子节点
//            for (Element e : elementList) {
//                map.put(e.getName(), e.getText());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(map);
        return map;
    }
}
