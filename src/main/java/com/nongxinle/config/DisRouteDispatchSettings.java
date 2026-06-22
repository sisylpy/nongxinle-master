package com.nongxinle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 配送派车模块运行时配置。
 */
@Component
public class DisRouteDispatchSettings {

    /**
     * true：司机路线全部 task 装车确认（READY_TO_GO）后才可出发；
     * false：出库确认（ASSIGNED）即可出发。
     */
    @Value("${dis.route.dispatch.require-load-before-depart:false}")
    private boolean requireLoadBeforeDepart;

    public boolean isRequireLoadBeforeDepart() {
        return requireLoadBeforeDepart;
    }

    public boolean isRequireLoadBeforeDepart(Integer disId) {
        return requireLoadBeforeDepart;
    }
}
