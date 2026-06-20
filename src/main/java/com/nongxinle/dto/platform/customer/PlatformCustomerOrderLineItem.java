package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;

/** 饭店端：今日平台订货行（MyBatis 或 Service 填充） */
@Getter
@Setter
public class PlatformCustomerOrderLineItem {
    private Integer orderId;
    private Integer platformAssignId;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
    private String assignStatus;
    /** UNMATCHED | SEARCHING | ASSIGNED */
    private String statusCode;
    private String statusText;
    private Boolean editable;
    private Integer assignedDistributerId;
    private String assignedDistributerName;
    private Integer defaultDistributerId;
    /** GB 侧订单 id，批发商订单删除/改量用 */
    private Integer gbDepartmentOrderId;
    /** CUSTOMER_SELECTED_SUPPLIER | PLATFORM_MANUAL 等 */
    private String assignSource;
    private String goodsBrand;
    private String orderPrice;
    private String orderSubtotal;
    /** 出货数量，无则与订货量相同 */
    private String weight;
    private String defaultDistributerName;
    /** 前端展示用（服务端填充） */
    private String titleLine;
    private String priceLine;
    private String supplierLine;
}
