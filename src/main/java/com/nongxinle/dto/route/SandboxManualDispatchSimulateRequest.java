package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxManualDispatchSimulateRequest extends SandboxManualDispatchBaseRequest {
    private Integer driverUserId;
    /** 人工锁定第 N 个送（≥1）；有则只返回单方案。 */
    private Integer manualStopSeq;
    /** 必须送达截止时间，如 2026-06-22 15:00:00 */
    private String requiredLatestArrivalAt;
}
