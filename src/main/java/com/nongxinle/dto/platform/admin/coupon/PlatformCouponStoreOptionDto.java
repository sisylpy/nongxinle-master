package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformCouponStoreOptionDto {

    /** 门店级 gb_department_id（持券主体） */
    private Integer storeGbDepartmentId;
    private String storeName;
}
