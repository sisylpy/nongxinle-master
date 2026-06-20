package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformDistributerOrderWeightRequest extends PlatformDistributerOrderIdRequest {
    private static final long serialVersionUID = 1L;

    private String weight;
}
