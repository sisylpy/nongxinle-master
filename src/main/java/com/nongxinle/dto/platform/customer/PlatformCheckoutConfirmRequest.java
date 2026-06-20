package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCheckoutConfirmRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer gbDepartmentId;
    private Integer gbDepartmentFatherId;
    private Integer gbDistributerId;
    private Integer gbOrderUserId;
    private String deliveryDate;
    private String remark;
    /** NX 订单 ID，均为购物车 status=-1 */
    private List<Integer> orderIds;
    /** checkout 批次幂等 token */
    private String checkoutToken;
}
