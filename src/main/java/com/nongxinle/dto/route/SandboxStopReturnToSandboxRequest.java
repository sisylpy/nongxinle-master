package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 3c：已确认店返回动态沙盘 */
@Setter
@Getter
@ToString
public class SandboxStopReturnToSandboxRequest {
    private Integer operatorUserId;
    private String reason;
    /** 可选：与 GET /sandbox/today 一致，便于返回同一视图 */
    private Integer disId;
    private String routeDate;
    private String batchCode;
    /** 批量路线编辑：跳过每次撤销后的 Today 重建 */
    private Boolean suppressTodayResponse;
    /** 批量路线编辑：延迟到批次结束后再刷新计划排程 */
    private Boolean deferPlanRefresh;
}
