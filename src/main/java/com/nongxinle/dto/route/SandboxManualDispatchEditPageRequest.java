package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 人工路线编辑页 — 与 simulate 相同上下文；可选锁定插入顺序与必须送达时间。
 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageRequest extends SandboxManualDispatchBaseRequest {
    private Integer driverUserId;
    private Integer manualStopSeq;
    private String requiredLatestArrivalAt;
}
