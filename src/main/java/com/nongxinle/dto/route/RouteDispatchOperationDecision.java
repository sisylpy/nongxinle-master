package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-2：单一调度操作判定结果 */
@Setter
@Getter
@ToString
public class RouteDispatchOperationDecision {
    private boolean allowed;
    private String blockedReason;
    private String operationHint;

    public static RouteDispatchOperationDecision allow() {
        RouteDispatchOperationDecision decision = new RouteDispatchOperationDecision();
        decision.setAllowed(true);
        return decision;
    }

    public static RouteDispatchOperationDecision deny(String blockedReason) {
        RouteDispatchOperationDecision decision = new RouteDispatchOperationDecision();
        decision.setAllowed(false);
        decision.setBlockedReason(blockedReason);
        return decision;
    }

    public void requireAllowed() {
        if (!allowed) {
            throw new IllegalStateException(blockedReason != null ? blockedReason : "当前操作不允许");
        }
    }
}
