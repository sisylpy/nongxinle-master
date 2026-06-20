package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformSubmitLineResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private Integer platformAssignId;
    private String assignStatus;
    private String assignMode;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    /** 价格维度：CONFIRMED / PENDING（购物车预览用，checkout 前无 assignStatus） */
    private String priceConfirmStatus;
    private String lineSubtotal;
    private Integer nxDoDistributerId;
    private Integer nxDoDisGoodsId;
    private Integer nxDoCollaborativeNxDisId;
}
