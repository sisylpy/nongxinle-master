package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformOutstandingBillInfo;
import com.nongxinle.entity.GbDepartmentBillEntity;

public interface PlatformOutstandingBillService {

    GbDepartmentBillEntity findBlockingPlatformCashBill(Integer gbDepartmentId);

    boolean hasOutstandingPlatformCashBill(Integer gbDepartmentId);

    PlatformOutstandingBillInfo buildOutstandingInfo(GbDepartmentBillEntity bill);

    PlatformOutstandingBillInfo findOutstandingInfo(Integer gbDepartmentId);

    /**
     * checkoutConfirm 前阻断：存在未结清的平台 cash bill 时不允许再 checkout 生成新 bill。
     * 不阻断写入购物车临时行（status=-1）；与「补款 paySupplement」不同，禁止向旧 bill 追加商品。
     */
    void assertNotBlockedForNewSubmit(Integer gbDepartmentId);
}
