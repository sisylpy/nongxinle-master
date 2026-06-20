package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformDistributerOrderIdRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer disId;
    private Integer orderId;
    private Integer operatorId;
}
