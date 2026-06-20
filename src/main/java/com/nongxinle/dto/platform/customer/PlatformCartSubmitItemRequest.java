package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCartSubmitItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDistributerGoodsId;
    private Integer nxGoodsId;
    private String goodsName;
    private String quantity;
    private String standard;
    private String remark;
    /** 仅前端回显参考，后端不以之为支付主权 */
    private String expectPrice;
}
