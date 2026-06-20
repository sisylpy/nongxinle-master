package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCartSubmitRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String submitToken;
    private Integer marketId;
    private Integer gbDepartmentId;
    private Integer gbDepartmentFatherId;
    private Integer gbDistributerId;
    private Integer nxDistributerId;
    private Integer gbOrderUserId;
    private String deliveryDate;
    private String remark;
    private List<PlatformCartSubmitItemRequest> items;
}
