package com.nongxinle.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;

public class PropertiesUtil {

    private static PropertiesConfiguration config;

    static {
        try {
            // 假设你的配置文件放在 classpath 下，文件名为 config.properties
            config = new PropertiesConfiguration("config.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取外部图片存放路径
     * 如果配置文件没有配置，则返回默认路径
     */
    public static String getExternalImagesPath() {
        return config.getString("external.images.path", "file:///opt/tomcat/latest/app-data/images/");
    }
}
