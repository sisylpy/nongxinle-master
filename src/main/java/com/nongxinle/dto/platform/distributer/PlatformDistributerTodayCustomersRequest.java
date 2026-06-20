package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformDistributerTodayCustomersRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 配送商 nx_distributer_id */
    private Integer disId;
}
