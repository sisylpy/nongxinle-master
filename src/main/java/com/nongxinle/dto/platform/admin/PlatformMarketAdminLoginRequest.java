package com.nongxinle.dto.platform.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformMarketAdminLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private String loginAccount;
    private String password;
}
