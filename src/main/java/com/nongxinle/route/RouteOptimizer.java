package com.nongxinle.route;

import com.nongxinle.route.model.RouteOptimizeRequest;
import com.nongxinle.route.model.RouteOptimizeResult;

/**
 * 路线优化器，可替换为腾讯排线、高德排线、OR-Tools、jsprit 等实现。
 */
public interface RouteOptimizer {

    RouteOptimizerType optimizerType();

    RouteOptimizeResult optimize(RouteOptimizeRequest request);
}
