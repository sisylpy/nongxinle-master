package com.nongxinle.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地开发防误连：启动时校验 jdbc.url 必须指向本机 MySQL。
 * 默认关闭；仅在 application.properties 中显式开启时生效：
 * local.database.only.guard.enabled=true
 */
@Component
public class LocalDatabaseOnlyGuard implements InitializingBean {

    private static final Pattern JDBC_HOST_PATTERN =
            Pattern.compile("jdbc:mysql://([^/:?]+)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> ALLOWED_HOSTS = new HashSet<>(Arrays.asList(
            "localhost",
            "127.0.0.1",
            "::1"
    ));

    @Value("${local.database.only.guard.enabled:true}")
    private boolean guardEnabled;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Override
    public void afterPropertiesSet() {
        if (!guardEnabled) {
            return;
        }
        String host = extractHost(jdbcUrl);
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "禁止连接远程数据库。请将 jdbc.url 配置为 localhost/127.0.0.1，当前 host=" + host
            );
        }
    }

    private String extractHost(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = JDBC_HOST_PATTERN.matcher(url.trim());
        if (!matcher.find()) {
            return null;
        }
        String host = matcher.group(1);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        return host;
    }
}
