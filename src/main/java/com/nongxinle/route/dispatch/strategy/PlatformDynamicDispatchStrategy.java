package com.nongxinle.route.dispatch.strategy;

import org.springframework.stereotype.Component;

/**
 * 未来平台派单策略 stub。不进入当前私人老板正式链路。
 */
@Component
public class PlatformDynamicDispatchStrategy implements DispatchStrategy {

    @Override
    public DispatchStrategyMode strategyMode() {
        return DispatchStrategyMode.PLATFORM_DYNAMIC;
    }

    @Override
    public DispatchStrategyOutcome plan(DispatchStrategyContext context) {
        throw new UnsupportedOperationException(
                "PLATFORM_DYNAMIC is reserved for future platform dispatch and is not enabled "
                        + "for owner dispatch (disId="
                        + (context != null ? context.getDisId() : null) + ")");
    }
}
