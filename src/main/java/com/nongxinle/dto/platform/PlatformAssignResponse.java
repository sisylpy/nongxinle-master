package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformAssignResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private Integer platformAssignId;
    private String assignStatus;
    private Integer nxDoDistributerId;
    private Integer nxDoDisGoodsId;
    private Integer nxDoCollaborativeNxDisId;
    private String nxDoPrice;
    private String nxDoExpectPrice;
    private String nxDoPriceDifferent;
    private String nxDoSubtotal;
    private Integer defaultId;
    private Integer switchLogId;
    /** 可选：assign 成功后履约状态 */
    private String fulfillmentStatus;
    private Integer costMissing;
}
