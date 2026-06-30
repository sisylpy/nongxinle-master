package com.nongxinle.route.dispatch.strategy;

/**
 * 派单策略模式。当前老板自用系统默认 {@link #OWNER_FIXED_ROUTE}。
 */
public enum DispatchStrategyMode {
    /** 私人配送商：历史司机/顺序 + 送达时间优先。 */
    OWNER_FIXED_ROUTE,
    /** 未来平台派单（stub，不进入当前老板链路）。 */
    PLATFORM_DYNAMIC,
    /** 未来商城派单（stub，不进入当前老板链路）。 */
    MALL_DYNAMIC;

    public static DispatchStrategyMode fromConfig(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return OWNER_FIXED_ROUTE;
        }
        return valueOf(raw.trim().toUpperCase());
    }
}
