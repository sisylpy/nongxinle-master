package com.nongxinle.service.impl;

import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.service.platform.PlatformDisGoodsCostResolver;
import com.nongxinle.service.platform.PlatformOutboundFinishSupport;
import com.nongxinle.utils.PlatformConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service("platformOrderFulfillmentService")
public class PlatformOrderFulfillmentServiceImpl implements PlatformOrderFulfillmentService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformOrderFulfillmentServiceImpl.class);

    @Autowired
    private NxPlatformOrderFulfillmentDao nxPlatformOrderFulfillmentDao;
    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;

    @Override
    public NxPlatformOrderFulfillmentEntity queryByOrderId(Integer orderId) {
        return nxPlatformOrderFulfillmentDao.queryByOrderId(orderId);
    }

    @Override
    @Transactional
    public NxPlatformOrderFulfillmentEntity ensureAssignedFulfillment(
            NxPlatformOrderAssignEntity poa,
            NxDepartmentOrdersEntity order,
            PlatformDisGoodsCostResolver.CostResolveResult costResult,
            Integer operatorId) {

        if (poa == null || order == null) {
            throw new IllegalArgumentException("平台 assign / 订单不能为空");
        }
        if (!PlatformConstants.ASSIGN_MODE_PLATFORM.equals(poa.getNxPoaAssignMode())) {
            throw new IllegalArgumentException("非平台订单，不创建 fulfillment");
        }
        if (!PlatformConstants.ASSIGN_STATUS_ASSIGNED.equals(poa.getNxPoaAssignStatus())) {
            throw new IllegalArgumentException("assign 未完成，不创建 fulfillment");
        }

        Integer orderId = poa.getNxPoaOrderId();
        NxPlatformOrderFulfillmentEntity existing = nxPlatformOrderFulfillmentDao.queryByOrderId(orderId);
        if (existing != null) {
            logger.info("[ensureAssignedFulfillment] 已存在 fulfillment orderId={} pofId={} status={}",
                    orderId, existing.getNxPofId(), existing.getNxPofFulfillmentStatus());
            return existing;
        }

        if (poa.getNxPoaId() == null || orderId == null
                || poa.getNxPoaAssignedDistributerId() == null || poa.getNxPoaAssignedDisGoodsId() == null
                || order.getNxDoDistributerId() == null || order.getNxDoDisGoodsId() == null) {
            throw new IllegalStateException("assign 字段不完整，无法创建 fulfillment: orderId=" + orderId);
        }

        Date now = new Date();
        NxPlatformOrderFulfillmentEntity pof = new NxPlatformOrderFulfillmentEntity();
        pof.setNxPofMarketId(poa.getNxPoaMarketId());
        pof.setNxPofOrderId(orderId);
        pof.setNxPofPlatformAssignId(poa.getNxPoaId());
        pof.setNxPofDepartmentId(poa.getNxPoaDepartmentId());
        pof.setNxPofNxGoodsId(poa.getNxPoaNxGoodsId() != null ? poa.getNxPoaNxGoodsId() : order.getNxDoNxGoodsId());
        pof.setNxPofDistributerId(poa.getNxPoaAssignedDistributerId());
        pof.setNxPofDisGoodsId(poa.getNxPoaAssignedDisGoodsId());
        pof.setNxPofFulfillmentStatus(PlatformConstants.FULFILLMENT_STATUS_ASSIGNED);
        applyCostMissingFields(pof, costResult);
        pof.setNxPofCreatedAt(poa.getNxPoaAssignedAt() != null ? poa.getNxPoaAssignedAt() : now);
        pof.setNxPofUpdatedAt(now);
        pof.setNxPofUpdatedBy(operatorId);

        nxPlatformOrderFulfillmentDao.save(pof);
        logger.info("[ensureAssignedFulfillment] 新建 fulfillment orderId={} pofId={} costMissing={}",
                orderId, pof.getNxPofId(), pof.getNxPofCostMissing());
        return pof;
    }

    @Override
    public void tryResolveOutboundCost(NxDepartmentOrdersEntity order) {
        if (order == null || order.getNxDepartmentOrdersId() == null) {
            return;
        }
        NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(order.getNxDepartmentOrdersId());
        if (!isPlatformAssigned(poa)) {
            return;
        }
        if (PlatformOutboundFinishSupport.hasValidOrderCostPrice(order)) {
            return;
        }
        PlatformDisGoodsCostResolver.CostResolveResult costResult = resolveCostFromDisGoods(order);
        PlatformOutboundFinishSupport.applyCostToOrder(order, costResult);
    }

    @Override
    public void applyOutboundCostSubtotalIfValid(NxDepartmentOrdersEntity order) {
        BigDecimal weight = PlatformOutboundFinishSupport.resolveOutboundWeight(order);
        if (weight != null) {
            PlatformOutboundFinishSupport.applyCostSubtotalIfValid(order, weight);
        }
    }

    @Override
    @Transactional
    public void syncReadyForPickupAfterOutboundFinish(NxDepartmentOrdersEntity order, Integer operatorId) {
        if (order == null || order.getNxDepartmentOrdersId() == null) {
            return;
        }
        if (!isOutboundFinished(order)) {
            return;
        }

        NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(order.getNxDepartmentOrdersId());
        if (!isPlatformAssigned(poa)) {
            return;
        }

        PlatformDisGoodsCostResolver.CostResolveResult costResult = resolveOutboundCostResult(order);
        NxPlatformOrderFulfillmentEntity pof = ensureAssignedFulfillment(poa, order, costResult, operatorId);

        String status = pof.getNxPofFulfillmentStatus();
        if (isFulfillmentPastReadyForPickup(status)) {
            logger.info("[syncReadyForPickup] 跳过终态不回退 orderId={} status={}",
                    order.getNxDepartmentOrdersId(), status);
            return;
        }

        Date now = new Date();
        NxPlatformOrderFulfillmentEntity patch = new NxPlatformOrderFulfillmentEntity();
        patch.setNxPofId(pof.getNxPofId());
        patch.setNxPofUpdatedAt(now);
        patch.setNxPofUpdatedBy(operatorId);
        applyCostMissingFields(patch, costResult);

        if (PlatformConstants.FULFILLMENT_STATUS_ASSIGNED.equals(status)) {
            patch.setNxPofFulfillmentStatus(PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP);
            patch.setNxPofReadyForPickupAt(now);
        } else if (PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP.equals(status)) {
            if (pof.getNxPofReadyForPickupAt() == null) {
                patch.setNxPofReadyForPickupAt(now);
            }
        } else {
            return;
        }

        nxPlatformOrderFulfillmentDao.updateFulfillment(patch);
        logger.info("[syncReadyForPickup] orderId={} pofId={} -> READY_FOR_PICKUP costMissing={}",
                order.getNxDepartmentOrdersId(), pof.getNxPofId(), patch.getNxPofCostMissing());
    }

    @Override
    @Transactional
    public void revertReadyForPickupAfterOutboundCancel(NxDepartmentOrdersEntity order, Integer operatorId) {
        if (order == null || order.getNxDepartmentOrdersId() == null) {
            return;
        }
        NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(order.getNxDepartmentOrdersId());
        if (!isPlatformAssigned(poa)) {
            return;
        }
        NxPlatformOrderFulfillmentEntity pof = nxPlatformOrderFulfillmentDao.queryByOrderId(order.getNxDepartmentOrdersId());
        if (pof == null) {
            return;
        }
        String status = pof.getNxPofFulfillmentStatus();
        if (!PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP.equals(status)) {
            logger.info("[revertReadyForPickup] 跳过非 READY 状态 orderId={} status={}",
                    order.getNxDepartmentOrdersId(), status);
            return;
        }
        int rows = nxPlatformOrderFulfillmentDao.revertReadyForPickupToAssigned(
                order.getNxDepartmentOrdersId(), operatorId);
        logger.info("[revertReadyForPickup] orderId={} rows={}", order.getNxDepartmentOrdersId(), rows);
    }

    private PlatformDisGoodsCostResolver.CostResolveResult resolveOutboundCostResult(
            NxDepartmentOrdersEntity order) {
        if (PlatformOutboundFinishSupport.hasValidOrderCostPrice(order)) {
            return PlatformDisGoodsCostResolver.CostResolveResult.resolved(
                    order.getNxDoCostPrice().trim(),
                    order.getNxDoCostPriceUpdate(),
                    order.getNxDoCostPriceLevel());
        }
        return resolveCostFromDisGoods(order);
    }

    private PlatformDisGoodsCostResolver.CostResolveResult resolveCostFromDisGoods(
            NxDepartmentOrdersEntity order) {
        if (order.getNxDoDisGoodsId() == null) {
            return PlatformDisGoodsCostResolver.CostResolveResult.missing(
                    PlatformDisGoodsCostResolver.REASON_NO_VALID_BUYING_PRICE);
        }
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(order.getNxDoDisGoodsId());
        return PlatformDisGoodsCostResolver.resolve(disGoods, order);
    }

    private static boolean isPlatformAssigned(NxPlatformOrderAssignEntity poa) {
        return poa != null
                && PlatformConstants.ASSIGN_MODE_PLATFORM.equals(poa.getNxPoaAssignMode())
                && PlatformConstants.ASSIGN_STATUS_ASSIGNED.equals(poa.getNxPoaAssignStatus());
    }

    private static boolean isOutboundFinished(NxDepartmentOrdersEntity order) {
        return order.getNxDoPurchaseStatus() != null && order.getNxDoPurchaseStatus() >= 4;
    }

    private static boolean isFulfillmentPastReadyForPickup(String status) {
        return PlatformConstants.FULFILLMENT_STATUS_PICKED_UP.equals(status)
                || PlatformConstants.FULFILLMENT_STATUS_DELIVERING.equals(status)
                || PlatformConstants.FULFILLMENT_STATUS_DELIVERED.equals(status)
                || PlatformConstants.FULFILLMENT_STATUS_SUPPLIER_EXCEPTION.equals(status);
    }

    private static void applyCostMissingFields(
            NxPlatformOrderFulfillmentEntity target,
            PlatformDisGoodsCostResolver.CostResolveResult costResult) {
        if (costResult != null && costResult.isCostMissing()) {
            target.setNxPofCostMissing(1);
            target.setNxPofCostMissingReason(costResult.getCostMissingReason());
        } else {
            target.setNxPofCostMissing(0);
            target.setNxPofCostMissingReason(null);
        }
    }
}
