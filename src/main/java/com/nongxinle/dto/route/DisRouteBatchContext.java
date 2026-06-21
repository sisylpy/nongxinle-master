package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/** Phase 2b-1：批次时间快照（写入 plan） */
@Setter
@Getter
@ToString
public class DisRouteBatchContext {
    private String batchCode;
    private Date batchStartAt;
    private Date batchEndAt;
    private Date defaultDepartAt;
}
