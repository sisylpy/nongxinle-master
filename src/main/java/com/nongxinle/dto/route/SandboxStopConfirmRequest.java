package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SandboxStopConfirmRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    /** 客户 fatherId，与 sandboxStopKey 二选一 */
    private Integer depFatherId;
    private String sandboxStopKey;
    private Integer driverUserId;
    private Integer operatorUserId;
    private Integer manualStopSeq;
    private String assignReason;
    /** 可选：校验当前 eligible liveOrderIds */
    private List<Integer> liveOrderIds;
    /** 若已有 taskId（旧数据），可传入走 assign 快路径 */
    private Integer taskId;
    /** 批量路线编辑：跳过每次确认后的 Today 重建 */
    private Boolean suppressTodayResponse;
    /** 批量路线编辑：延迟到批次结束后再刷新计划排程 */
    private Boolean deferPlanRefresh;
    /** todaydispatch 批次确认：统一 planId，避免 task 上过期 plan 漂移 */
    private Integer planId;
    /** 同一次 confirm 内复用，避免每个 stop 重复扫 nx_department_orders */
    private List<DisRouteOrderSnapshotDto> eligibleOrderSnapshotCache;
}
