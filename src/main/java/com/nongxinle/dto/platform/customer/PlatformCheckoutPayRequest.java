package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class PlatformCheckoutPayRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer gbDepartmentId;
    private Integer gbDepartmentFatherId;
    private Integer gbDistributerId;
    private Integer gbOrderUserId;
    private String deliveryDate;
    private String remark;
    private List<Integer> orderIds;
    private String checkoutToken;
    /** 小程序 JSAPI 支付必填 */
    private String openId;
}
