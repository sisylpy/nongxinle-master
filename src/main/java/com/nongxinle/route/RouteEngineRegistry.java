package com.nongxinle.route;

import com.nongxinle.route.cost.TencentMatrixRouteCostProvider;
import com.nongxinle.route.optimizer.BalancedInsertion2OptRouteOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RouteEngineRegistry {

    private final Map<RouteCostProviderType, RouteCostProvider> costProviders = new HashMap<RouteCostProviderType, RouteCostProvider>();
    private final Map<RouteOptimizerType, RouteOptimizer> optimizers = new HashMap<RouteOptimizerType, RouteOptimizer>();

    @Autowired
    public RouteEngineRegistry(TencentMatrixRouteCostProvider tencentMatrixRouteCostProvider,
                               BalancedInsertion2OptRouteOptimizer balancedInsertion2OptRouteOptimizer) {
        registerCostProvider(tencentMatrixRouteCostProvider);
        registerOptimizer(balancedInsertion2OptRouteOptimizer);
    }

    public void registerCostProvider(RouteCostProvider provider) {
        costProviders.put(provider.providerType(), provider);
    }

    public void registerOptimizer(RouteOptimizer optimizer) {
        optimizers.put(optimizer.optimizerType(), optimizer);
    }

    public RouteCostProvider costProvider(RouteCostProviderType type) {
        RouteCostProvider provider = costProviders.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("未注册的 RouteCostProvider: " + type);
        }
        return provider;
    }

    public RouteOptimizer optimizer(RouteOptimizerType type) {
        RouteOptimizer optimizer = optimizers.get(type);
        if (optimizer == null) {
            throw new IllegalArgumentException("未注册的 RouteOptimizer: " + type);
        }
        return optimizer;
    }

    public List<RouteCostProviderType> listCostProviderTypes() {
        return new java.util.ArrayList<RouteCostProviderType>(costProviders.keySet());
    }

    public List<RouteOptimizerType> listOptimizerTypes() {
        return new java.util.ArrayList<RouteOptimizerType>(optimizers.keySet());
    }
}
