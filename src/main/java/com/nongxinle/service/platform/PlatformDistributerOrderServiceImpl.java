package com.nongxinle.service.platform;

import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderIdRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderLinesRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderPriceRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderWeightRequest;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.service.platform.PlatformCartLineSupport;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.PlatformOrderDisplaySupport;
import com.nongxinle.utils.PlatformOrderPriceSupport;
import com.nongxinle.utils.SalesPriceUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishOut;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusHasFinished;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusNew;

@Service
public class PlatformDistributerOrderServiceImpl implements PlatformDistributerOrderService {

    private static final Logger log = LoggerFactory.getLogger(PlatformDistributerOrderServiceImpl.class);

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private PlatformOrderFulfillmentService platformOrderFulfillmentService;
    @Autowired
    private PlatformDistributerGbOrderSyncService platformDistributerGbOrderSyncService;

    @Override
    public List<NxDepartmentOrdersSimpleDTO> listOrderLines(PlatformDistributerOrderLinesRequest request) {
        validateLinesRequest(request);
        Map<String, Object> map = buildCustomerQueryMap(request);
        List<NxDepartmentOrdersSimpleDTO> lines =
                nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);
        List<NxDepartmentOrdersSimpleDTO> platformLines = filterPlatformLines(lines, request.getDisId());
        PlatformOrderDisplaySupport.enrichSimpleOrderList(platformLines);
        log.info("[platform/distributer/orders/lines] disId={} depFatherId={} gbDepFatherId={} count={}",
                request.getDisId(), request.getDepFatherId(), request.getGbDepFatherId(), platformLines.size());
        return platformLines;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveWeight(PlatformDistributerOrderWeightRequest request) {
        PlatformOrderContext ctx = loadAndValidateOrder(request, request.getOrderId());
        assertNotOutboundFinished(ctx.order, "已出库完成，请使用 cancelOutbound 后再改重量");
        requireWeight(request.getWeight());

        applyWeightAndSubtotal(ctx.order, request.getWeight().trim());
        PlatformOrderPriceSupport.recalculatePriceDifferent(ctx.order);
        nxDepartmentOrdersService.update(ctx.order);
        platformDistributerGbOrderSyncService.syncAfterWeightSave(ctx.order);

        log.info("[platform/distributer/orders/saveWeight] orderId={} weight={}", request.getOrderId(), request.getWeight());
        return buildOrderResult(ctx.order, "WEIGHT_SAVED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> finishOutbound(PlatformDistributerOrderWeightRequest request) {
        PlatformOrderContext ctx = loadAndValidateOrder(request, request.getOrderId());
        assertNotOutboundFinished(ctx.order, "订单已出库完成");
        requireWeight(request.getWeight());

        applyWeightAndSubtotal(ctx.order, request.getWeight().trim());
        if (SalesPriceUtils.isValidSalesPrice(ctx.order.getNxDoPrice())) {
            ctx.order.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        platformOrderFulfillmentService.tryResolveOutboundCost(ctx.order);
        platformOrderFulfillmentService.applyOutboundCostSubtotalIfValid(ctx.order);
        ctx.order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ctx.order);
        platformDistributerGbOrderSyncService.syncAfterOutboundFinish(ctx.order);
        platformOrderFulfillmentService.syncReadyForPickupAfterOutboundFinish(ctx.order, request.getOperatorId());

        log.info("[platform/distributer/orders/finishOutbound] orderId={} weight={}", request.getOrderId(), request.getWeight());
        return buildOrderResult(ctx.order, "OUTBOUND_FINISHED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePrice(PlatformDistributerOrderPriceRequest request) {
        PlatformOrderContext ctx = loadAndValidateOrder(request, request.getOrderId());
        if (StringUtils.isBlank(request.getPrice())) {
            throw new IllegalArgumentException("price 不能为空");
        }
        if (!SalesPriceUtils.isValidSalesPrice(request.getPrice())) {
            throw new IllegalArgumentException("平台单实际单价无效");
        }

        PlatformOrderPriceSupport.applyActualPriceChange(ctx.order, request.getPrice().trim());
        if (ctx.order.getNxDoWeight() != null && StringUtils.isNotBlank(ctx.order.getNxDoWeight())
                && new BigDecimal(ctx.order.getNxDoWeight()).compareTo(BigDecimal.ZERO) > 0
                && isOutboundFinished(ctx.order)) {
            ctx.order.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        nxDepartmentOrdersService.update(ctx.order);
        platformDistributerGbOrderSyncService.syncAfterPriceUpdate(ctx.order);

        log.info("[platform/distributer/orders/updatePrice] orderId={} price={}", request.getOrderId(), request.getPrice());
        return buildOrderResult(ctx.order, "PRICE_UPDATED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelOutbound(PlatformDistributerOrderIdRequest request) {
        PlatformOrderContext ctx = loadAndValidateOrder(request, request.getOrderId());
        if (!isOutboundFinished(ctx.order)) {
            throw new IllegalArgumentException("订单尚未出库完成，无需取消出库");
        }

        ctx.order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        ctx.order.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ctx.order);
        platformDistributerGbOrderSyncService.syncAfterCancelOutbound(ctx.order);
        platformOrderFulfillmentService.revertReadyForPickupAfterOutboundCancel(ctx.order, request.getOperatorId());

        log.info("[platform/distributer/orders/cancelOutbound] orderId={}", request.getOrderId());
        return buildOrderResult(ctx.order, "OUTBOUND_CANCELLED");
    }

    private List<NxDepartmentOrdersSimpleDTO> filterPlatformLines(
            List<NxDepartmentOrdersSimpleDTO> lines, Integer disId) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> platformOrderIds = loadPlatformOrderIds(lines, disId);
        List<NxDepartmentOrdersSimpleDTO> result = new ArrayList<>();
        for (NxDepartmentOrdersSimpleDTO line : lines) {
            if (line != null && line.getNxDepartmentOrdersId() != null
                    && PlatformCartLineSupport.isFormalStatus(line.getNxDoStatus())
                    && platformOrderIds.contains(line.getNxDepartmentOrdersId())) {
                result.add(line);
            }
        }
        return result;
    }

    private Set<Integer> loadPlatformOrderIds(List<NxDepartmentOrdersSimpleDTO> lines, Integer disId) {
        Set<Integer> ids = new HashSet<>();
        for (NxDepartmentOrdersSimpleDTO line : lines) {
            if (line == null || line.getNxDepartmentOrdersId() == null) {
                continue;
            }
            NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(line.getNxDepartmentOrdersId());
            if (isPlatformAssigned(poa)) {
                ids.add(line.getNxDepartmentOrdersId());
            }
        }
        return ids;
    }

    private PlatformOrderContext loadAndValidateOrder(PlatformDistributerOrderIdRequest request, Integer orderId) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: orderId=" + orderId);
        }
        if (PlatformCartLineSupport.isCartStatus(order.getNxDoStatus())) {
            throw new IllegalArgumentException("购物车行不可操作: orderId=" + orderId);
        }
        NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(orderId);
        if (!isPlatformAssigned(poa)) {
            throw new IllegalArgumentException("非本平台分配订单或不属于当前配送商");
        }
        if (!belongsToDis(order, poa, request.getDisId())) {
            throw new IllegalArgumentException("订单不属于当前配送商");
        }
        return new PlatformOrderContext(order, poa);
    }

