package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCheckoutLineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxOrderId;
    private Integer gbOrderId;
    private Integer nxGoodsId;
    private Integer nxDistributerId;
    private Integer nxDistributerGoodsId;
    private String goodsName;
    private String goodsImage;
    private String goodsImageLarge;
    private String quantity;
    private String standard;
    /** 价格维度：CONFIRMED / PENDING */
    private String priceConfirmStatus;
    private String unitPrice;
    private String pricingStandard;
    private String lineSubtotal;
    private String remark;
    private Boolean hasSupplierSelected;
    private Boolean editable;
    private PlatformCartSupplierItem supplier;
    /** 配送商维度：checkout 预览阶段为 null；confirm 后为 ASSIGNED / PENDING */
    private String assignStatus;
}
