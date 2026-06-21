package com.nongxinle.service.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nongxinle.dao.PlatformCheckoutPaymentDao;
import com.nongxinle.entity.PlatformCheckoutPaymentEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * checkoutPay 后 PENDING 且未超时支付对本批购物车行的锁定。
 */
@Service
public class PlatformCheckoutPaymentLockService {

    @Autowired
    private PlatformCheckoutPaymentDao platformCheckoutPaymentDao;
    @Autowired
    private PlatformCheckoutPaymentLifecycleService platformCheckoutPaymentLifecycleService;

    public void assertOrderIdsNotLockedByPendingPayment(Integer gbDepartmentId, List<Integer> orderIds) {
        assertOrderIdsNotLockedByPendingPayment(gbDepartmentId, orderIds, null);
    }

    public void assertOrderIdsNotLockedByPendingPayment(Integer gbDepartmentId, List<Integer> orderIds,
                                                        Integer excludePaymentId) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        platformCheckoutPaymentLifecycleService.releaseExpiredPendingPayments(gbDepartmentId);
        Set<Integer> target = new HashSet<>(orderIds);
        for (PlatformCheckoutPaymentEntity pending : listActivePendingPayments(gbDepartmentId)) {
            if (excludePaymentId != null && excludePaymentId.equals(pending.getPcpId())) {
                continue;
            }
            Set<Integer> locked = parseOrderIds(pending);
            for (Integer orderId : target) {
                if (locked.contains(orderId)) {
                    throw new IllegalArgumentException(
                            "购物车行正在支付中，暂不可修改/删除/checkout: nxOrderId=" + orderId
                                    + ", paymentId=" + pending.getPcpId());
                }
            }
        }
    }

    public void assertNxOrderNotLockedByPendingPayment(Integer gbDepartmentId, Integer nxOrderId) {
        if (nxOrderId == null) {
            return;
        }
        assertOrderIdsNotLockedByPendingPayment(gbDepartmentId, Collections.singletonList(nxOrderId));
    }

    public Map<Integer, PlatformCheckoutPaymentEntity> mapActivePaymentByOrderId(Integer gbDepartmentId) {
        platformCheckoutPaymentLifecycleService.releaseExpiredPendingPayments(gbDepartmentId);
        Map<Integer, PlatformCheckoutPaymentEntity> result = new HashMap<>();
        for (PlatformCheckoutPaymentEntity pending : listActivePendingPayments(gbDepartmentId)) {
            for (Integer orderId : parseOrderIds(pending)) {
                result.put(orderId, pending);
            }
        }
        return result;
    }

    public PlatformCheckoutPaymentEntity findActivePendingPaymentContainingOrder(
            Integer gbDepartmentId, Integer nxOrderId) {
        if (gbDepartmentId == null || nxOrderId == null) {
            return null;
        }
        return mapActivePaymentByOrderId(gbDepartmentId).get(nxOrderId);
    }

    public Set<Integer> parseOrderIds(PlatformCheckoutPaymentEntity payment) {
        if (payment == null || StringUtils.isBlank(payment.getPcpOrderIdsJson())) {
            return Collections.emptySet();
        }
        List<Integer> ids = JSON.parseObject(payment.getPcpOrderIdsJson(), new TypeReference<List<Integer>>() {
        });
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(ids);
    }

    private List<PlatformCheckoutPaymentEntity> listActivePendingPayments(Integer gbDepartmentId) {
        if (gbDepartmentId == null) {
            return Collections.emptyList();
        }
        List<PlatformCheckoutPaymentEntity> pending =
                platformCheckoutPaymentDao.queryPendingByGbDepartmentId(gbDepartmentId);
        if (pending == null || pending.isEmpty()) {
            return Collections.emptyList();
        }
        List<PlatformCheckoutPaymentEntity> active = new java.util.ArrayList<>();
        for (PlatformCheckoutPaymentEntity payment : pending) {
            if (platformCheckoutPaymentLifecycleService.isActiveLock(payment)) {
                active.add(payment);
            }
        }
        return active;
    }
}
