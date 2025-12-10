package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 市场管理员实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachineMarketManagerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 管理员ID
     */
    private Integer nxMmId;

    /**
     * 所属市场ID（外键→sys_city_market）
     */
    private Integer nxMmMarketId;

    /**
     * 微信OpenID（唯一）
     */
    private String nxMmWxOpenid;

    /**
     * 微信UnionID
     */
    private String nxMmWxUnionid;

    /**
     * 微信昵称
     */
    private String nxMmWxNickname;

    /**
     * 微信头像URL
     */
    private String nxMmWxAvatar;

    /**
     * 手机号
     */
    private String nxMmPhone;

    /**
     * 真实姓名
     */
    private String nxMmName;

    /**
     * 角色（1-普通管理员 2-市场主管）
     */
    private Integer nxMmRole;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer nxMmStatus;

    /**
     * 创建时间
     */
    private Date nxMmCreateTime;

    /**
     * 最后登录时间
     */
    private Date nxMmLastLoginTime;

    /**
     * 关联市场实体
     */
    private SysCityMarketEntity sysCityMarketEntity;
}

