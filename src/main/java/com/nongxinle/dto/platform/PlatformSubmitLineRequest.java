package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformSubmitLineRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer departmentId;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
    private Integer orderUserId;
}
