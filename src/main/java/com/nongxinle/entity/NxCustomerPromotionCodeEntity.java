package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerPromotionCodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerPromotionCodeId;
    private String promotionCode;
    private String ownerType;
    private Integer ownerId;
    private Integer commerceId;
    private Integer communityId;
    private String codeStatus;
    private Date validStartAt;
    private Date validEndAt;
    private Date disabledAt;
    private String disabledReason;
    private Integer useCount;
    private Integer validRegisterCount;
    private Integer invalidRegisterCount;
    private Integer rewardRuleId;
    private Date createdAt;
    private Date updatedAt;

    /** 推广主体展示名（昵称/姓名） */
    private String ownerName;
    /** 推广主体手机号 */
    private String ownerPhone;
    /** 社区名称 */
    private String communityName;
}
