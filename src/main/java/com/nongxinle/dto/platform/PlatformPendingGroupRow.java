package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;

/** MyBatis 映射：pending 按客户聚合行 */
@Setter
@Getter
public class PlatformPendingGroupRow {
    private Integer departmentId;
    private String departmentName;
    private String departmentOrderCode;
    private Integer pendingLineCount;
    private String orderIdsCsv;
    private String firstPendingAt;
    private String lastPendingAt;
}
