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
 * Phase 2b Round 2-A.1：所有出库完成入口统一 READY hook 验收。
 */
public class PlatformOrderFulfillmentRound2A1Runner {

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

            int baseline = fulfillmentDao.countAll();

            // 1. disOutOrdersFinish
            Integer o1 = createAndAssign(assignService);
            simulateDisOutOrdersFinish(fulfillmentService, ordersDao, o1);
            assertReady(fulfillmentService, o1, 1);
            System.out.println("[path disOutOrdersFinish] OK orderId=" + o1);

            // 2. giveOrderWeightForStockPrintAndFinish
            Integer o2 = createAndAssign(assignService);
            simulateGiveOrderWeightForStockPrintAndFinish(fulfillmentService, ordersDao, o2, "2.5");
            assertReady(fulfillmentService, o2, 1);
            System.out.println("[path giveOrderWeightForStockPrintAndFinish] OK orderId=" + o2);

            // 3. disOutOrdersWithWeightFinish
            Integer o3 = createAndAssign(assignService);
            simulateDisOutOrdersWithWeightFinish(fulfillmentService, ordersDao, o3, "1.8");
            assertReady(fulfillmentService, o3, 1);
            System.out.println("[path disOutOrdersWithWeightFinish] OK orderId=" + o3);

            // 4. pickerGiveOrderWeight
            Integer o4 = createAndAssign(assignService);
            simulatePickerGiveOrderWeight(fulfillmentService, ordersDao, o4, "3");
            assertReady(fulfillmentService, o4, 1);
            System.out.println("[path pickerGiveOrderWeight] OK orderId=" + o4);

            // 5. giveOrderWeightListForStockAndFinish（批量路径代表）
            Integer o5 = createAndAssign(assignService);
            simulateBatchStockAndFinish(fulfillmentService, ordersDao, o5, "2");
            assertReady(fulfillmentService, o5, 1);
            System.out.println("[path giveOrderWeightListForStockAndFinish] OK orderId=" + o5);

            // 6. 重复出库幂等
            simulateDisOutOrdersFinish(fulfillmentService, ordersDao, o1);
            assertReady(fulfillmentService, o1, 1);
            System.out.println("[idempotent] OK orderId=" + o1);

            // 7. 有效 buying price
            Integer o6 = createAndAssign(assignService);
            setBuyingPriceOne(dataSource, DIS_GOODS_ID, "2.5");
            try {
                simulateDisOutOrdersFinish(fulfillmentService, ordersDao, o6);
                assertReady(fulfillmentService, o6, 0);
                assertEq("nxDoCostPrice", "2.5", ordersDao.queryObject(o6).getNxDoCostPrice());
                System.out.println("[valid buying cost] OK orderId=" + o6);
            } finally {
                setBuyingPriceOne(dataSource, DIS_GOODS_ID, "0.1");
            }

            // 8. PENDING 不变 READY
            Integer pendingId = submitOnly(assignService);
            NxDepartmentOrdersEntity pending = ordersDao.queryObject(pendingId);
            finishOutboundOrder(fulfillmentService, ordersDao, pending);
            assertTrue("pending no fulfillment", fulfillmentService.queryByOrderId(pendingId) == null);
            System.out.println("[PENDING] OK orderId=" + pendingId);

            // 9. 自有单不创建 fulfillment
            Integer legacyId = queryOneNonPlatformOrderId(dataSource);
            NxDepartmentOrdersEntity legacy = ordersDao.queryObject(legacyId);
            finishOutboundOrder(fulfillmentService, ordersDao, legacy);
            assertTrue("legacy no fulfillment", fulfillmentService.queryByOrderId(legacyId) == null);
            System.out.println("[legacy] OK orderId=" + legacyId);

            assertEq("fulfillment count", baseline + 6, fulfillmentDao.countAll());
            System.out.println("=== Phase 2b Java Round 2-A.1 ALL PASSED ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
        }
    }

    private static void simulateDisOutOrdersFinish(
            PlatformOrderFulfillmentService fulfillmentService, NxDepartmentOrdersDao ordersDao, Integer orderId) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(order.getNxDoQuantity());
        applySalesSubtotal(order);
        finishOutboundOrder(fulfillmentService, ordersDao, order);
    }

    private static void simulateGiveOrderWeightForStockPrintAndFinish(
            PlatformOrderFulfillmentService fulfillmentService, NxDepartmentOrdersDao ordersDao,
            Integer orderId, String weight) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(weight);
        applySalesSubtotal(order);
        finishOutboundOrder(fulfillmentService, ordersDao, order);
    }

    private static void simulateDisOutOrdersWithWeightFinish(
            PlatformOrderFulfillmentService fulfillmentService, NxDepartmentOrdersDao ordersDao,
            Integer orderId, String weight) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(weight);
        applySalesSubtotal(order);
        finishOutboundOrder(fulfillmentService, ordersDao, order);
    }

    private static void simulatePickerGiveOrderWeight(
            PlatformOrderFulfillmentService fulfillmentService, NxDepartmentOrdersDao ordersDao,
            Integer orderId, String weight) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(weight);
        applySalesSubtotal(order);
        finishOutboundOrder(fulfillmentService, ordersDao, order);
    }

    private static void simulateBatchStockAndFinish(
            PlatformOrderFulfillmentService fulfillmentService, NxDepartmentOrdersDao ordersDao,
            Integer orderId, String weight) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        order.setNxDoWeight(weight);
        applySalesSubtotal(order);
        finishOutboundOrder(fulfillmentService, ordersDao, order);
    }

    /** 与 Controller.finishOutboundOrder 等价 */
    private static void finishOutboundOrder(
            PlatformOrderFulfillmentService fulfillmentService,
            NxDepartmentOrdersDao ordersDao,
            NxDepartmentOrdersEntity order) {
        fulfillmentService.tryResolveOutboundCost(order);
        fulfillmentService.applyOutboundCostSubtotalIfValid(order);
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        ordersDao.update(order);
        fulfillmentService.syncReadyForPickupAfterOutboundFinish(order, OPERATOR_ID);
    }

    private static void applySalesSubtotal(NxDepartmentOrdersEntity order) {
        if (order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {
            BigDecimal weightB = new BigDecimal(order.getNxDoWeight());
            BigDecimal priceB = new BigDecimal(order.getNxDoPrice());
            order.setNxDoSubtotal(priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            order.setNxDoStatus(getNxOrderStatusHasFinished());
        }
    }

    private static void assertReady(
            PlatformOrderFulfillmentService fulfillmentService, Integer orderId, int expectedCostMissing) {
        NxPlatformOrderFulfillmentEntity pof = fulfillmentService.queryByOrderId(orderId);
        assertEq("fulfillment status orderId=" + orderId, PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP,
                pof.getNxPofFulfillmentStatus());
        assertEq("costMissing orderId=" + orderId, Integer.valueOf(expectedCostMissing), pof.getNxPofCostMissing());
        assertTrue("ready_at orderId=" + orderId, pof.getNxPofReadyForPickupAt() != null);
    }

    private static Integer createAndAssign(PlatformOrderAssignService assignService) {
        Integer orderId = submitOnly(assignService);
        PlatformAssignRequest req = new PlatformAssignRequest();
        req.setMarketId(MARKET_ID);
        req.setOrderId(orderId);
        req.setDisGoodsId(DIS_GOODS_ID);
        req.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
        req.setReasonCode("OTHER");
        req.setReasonNote("Round2A1 test");
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
