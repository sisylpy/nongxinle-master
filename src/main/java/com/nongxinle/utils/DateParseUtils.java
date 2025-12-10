package com.nongxinle.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日期解析工具类，用于安全地解析日期字符串
 */
public class DateParseUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    /**
     * 安全地解析日期字符串，返回时间戳
     * @param dateString 日期字符串
     * @return 时间戳，如果解析失败返回0
     */
    public static long parseDateToTimestamp(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return 0;
        }
        
        try {
            Date date = DATE_FORMAT.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 安全地解析日期字符串，返回Date对象
     * @param dateString 日期字符串
     * @return Date对象，如果解析失败返回null
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查日期字符串是否有效
     * @param dateString 日期字符串
     * @return 如果有效返回true，否则返回false
     */
    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        
        try {
            Date date = DATE_FORMAT.parse(dateString);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }
}
