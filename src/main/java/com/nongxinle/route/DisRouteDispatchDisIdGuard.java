package com.nongxinle.route;

import com.nongxinle.service.NxDistributerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 独立配送商派车：disId 必须为 nxDistributerId，不接受 marketId。
 */
@Component
public class DisRouteDispatchDisIdGuard {

    private static final Logger logger = LoggerFactory.getLogger(DisRouteDispatchDisIdGuard.class);

    @Autowired
    private NxDistributerService nxDistributerService;

    public boolean distributerExists(Integer disId) {
        return disId != null && nxDistributerService.queryObject(disId) != null;
    }

    /** 未命中配送商时返回提示文案；命中则 null。 */
    public String missingDistributerHint(Integer disId) {
        if (disId == null || distributerExists(disId)) {
            return null;
        }
        return "disId=" + disId + " 未命中配送商，请检查是否误传市场 ID；应传 nxDistributerId";
    }

    public void logMissingDistributerIfNeeded(Integer disId, String api) {
        String hint = missingDistributerHint(disId);
        if (hint != null) {
            logger.warn("[{}] {}", api != null ? api : "route-dispatch", hint);
        }
    }
}
