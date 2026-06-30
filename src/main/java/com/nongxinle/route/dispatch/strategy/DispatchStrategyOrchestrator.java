package com.nongxinle.route.dispatch.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatchStrategyOrchestrator {

    @Autowired
    private DispatchStrategyRegistry dispatchStrategyRegistry;

    public DispatchStrategyOutcome plan(DispatchStrategyContext context) {
        if (context == null || context.getDisId() == null) {
            throw new IllegalArgumentException("dispatch strategy context disId 不能为空");
        }
        DispatchStrategyMode mode = dispatchStrategyRegistry.resolveMode(context.getDisId());
        DispatchStrategy strategy = dispatchStrategyRegistry.strategyFor(mode);
        return strategy.plan(context);
    }
}
