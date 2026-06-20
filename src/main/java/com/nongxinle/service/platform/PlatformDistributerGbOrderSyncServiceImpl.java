package com.nongxinle.service.platform;

import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.GbDistributerPurchaseGoodsEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerPurchaseGoodsService;
import com.nongxinle.utils.SalesPriceUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.GbTypeUtils.getGbOrderBuyStatusHasWeightAndPrice;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusProcurement;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsIsPurchase;

@Service
public class PlatformDistributerGbOrderSyncServiceImpl implements PlatformDistributerGbOrderSyncService {

    private static final Logger log = LoggerFactory.getLogger(PlatformDistributerGbOrderSyncServiceImpl.class);

    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;

    @Override
    public void syncAfterWeightSave(NxDepartmentOrdersEntity nxOrder) {
        syncWeight(nxOrder, false);
    }

    @Override
    public void syncAfterOutboundFinish(NxDepartmentOrdersEntity nxOrder) {
        syncWeight(nxOrder, true);
    }

    @Override
    public void syncAfterPriceUpdate(NxDepartmentOrdersEntity nxOrder) {
        GbDepartmentOrdersEntity gbOrder = loadLinkedGbOrder(nxOrder);
        if (gbOrder == null) {
            return;
        }
        if (SalesPriceUtils.isValidSalesPrice(nxOrder.getNxDoPrice())) {
            gbOrder.setGbDoPrice(nxOrder.getNxDoPrice().trim());
        }
        if (nxOrder.getNxDoPriceDifferent() != null) {
            gbOrder.setGbDoPriceDifferent(nxOrder.getNxDoPriceDifferent());
        }
        if (StringUtils.isNotBlank(nxOrder.getNxDoSubtotal())) {
            gbOrder.setGbDoSubtotal(nxOrder.getNxDoSubtotal());
        } else {
            recalcGbSubtotal(gbOrder);
        }
        gbDepartmentOrdersService.update(gbOrder);
        log.info("[platform/gb-sync/price] nxOrderId={} gbOrderId={} price={}",
                nxOrder.getNxDepartmentOrdersId(), gbOrder.getGbDepartmentOrdersId(), gbOrder.getGbDoPrice());
    }

    @Override
    public void syncAfterCancelOutbound(NxDepartmentOrdersEntity nxOrder) {
        GbDepartmentOrdersEntity gbOrder = loadLinkedGbOrder(nxOrder);
        if (gbOrder == null) {
            return;
        }
        gbOrder.setGbDoStatus(0);
        gbOrder.setGbDoBuyStatus(0);
        gbDepartmentOrdersService.update(gbOrder);
        log.info("[platform/gb-sync/cancelOutbound] nxOrderId={} gbOrderId={}",
                nxOrder.getNxDepartmentOrdersId(), gbOrder.getGbDepartmentOrdersId());
    }

    private void syncWeight(NxDepartmentOrdersEntity nxOrder, boolean outboundFinished) {
        GbDepartmentOrdersEntity gbOrder = loadLinkedGbOrder(nxOrder);
        if (gbOrder == null) {
            return;
        }
        gbOrder.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
        gbOrder.setGbDoStatus(getGbOrderStatusProcurement());
        gbOrder.setGbDoWeight(nxOrder.getNxDoWeight());
        recalcGbSubtotal(gbOrder);
        gbDepartmentOrdersService.update(gbOrder);
        syncGbPurchaseGoodsWeight(gbOrder, outboundFinished);
        log.info("[platform/gb-sync/weight] nxOrderId={} gbOrderId={} weight={} outboundFinished={}",
                nxOrder.getNxDepartmentOrdersId(), gbOrder.getGbDepartmentOrdersId(),
                nxOrder.getNxDoWeight(), outboundFinished);
    }

    private GbDepartmentOrdersEntity loadLinkedGbOrder(NxDepartmentOrdersEntity nxOrder) {
        if (nxOrder == null || nxOrder.getNxDoGbDepartmentOrderId() == null) {
            return null;
        }
        GbDepartmentOrdersEntity gbOrder =
                gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxOrder.getNxDepartmentOrdersId());
        if (gbOrder == null) {
            gbOrder = gbDepartmentOrdersService.queryObject(nxOrder.getNxDoGbDepartmentOrderId());
        }
        return gbOrder;
    }

    private static void recalcGbSubtotal(GbDepartmentOrdersEntity gbOrder) {
        if (gbOrder.getGbDoPrice() == null || gbOrder.getGbDoWeight() == null) {
            return;
        }
        String price = gbOrder.getGbDoPrice().trim();
        if (price.isEmpty() || "0.0".equals(price)) {
            return;
        }
        try {
            BigDecimal subtotal = new BigDecimal(price)
                    .multiply(new BigDecimal(gbOrder.getGbDoWeight()))
                    .setScale(1, BigDecimal.ROUND_HALF_UP);
            gbOrder.setGbDoSubtotal(subtotal.toPlainString());
        } catch (NumberFormatException ignored) {
        }
    }

    private void syncGbPurchaseGoodsWeight(GbDepartmentOrdersEntity gbOrder, boolean markPurchased) {
        Integer purchaseGoodsId = gbOrder.getGbDoPurchaseGoodsId();
        if (purchaseGoodsId == null || purchaseGoodsId <= 0) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("purGoodsId", purchaseGoodsId);
        List<GbDepartmentOrdersEntity> gbOrders = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        BigDecimal purWeight = BigDecimal.ZERO;
        if (gbOrders != null) {
            for (GbDepartmentOrdersEntity order : gbOrders) {
                if (order != null && order.getGbDoWeight() != null) {
                    purWeight = purWeight.add(new BigDecimal(order.getGbDoWeight()));
                }
            }
        }
        GbDistributerPurchaseGoodsEntity purchaseGoods =
                gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
        if (purchaseGoods == null) {
            return;
        }
        purchaseGoods.setGbDpgBuyQuantity(purWeight.toPlainString());
        if (StringUtils.isNotBlank(purchaseGoods.getGbDpgBuyPrice())) {
            BigDecimal buySubtotal = new BigDecimal(purchaseGoods.getGbDpgBuyPrice().trim())
                    .multiply(purWeight)
                    .setScale(1, BigDecimal.ROUND_HALF_UP);
            purchaseGoods.setGbDpgBuySubtotal(buySubtotal.toPlainString());
        }
        if (markPurchased) {
            purchaseGoods.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
        }
        gbDistributerPurchaseGoodsService.update(purchaseGoods);
    }
}
