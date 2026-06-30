package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.config.DisRouteDispatchSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class DispatchStrategyRegistry {

    private final Map<DispatchStrategyMode, DispatchStrategy> strategies =
            new EnumMap<DispatchStrategyMode, DispatchStrategy>(DispatchStrategyMode.class);

    @Autowired
    private DisRouteDispatchSettings disRouteDispatchSettings;

    @Autowired
    public DispatchStrategyRegistry(OwnerFixedRouteDispatchStrategy ownerFixedRouteDispatchStrategy,
                                    PlatformDynamicDispatchStrategy platformDynamicDispatchStrategy,
                                    MallDynamicDispatchStrategy mallDynamicDispatchStrategy) {
        register(ownerFixedRouteDispatchStrategy);
        register(platformDynamicDispatchStrategy);
        register(mallDynamicDispatchStrategy);
    }

    private void register(DispatchStrategy strategy) {
        if (strategy != null) {
            strategies.put(strategy.strategyMode(), strategy);
        }
    }

    /**
     * 当前私人老板系统：配置默认 OWNER_FIXED_ROUTE；不根据 disId 自动切换 PLATFORM/MALL。
     */
    public DispatchStrategyMode resolveMode(Integer disId) {
        return disRouteDispatchSettings.getStrategyMode();
    }

    public DispatchStrategy strategyFor(DispatchStrategyMode mode) {
        DispatchStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("未注册的 DispatchStrategy: " + mode);
        }
        return strategy;
    }
}
