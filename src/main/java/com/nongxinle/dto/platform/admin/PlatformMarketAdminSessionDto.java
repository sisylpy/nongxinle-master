package com.nongxinle.dto.platform.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformMarketAdminSessionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private Integer currentMarketUserId;
    private Integer marketId;
    private String loginAccount;
    private String realName;
    private String roleType;
    private String status;
    private String marketName;
}
