package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class PlatformMarketUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pmuId;
    private Integer marketId;
    private String loginAccount;
    private String phone;
    private String passwordHash;
    private String realName;
    private String roleType;
    private String status;
    private Date lastLoginAt;
    private Date createdAt;
    private Date updatedAt;
}
