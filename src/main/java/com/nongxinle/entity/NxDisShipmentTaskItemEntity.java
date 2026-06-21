package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxDisShipmentTaskItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDstiId;
    private Integer nxDstiTaskId;
    private Integer nxDstiLiveOrderId;
    private Integer nxDstiHistoryOrderId;
    private Integer nxDstiBillId;
    private String nxDstiGoodsName;
    private String nxDstiQuantity;
    private String nxDstiStandard;
    private String nxDstiRemark;
    private String nxDstiItemStatus;
    private Date nxDstiCreatedAt;
    private Date nxDstiUpdatedAt;

    /** MyBatis 动态更新：置 true 时清空 bill_id */
    private Boolean clearBillId;
    /** MyBatis 动态更新：置 true 时清空 history_order_id */
    private Boolean clearHistoryOrderId;
}
