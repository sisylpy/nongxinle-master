package com.nongxinle.dao;

import com.nongxinle.entity.PlatformCheckoutPaymentEntity;

import java.util.List;
import java.util.Map;

public interface PlatformCheckoutPaymentDao extends BaseDao<PlatformCheckoutPaymentEntity> {

    PlatformCheckoutPaymentEntity queryByCheckoutToken(String checkoutToken);

    PlatformCheckoutPaymentEntity queryByOutTradeNo(String outTradeNo);

    List<PlatformCheckoutPaymentEntity> queryList(Map<String, Object> map);

    List<PlatformCheckoutPaymentEntity> queryPendingByGbDepartmentId(Integer gbDepartmentId);

    /**
     * 仅 PENDING → SUCCESS 一次；返回受影响行数（0 表示已被其他线程 finalize）。
     */
    int finalizeSuccessIfPending(PlatformCheckoutPaymentEntity payment);

    int closeIfPending(PlatformCheckoutPaymentEntity payment);
}
