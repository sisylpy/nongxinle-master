package com.nongxinle.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NxCommunityPosPaymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxPosPaymentId;
    private Integer nxPpOrderId;
    private Integer nxPpCommunityId;
    private String nxPpPayChannel;
    private String nxPpOutTradeNo;
    private String nxPpTransactionId;
    private BigDecimal nxPpAmount;
    private String nxPpStatus;
    private String nxPpQrCodeUrl;
    private Integer nxPpOperatorId;
    private String nxPpPaidAt;
    private String nxPpNotifyRaw;
    private String nxPpCreateAt;
    private String nxPpExpireAt;
}
