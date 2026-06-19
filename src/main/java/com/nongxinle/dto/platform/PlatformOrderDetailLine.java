package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformOrderDetailLine implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private Integer platformAssignId;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
    private String assignStatus;
    private String assignMode;
    private Integer assignedDistributerId;
    private Integer assignedDisGoodsId;
    private String orderPrice;
    private String orderSubtotal;
    private PlatformDefaultRecommendInfo defaultRecommend;
}
