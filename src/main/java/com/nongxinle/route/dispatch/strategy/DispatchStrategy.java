package com.nongxinle.route.dispatch.strategy;

public interface DispatchStrategy {

    DispatchStrategyMode strategyMode();

    DispatchStrategyOutcome plan(DispatchStrategyContext context);
}
