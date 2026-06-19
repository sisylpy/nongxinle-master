package com.nongxinle.platform;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.utils.PlatformConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusFinishOut;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusHasFinished;

/**
 * Phase 2b Round 2-A：出库完成 hook → READY_FOR_PICKUP 本地验收。
 */
public class PlatformOrderFulfillmentRound2ARunner {

    private static final int MARKET_ID = 1;
    private static final int DEPARTMENT_ID = 1436;
    private static final int NX_GOODS_ID = 100470;
    private static final int DIS_GOODS_ID = 31235;
    private static final int OPERATOR_ID = 1;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-fulfillment-test.xml");
        try {
            PlatformOrderAssignService assignService = ctx.getBean(PlatformOrderAssignService.class);
            PlatformOrderFulfillmentService fulfillmentService = ctx.getBean(PlatformOrderFulfillmentService.class);
            NxDepartmentOrdersDao ordersDao = ctx.getBean(NxDepartmentOrdersDao.class);
            NxPlatformOrderFulfillmentDao fulfillmentDao = ctx.getBean(NxPlatformOrderFulfillmentDao.class);
            DataSource dataSource = ctx.getBean(DataSource.class);

            int baselineCount = fulfillmentDao.countAll();

            Integer orderIdMissingCost = createAndAssign(assignService);
            assertEq("assign fulfillment", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED,
                    fulfillmentService.queryByOrderId(orderIdMissingCost).getNxPofFulfillmentStatus());

            simulateOutboundFinish(fulfillmentService, ordersDao, orderIdMissingCost);
            NxPlatformOrderFulfillmentEntity pofMissing = fulfillmentService.queryByOrderId(orderIdMissingCost);
            assertEq("costMissing outbound status", PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP,
                    pofMissing.getNxPofFulfillmentStatus());
            assertTrue("ready_for_pickup_at", pofMissing.getNxPofReadyForPickupAt() != null);
            assertEq("costMissing flag", Integer.valueOf(1), pofMissing.getNxPofCostMissing());

            NxDepartmentOrdersEntity orderMissing = ordersDao.queryObject(orderIdMissingCost);
            assertEq("purchaseStatus", getNxDepOrderBuyStatusFinishOut(), orderMissing.getNxDoPurchaseStatus());
            assertTrue("no fake cost", orderMissing.getNxDoCostPrice() == null
                    || orderMissing.getNxDoCostPrice().trim().isEmpty());
            System.out.println("[case1] costMissing outbound OK orderId=" + orderIdMissingCost);

            simulateOutboundFinish(fulfillmentService, ordersDao, orderIdMissingCost);
            assertEq("idempotent count", baselineCount + 1, fulfillmentDao.countAll());
            assertEq("idempotent status", PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP,
                    fulfillmentService.queryByOrderId(orderIdMissingCost).getNxPofFulfillmentStatus());
            System.out.println("[case2] idempotent outbound OK");

            Integer orderIdWithCost = createAndAssign(assignService);
            setBuyingPriceOne(dataSource, DIS_GOODS_ID, "2.5");
            try {
                simulateOutboundFinish(fulfillmentService, ordersDao, orderIdWithCost);
                NxPlatformOrderFulfillmentEntity pofCost = fulfillmentService.queryByOrderId(orderIdWithCost);
                assertEq("valid cost status", PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP,
                        pofCost.getNxPofFulfillmentStatus());
                assertEq("valid costMissing", Integer.valueOf(0), pofCost.getNxPofCostMissing());
                NxDepartmentOrdersEntity orderCost = ordersDao.queryObject(orderIdWithCost);
                assertEq("nxDoCostPrice", "2.5", orderCost.getNxDoCostPrice());
                assertTrue("nxDoCostSubtotal", orderCost.getNxDoCostSubtotal() != null);
                System.out.println("[case3] valid buying cost OK orderId=" + orderIdWithCost);
            } finally {
                setBuyingPriceOne(dataSource, DIS_GOODS_ID, "0.1");
            }

            Integer pendingOrderId = submitOnly(assignService);
            assertTrue("pending no fulfillment", fulfillmentService.queryByOrderId(pendingOrderId) == null);
            NxDepartmentOrdersEntity pendingOrder = ordersDao.queryObject(pendingOrderId);
            pendingOrder.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            ordersDao.update(pendingOrder);
            fulfillmentService.syncReadyForPickupAfterOutboundFinish(pendingOrder, OPERATOR_ID);
            assertTrue("pending still no fulfillment", fulfillmentService.queryByOrderId(pendingOrderId) == null);
            System.out.println("[case4] PENDING no READY OK orderId=" + pendingOrderId);

            Integer legacyOrderId = queryOneNonPlatformOrderId(dataSource);
            assertTrue("legacy order exists", legacyOrderId != null);
            NxDepartmentOrdersEntity legacyOrder = ordersDao.queryObject(legacyOrderId);
            legacyOrder.setNxDoWeight(legacyOrder.getNxDoQuantity());
            legacyOrder.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            ordersDao.update(legacyOrder);
            fulfillmentService.syncReadyForPickupAfterOutboundFinish(legacyOrder, OPERATOR_ID);
            assertTrue("legacy no fulfillment", fulfillmentService.queryByOrderId(legacyOrderId) == null);
            assertEq("fulfillment count after legacy", baselineCount + 2, fulfillmentDao.countAll());
            System.out.println("[case5] legacy order no fulfillment OK orderId=" + legacyOrderId);

            System.out.println("=== Phase 2b Java Round 2-A ALL PASSED ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
        }
    }

