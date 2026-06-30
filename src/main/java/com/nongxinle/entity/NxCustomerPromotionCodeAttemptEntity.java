package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerPromotionCodeAttemptEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerPromotionCodeAttemptId;
    private Integer inviteeUserId;
    private Integer promotionCodeId;
    private String promotionCodeSnapshot;
    private String sourceOwnerType;
    private Integer sourceOwnerId;
    private String invalidReason;
    private Integer commerceId;
    private Integer communityId;
    private String shareEntry;
    private Date attemptedAt;

    private NxCustomerUserEntity inviteeUser;
}
