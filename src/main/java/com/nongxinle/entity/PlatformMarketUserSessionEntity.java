package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class PlatformMarketUserSessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pmsId;
    private Integer pmuId;
    private String token;
    private Date expireAt;
    private Date createdAt;
}
