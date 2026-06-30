package com.nongxinle.dto.pos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosSaveOrderRequest {
    private Integer communityId;
    private Integer operatorId;
    private Integer deskId;
    private String remark;
    /** 会员用户 ID，用于购物车实时券计算 */
    private Integer userId;
    private List<PosOrderItemRequest> items;
}
