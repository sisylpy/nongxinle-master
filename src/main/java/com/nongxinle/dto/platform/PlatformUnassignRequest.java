package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformUnassignRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer orderId;
    private Integer operatorId;
    private String reasonNote;
}
