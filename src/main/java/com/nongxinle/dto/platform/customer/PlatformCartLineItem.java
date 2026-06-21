package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCartLineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxOrderId;
    private Integer gbOrderId;
    private Integer nxGoodsId;
    private Integer nxDistributerId;
    private Integer nxDistributerGoodsId;
    private String goodsName;
    /** 商品缩略图（相对路径，与现有 uploadImage/ goodsImage 一致） */
    private String goodsImage;
    private String goodsImageLarge;
    private String quantity;
    private String standard;
    /** 价格是否已知：CONFIRMED / PENDING（与 assignStatus 无关） */
    private String priceConfirmStatus;
    /** 已知价时：单价（如 1.30） */
    private String unitPrice;
    /** 已知价时：单价对应单位（如 斤），用于展示「¥1.30/斤」 */
    private String pricingStandard;
    private String lineSubtotal;
    private String remark;
    /** 是否已选批发商加购（nxDistributerGoodsId 有效） */
    private Boolean hasSupplierSelected;
    /** 购物车临时行恒为 true；支付锁定或已 checkout 时为 false */
    private Boolean editable;
    /** 本行处于 checkout 支付 PENDING 锁定 */
    private Boolean paymentLocked;
    /** 与 paymentLocked 同义，便于前端展示「支付处理中」 */
    private Boolean paymentProcessing;
    private String paymentProcessingMessage;
    /** 已选批发商时非 null；未选配送商加购时为 null */
    private PlatformCartSupplierItem supplier;
}
