package com.nongxinle.config;

import com.nongxinle.route.dispatch.strategy.DispatchStrategyMode;
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

    /** 历史配送偏好回看天数（P1 只读服务）。 */
    @Value("${dis.route.dispatch.history.lookback-days:180}")
    private int historyLookbackDays;

    /** manual_locked=1 的历史记录权重倍数。 */
    @Value("${dis.route.dispatch.history.manual-locked-weight:2.0}")
    private double historyManualLockedWeight;

    /** 历史偏好最低送达次数。 */
    @Value("${dis.route.dispatch.history.min-delivered-times:1}")
    private int historyMinDeliveredTimes;

    /** Phase 2 派单策略模式；老板系统默认 OWNER_FIXED_ROUTE。 */
    @Value("${dis.route.dispatch.strategy-mode:OWNER_FIXED_ROUTE}")
    private String strategyMode;

    public boolean isRequireLoadBeforeDepart() {
        return requireLoadBeforeDepart;
    }

    public boolean isRequireLoadBeforeDepart(Integer disId) {
        return requireLoadBeforeDepart;
    }

    public int getHistoryLookbackDays() {
        return historyLookbackDays;
    }

    public double getHistoryManualLockedWeight() {
        return historyManualLockedWeight;
    }

    public int getHistoryMinDeliveredTimes() {
        return historyMinDeliveredTimes;
    }

    public DispatchStrategyMode getStrategyMode() {
        return DispatchStrategyMode.fromConfig(strategyMode);
    }
}
