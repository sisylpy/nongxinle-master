package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GbPlatformOrderBridgeResult {

    private Integer gbDepartmentOrderId;
    private Integer nxDepartmentOrderId;
    private Integer platformAssignId;
    private Integer fulfillmentId;
    /** true 表示 assign 已存在，本次未重复插入 */
    private boolean idempotent;
}
