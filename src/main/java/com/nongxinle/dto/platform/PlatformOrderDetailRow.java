package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;

/** MyBatis 映射：平台订单明细行 */
@Setter
@Getter
public class PlatformOrderDetailRow {
    private Integer orderId;
    private Integer platformAssignId;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
    private String assignStatus;
    private String assignMode;
    private String assignSource;
    private Integer assignedDistributerId;
    private Integer assignedDisGoodsId;
    private String assignedDistributerName;
    private String orderPrice;
    private String orderSubtotal;
    private Integer defaultId;
    private Integer defaultDistributerId;
    private Integer defaultDisGoodsId;
    private String defaultSource;
}
