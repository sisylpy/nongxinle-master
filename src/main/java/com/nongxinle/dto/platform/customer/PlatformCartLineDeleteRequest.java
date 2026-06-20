package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCartLineDeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer gbDepartmentId;
    /** NX 购物车行 ID（nx_department_orders_id） */
    private Integer nxOrderId;
}
