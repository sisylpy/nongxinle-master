package com.nongxinle.config;

import com.nongxinle.platform.admin.PlatformMarketAdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class PlatformMarketAdminWebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private PlatformMarketAdminAuthInterceptor platformMarketAdminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(platformMarketAdminAuthInterceptor)
                .addPathPatterns("/api/platform/admin/**")
                .excludePathPatterns(
                        "/api/platform/admin/auth/login",
                        "/api/platform/admin/auth/bootstrap"
                );
    }
}
