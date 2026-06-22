package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

/**
 * GET 读模型：无效/残留派车站点（不写库）。
 */
@Getter
@Setter
public class InvalidDispatchStopDto {
    private Integer stopId;
    private Integer taskId;
    /** Phase 3a.1：对外主键别名 */
    private Integer deliveryStopId;
    private Integer depFatherId;
    private String depName;
    private String invalidReason;
    private Boolean needsManualCleanup;
}
