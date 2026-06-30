package com.nongxinle.community.yunguotuan.util;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * 优果团群身份识别调试日志，统一前缀 [YGT-Identity] 便于 grep。
 */
public final class YgtIdentityLog {
    private YgtIdentityLog() {
    }

    public static void info(String message) {
        System.out.println("[YGT-Identity] " + message);
    }

    public static void info(String step, Map<String, Object> data) {
        System.out.println("[YGT-Identity] " + step + " " + JSON.toJSONString(data));
    }

    public static String maskId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "(empty)";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 8) {
            return trimmed.substring(0, Math.min(4, trimmed.length())) + "***";
        }
        return trimmed.substring(0, 6) + "..." + trimmed.substring(trimmed.length() - 4);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone == null ? "(empty)" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String present(String value) {
        return value == null || value.trim().isEmpty() ? "no" : "yes(len=" + value.trim().length() + ")";
    }
}