    private static Integer createAndAssign(PlatformOrderAssignService assignService) {
        Integer orderId = submitOnly(assignService);
        PlatformAssignRequest req = new PlatformAssignRequest();
        req.setMarketId(MARKET_ID);
        req.setOrderId(orderId);
        req.setDisGoodsId(DIS_GOODS_ID);
        req.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
        req.setReasonCode("OTHER");
        req.setReasonNote("Round2A test");
        req.setOperatorId(OPERATOR_ID);
        assignService.assign(req);
        return orderId;
    }

    private static Integer submitOnly(PlatformOrderAssignService assignService) {
        PlatformSubmitLineRequest submitReq = new PlatformSubmitLineRequest();
        submitReq.setMarketId(MARKET_ID);
        submitReq.setDepartmentId(DEPARTMENT_ID);
        submitReq.setNxGoodsId(NX_GOODS_ID);
        submitReq.setQuantity("2");
        submitReq.setStandard("斤");
        submitReq.setOrderUserId(OPERATOR_ID);
        return assignService.submitLine(submitReq).getOrderId();
    }

    private static void simulateOutboundFinish(
            PlatformOrderFulfillmentService fulfillmentService,
            NxDepartmentOrdersDao ordersDao,
            Integer orderId) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(order.getNxDoQuantity());
        if (order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {
            BigDecimal weightB = new BigDecimal(order.getNxDoWeight());
            BigDecimal priceB = new BigDecimal(order.getNxDoPrice());
            order.setNxDoSubtotal(priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            order.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        fulfillmentService.tryResolveOutboundCost(order);
        fulfillmentService.applyOutboundCostSubtotalIfValid(order);
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        ordersDao.update(order);
        fulfillmentService.syncReadyForPickupAfterOutboundFinish(order, OPERATOR_ID);
    }

    private static void setBuyingPriceOne(DataSource dataSource, int disGoodsId, String price) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "update nx_distributer_goods set nx_dg_buying_price_one = ? where nx_distributer_goods_id = ?")) {
            ps.setString(1, price);
            ps.setInt(2, disGoodsId);
            ps.executeUpdate();
        }
    }

    private static Integer queryOneNonPlatformOrderId(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "select dor.nx_department_orders_id from nx_department_orders dor "
                             + "left join nx_platform_order_assign poa on poa.nx_poa_order_id = dor.nx_department_orders_id "
                             + "where poa.nx_poa_id is null order by dor.nx_department_orders_id desc limit 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }

    private static void assertEq(String label, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
