package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 配送商邀请注册记录
 *
 * @author lpy
 */
@Setter
@Getter
@ToString
public class NxDistributerInviteEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDistributerInviteId;
    private Integer inviterNxDistributerId;
    private String inviteCode;
    private String inviteePhone;
    private Integer inviteType;
    private Integer status;
    private Integer inviteeNxDistributerId;
    private Date createdAt;
    private Date registeredAt;
    private Integer rewardStatus;
    private BigDecimal rewardAmount;
    private String rewardType;

    /** 状态：待注册 */
    public static final int STATUS_PENDING = 0;
    /** 状态：已注册 */
    public static final int STATUS_REGISTERED = 1;
    /** 状态：已过期 */
    public static final int STATUS_EXPIRED = 2;

    /** 奖励：待发放 */
    public static final int REWARD_PENDING = 0;
    /** 奖励：已发放 */
    public static final int REWARD_GRANTED = 1;
}
