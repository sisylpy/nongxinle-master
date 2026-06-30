package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosPaymentCreateRequest {
    private Integer orderId;
    private String payChannel;
}
