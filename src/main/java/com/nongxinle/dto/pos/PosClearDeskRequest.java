package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosClearDeskRequest {
    private Integer communityId;
    private Integer deskId;
    private Integer operatorId;
}
