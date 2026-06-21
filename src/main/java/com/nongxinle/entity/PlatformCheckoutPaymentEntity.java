package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PlatformCheckoutPaymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pcpId;
    private String pcpCheckoutToken;
    private Integer pcpMarketId;
    private Integer pcpGbDepartmentId;
    private Integer pcpGbDepartmentFatherId;
    private Integer pcpGbDistributerId;
    private Integer pcpGbOrderUserId;
    private String pcpDeliveryDate;
    private String pcpRemark;
    private String pcpOrderIdsJson;
    private BigDecimal pcpKnownTotal;
    private Integer pcpPendingPriceItemCount;
    private String pcpOutTradeNo;
    private String pcpWxPrepayId;
    private String pcpOpenId;
    private String pcpStatus;
    private Integer pcpBillId;
    private String pcpTransactionId;
    private String pcpNotifyRaw;
    private String pcpPaidAt;
    private String pcpExpireAt;
    private String pcpClosedAt;
    private String pcpCreatedAt;
    private String pcpUpdatedAt;
}
