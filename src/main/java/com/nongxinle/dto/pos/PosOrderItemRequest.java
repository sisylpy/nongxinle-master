package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosOrderItemRequest {
    private Integer goodsId;
    private Integer quantity;
    private String remark;
}
