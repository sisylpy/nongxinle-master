package com.nongxinle.utils;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * FastJSON配置类
 * 用于解决FastJSON反序列化时的循环引用和字节码生成问题
 */
@Configuration
public class FastJsonConfiguration {

    static {
        // 全局禁用ASM优化，使用反射方式，避免字节码生成问题
        ParserConfig.getGlobalInstance().setAsmEnable(false);
    }

    @Bean
    public HttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        
        // 配置支持的媒体类型
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        
        // 配置FastJSON特性
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
//            SerializerFeature.WriteMapNullValue,           // 输出空值字段
            SerializerFeature.WriteNullListAsEmpty,       // 将List类型的null转为[]
//            SerializerFeature.WriteNullStringAsEmpty,     // 将String类型的null转为""
//            SerializerFeature.WriteNullNumberAsZero,      // 将Number类型的null转为0
            SerializerFeature.WriteNullBooleanAsFalse,    // 将Boolean类型的null转为false
            SerializerFeature.DisableCircularReferenceDetect, // 禁用循环引用检测
            SerializerFeature.WriteDateUseDateFormat,     // 日期格式化
            SerializerFeature.QuoteFieldNames,            // 输出key时是否使用双引号
            SerializerFeature.WriteEnumUsingToString,     // 枚举输出toString
            SerializerFeature.WriteClassName,             // 输出类名
            SerializerFeature.WriteSlashAsSpecial,        // 对斜杠'/'进行转义
            SerializerFeature.BrowserCompatible,          // 浏览器兼容
            SerializerFeature.WriteNonStringKeyAsString,  // 非字符串key输出为字符串
            SerializerFeature.NotWriteDefaultValue,       // 不写入默认值
            SerializerFeature.BrowserSecure               // 浏览器安全
        );
        
        // 设置日期格式
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        
        converter.setFastJsonConfig(config);
        return converter;
    }
} 