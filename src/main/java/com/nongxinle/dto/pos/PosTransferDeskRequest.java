package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosTransferDeskRequest {
    private Integer communityId;
    private Integer orderId;
    private Integer fromDeskId;
    private Integer toDeskId;
    private Integer operatorId;
}
