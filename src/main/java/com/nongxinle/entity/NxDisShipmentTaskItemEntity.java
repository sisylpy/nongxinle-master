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

    /** 读模型：来自 live/history 订单状态，不落库 */
    private Integer orderStatus;
    /** 读模型：来自 live/history 送达日期，不落库 */
    private String orderArriveDate;
    /** 读模型：true 表示展示字段来自 history 表 */
    private Boolean orderFromHistory;

    /** 读模型：是否成功解析到 live/history 订单行 */
    private Boolean orderResolved;
    /** 读模型：解析到的订单批发商 ID */
    private Integer resolvedOrderDisId;
    /** 读模型：解析到的订单客户 depFatherId */
    private Integer resolvedOrderDepFatherId;
    /** 读模型：item 是否通过完整性校验，可展示 */
    private Boolean itemValid;
    /** 读模型：校验失败原因 */
    private String itemInvalidReason;
}
