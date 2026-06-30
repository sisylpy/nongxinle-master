package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxComputeRequest;

/** 统一构造 {@link SandboxComputeRequest}，避免各读路径重复 set 字段。 */
public final class SandboxComputeRequestFactory {

    private SandboxComputeRequestFactory() {
    }

    public static SandboxComputeRequest base(Integer disId, String routeDate, String batchCode) {
        SandboxComputeRequest request = new SandboxComputeRequest();
        request.setDisId(disId);
        request.setRouteDate(routeDate);
        request.setBatchCode(normalizeBatch(batchCode));
        return request;
    }

    /** 分派中页：正式 pageViewModel 契约，跳过偏好/缩小订单查询。 */
    public static SandboxComputeRequest dispatchTodayPage(Integer disId, String routeDate, String batchCode) {
        SandboxComputeRequest request = base(disId, routeDate, batchCode);
        request.setFormalPageContractMode(true);
        return request;
    }

    /** 装车/配送页：只读已落库路线，跳过沙盘优化与矩阵重算。 */
    public static SandboxComputeRequest persistedTodayPage(Integer disId, String routeDate, String batchCode) {
        SandboxComputeRequest request = dispatchTodayPage(disId, routeDate, batchCode);
        request.setPersistedRoutesOnlyMode(true);
        return request;
    }

    private static String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }
}
