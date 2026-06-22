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
}
