package com.nongxinle.dto.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosMemberSearchRequest {
    private Integer communityId;
    private String keyword;
}
