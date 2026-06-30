package com.nongxinle.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NxCommunityCouponVerifyLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCouponVerifyLogId;
    private Integer nxCvlUserCouponId;
    private Integer nxCvlCouponId;
    private Integer nxCvlCommunityId;
    private String nxCvlVerifyType;
    private Integer nxCvlOrderId;
    private Integer nxCvlOrderSubId;
    private Integer nxCvlDeskId;
    private Integer nxCvlOperatorId;
    private Integer nxCvlBeforeStatus;
    private Integer nxCvlAfterStatus;
    private String nxCvlRemark;
    private String nxCvlCreateAt;
}
