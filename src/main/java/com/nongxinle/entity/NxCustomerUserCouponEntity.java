package com.nongxinle.entity;

/**
 * 用户优惠券实例
 */
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NxCustomerUserCouponEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCustomerUserCouponId;
    private Integer nxCucCouponId;
    private Integer nxCucCustomerUserId;
    private Integer nxCucShareUserId;
    private Integer nxCucCommunityId;
    private Integer nxCucStatus;
    /** active_claim / gift_transfer / referral_reward / auto_grant */
    private String nxCucSourceType;
    /** 来源业务记录 id，如 referral_reward_id */
    private Integer nxCucSourceBizId;
    /** 锁定/核销时绑定的订单 ID */
    private Integer nxCucOrderId;
    private Integer nxCucFromShareUserId;
    private String nxCucShareTime;
    /** 实例级有效开始日（yyyy-MM-dd） */
    private String nxCucStartDate;
    /** 实例级有效截止日（yyyy-MM-dd，含当日） */
    private String nxCucStopDate;

    private NxCommunityCouponEntity nxCommunityCouponEntity;
    private NxCustomerUserEntity shareUser;
}
