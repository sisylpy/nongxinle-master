package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformPendingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    /** 订货日期 yyyy-MM-dd，缺省为当天 */
    private String applyDate;
}