    private static boolean isPlatformAssigned(NxPlatformOrderAssignEntity poa) {
        return poa != null
                && PlatformConstants.ASSIGN_MODE_PLATFORM.equals(poa.getNxPoaAssignMode())
                && PlatformConstants.ASSIGN_STATUS_ASSIGNED.equals(poa.getNxPoaAssignStatus());
    }

    private static boolean belongsToDis(
            NxDepartmentOrdersEntity order, NxPlatformOrderAssignEntity poa, Integer disId) {
        if (disId.equals(order.getNxDoDistributerId())) {
            return true;
        }
        return poa != null && disId.equals(poa.getNxPoaAssignedDistributerId());
    }

    private static void validateLinesRequest(PlatformDistributerOrderLinesRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getDepFatherId() == null && request.getGbDepFatherId() == null) {
            throw new IllegalArgumentException("depFatherId 与 gbDepFatherId 不能同时为空");
        }
    }

    private Map<String, Object> buildCustomerQueryMap(PlatformDistributerOrderLinesRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", request.getDisId());
        map.put("status", 3);
        map.put("dayuStatus", -1);
        if (request.getDepFatherId() != null) {
            map.put("depFatherId", request.getDepFatherId());
        }
        if (request.getGbDepFatherId() != null) {
            map.put("gbDepFatherId", request.getGbDepFatherId());
        }
        map.put("resFatherId", request.getResFatherId() != null ? request.getResFatherId() : -1);
        return map;
    }

    private static void requireWeight(String weight) {
        if (StringUtils.isBlank(weight)) {
            throw new IllegalArgumentException("weight 不能为空");
        }
    }

    private static void assertNotOutboundFinished(NxDepartmentOrdersEntity order, String message) {
        if (isOutboundFinished(order)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isOutboundFinished(NxDepartmentOrdersEntity order) {
        return order.getNxDoPurchaseStatus() != null
                && order.getNxDoPurchaseStatus() >= getNxDepOrderBuyStatusFinishOut();
    }

    private static void applyWeightAndSubtotal(NxDepartmentOrdersEntity order, String weight) {
        order.setNxDoWeight(weight);
        if (SalesPriceUtils.isValidSalesPrice(order.getNxDoPrice())) {
            BigDecimal subtotal = new BigDecimal(order.getNxDoPrice().trim())
                    .multiply(new BigDecimal(weight))
                    .setScale(1, BigDecimal.ROUND_HALF_UP);
            order.setNxDoSubtotal(subtotal.toPlainString());
        }
    }

    private Map<String, Object> buildOrderResult(NxDepartmentOrdersEntity order, String action) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getNxDepartmentOrdersId());
        result.put("action", action);
        result.put("weight", order.getNxDoWeight());
        result.put("price", order.getNxDoPrice());
        result.put("expectPrice", order.getNxDoExpectPrice());
        result.put("priceDifferent", order.getNxDoPriceDifferent());
        result.put("subtotal", order.getNxDoSubtotal());
        result.put("purchaseStatus", order.getNxDoPurchaseStatus());
        result.put("status", order.getNxDoStatus());
        result.put("isPlatformOrder", 1);
        return result;
    }

    private static final class PlatformOrderContext {
        private final NxDepartmentOrdersEntity order;
        private final NxPlatformOrderAssignEntity poa;

        private PlatformOrderContext(NxDepartmentOrdersEntity order, NxPlatformOrderAssignEntity poa) {
            this.order = order;
            this.poa = poa;
        }
    }
}
