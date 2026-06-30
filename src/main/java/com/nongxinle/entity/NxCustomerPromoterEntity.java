package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerPromoterEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerPromoterId;
    private String promoterName;
    private String promoterPhone;
    private String promoterType;
    private Integer commerceId;
    private Integer communityId;
    private String promoterStatus;
    private Date cooperationStartAt;
    private Date cooperationEndAt;
    private Date disabledAt;
    private String disabledReason;
    private Date createdAt;
    private Date updatedAt;

    private Integer qualifiedReferralCount;

    /** 当前有效推广码；无 ACTIVE 时取最近一条 */
    private String promotionCode;
    private Integer promotionCodeId;
    private String promotionCodeStatus;

    /** 社区名称（列表展示） */
    private String communityName;
}
