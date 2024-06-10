/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 10:42
 */

package com.nongxinle.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import java.io.*;
import java.util.Properties;

/**
 * @author lpy
 * @date 2020-03-11 10:42
 */


public class PropertiesUtil {


    public  void saveWxProperty(String key, String value) throws Exception {

        String dir = System.getProperty("user.dir");  //获得tomcat所在的工作路径
        PropertiesConfiguration configuration = new PropertiesConfiguration(dir + "/wx.properties");
        System.out.println("dir====" + dir);
        configuration.setProperty(key, value);
        configuration.save();
        System.out.println("condarrrr" + configuration.getKeys());
        System.out.println("saveWxProperty---------------" + key + "===" + configuration.getString(key));

    }




}
