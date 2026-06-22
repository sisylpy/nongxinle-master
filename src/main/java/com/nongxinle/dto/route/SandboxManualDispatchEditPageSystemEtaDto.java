package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 系统模拟 ETA（与客户原时间窗、老板人工约束区分）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageSystemEtaDto {
    private String plannedArrivalAt;
    private String plannedArrivalLabel;
}
