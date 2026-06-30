package com.nongxinle.route.dispatch.strategy;

import org.springframework.stereotype.Component;

/**
 * 未来商城派单策略 stub。不进入当前私人老板正式链路。
 */
@Component
public class MallDynamicDispatchStrategy implements DispatchStrategy {

    @Override
    public DispatchStrategyMode strategyMode() {
        return DispatchStrategyMode.MALL_DYNAMIC;
    }

    @Override
    public DispatchStrategyOutcome plan(DispatchStrategyContext context) {
        throw new UnsupportedOperationException(
                "MALL_DYNAMIC is reserved for future mall dispatch and is not enabled "
                        + "for owner dispatch (disId="
                        + (context != null ? context.getDisId() : null) + ")");
    }
}
