package com.nongxinle.utils;

import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * FastJSON初始化类
 * 在应用启动时立即禁用ASM优化，避免字节码生成问题
 */
@Component
public class FastJsonInitializer {

    @PostConstruct
    public void init() {
        // 禁用ASM优化，使用反射方式
        ParserConfig.getGlobalInstance().setAsmEnable(false);
        System.out.println("FastJSON ASM优化已禁用");
    }
} 