package com.nongxinle.dto.platform;

import com.nongxinle.dto.platform.customer.PlatformCartSubmitItemRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformCartLineCreateCommand {

    /** checkoutConfirm 正式化内部挂 bill 时使用；cartOnly 时必须为 null（禁止追加到已有 bill） */
    private Integer billId;
    private Integer marketId;
    private Integer gbDepartmentId;
    private Integer gbDepartmentFatherId;
    private Integer gbDistributerId;
    private Integer nxDistributerId;
    private Integer gbOrderUserId;
    private String deliveryDate;
    private String remark;
    /** true：购物车临时阶段，仅写 status=-1 临时行；禁止 bill / assign / fulfillment */
    private boolean cartOnly;
    private PlatformCartSubmitItemRequest item;
}
