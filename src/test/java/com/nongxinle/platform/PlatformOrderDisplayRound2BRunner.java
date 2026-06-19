package com.nongxinle.platform;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.OutGoodsSimpleDTO;
import com.nongxinle.entity.OutOrderSimpleDTO;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.PlatformOrderDisplaySupport;
import com.nongxinle.utils.PlatformOrderPriceSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2b Round 2-B：配送商端平台订单展示验收。
 */
public class PlatformOrderDisplayRound2BRunner {

    private static final int DISTRIBUTER_ID = 160;
    private static final int DEPARTMENT_FATHER_ID = 1436;
    private static final int DIS_GOODS_ID = 31235;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-fulfillment-test.xml");
        try {
            NxDepartmentOrdersService ordersService = ctx.getBean(NxDepartmentOrdersService.class);
            PlatformOrderAssignService assignService = ctx.getBean(PlatformOrderAssignService.class);
            NxDepartmentOrdersDao ordersDao = ctx.getBean(NxDepartmentOrdersDao.class);
            DataSource dataSource = ctx.getBean(DataSource.class);

            Integer assignOrderId = testAssignReferencePrice(assignService, ordersDao);
            testActualPriceChange(ordersDao, assignOrderId);
            testCustomerListSort(ordersService);
            testPrepareOutDepCataSort(ordersService);
        testOutGoodsAggregation(ordersService);
        testAssignedFulfillmentStatus(ordersService);
        testReadyFulfillmentStatusViaSql(dataSource);
        testPendingNotPlatform(ordersService, dataSource);
            testLegacyOwnOrder(ordersService, dataSource);

            System.out.println("=== Phase 2b Java Round 2-B ALL PASSED ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
        }
    }

    private static Integer testAssignReferencePrice(PlatformOrderAssignService assignService, NxDepartmentOrdersDao ordersDao) {
        Integer orderId = submitOnly(assignService);
        assign(assignService, orderId);
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        assertTrue("assign expectPrice", order.getNxDoExpectPrice() != null && !order.getNxDoExpectPrice().trim().isEmpty());
        assertEq("assign initial actual=expect", order.getNxDoExpectPrice(), order.getNxDoPrice());
        assertEq("assign initial priceDifferent", "0", normalizeZero(order.getNxDoPriceDifferent()));
        System.out.println("[assign reference price] OK orderId=" + orderId
                + " expect=" + order.getNxDoExpectPrice());
        return orderId;
    }

    private static void testActualPriceChange(NxDepartmentOrdersDao ordersDao, Integer orderId) {
        NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
        String expectPrice = order.getNxDoExpectPrice();
        assertTrue("assigned order has expectPrice", expectPrice != null && !expectPrice.trim().isEmpty());

        String newActual = new BigDecimal(expectPrice.trim()).add(new BigDecimal("0.3")).toPlainString();
        order.setNxDoPrice(newActual);
        PlatformOrderPriceSupport.recalculateSubtotalFromActualPrice(order);
        PlatformOrderPriceSupport.recalculatePriceDifferent(order);
        ordersDao.update(order);

        NxDepartmentOrdersEntity reloaded = ordersDao.queryObject(orderId);
        assertEq("expectPrice unchanged after actual change", expectPrice, reloaded.getNxDoExpectPrice());
        assertEq("actualPrice updated", newActual, reloaded.getNxDoPrice());
        assertEq("priceDifferent updated", "0.3", normalizeZero(reloaded.getNxDoPriceDifferent()));
        assertTrue("subtotal recalculated", reloaded.getNxDoSubtotal() != null);
        System.out.println("[actual price change] OK orderId=" + orderId);
    }

    private static Integer submitOnly(PlatformOrderAssignService assignService) {
        PlatformSubmitLineRequest submitReq = new PlatformSubmitLineRequest();
        submitReq.setMarketId(MARKET_ID);
        submitReq.setDepartmentId(DEPARTMENT_FATHER_ID);
        submitReq.setNxGoodsId(NX_GOODS_ID);
        submitReq.setQuantity("2");
        submitReq.setStandard("斤");
        submitReq.setOrderUserId(OPERATOR_ID);
        return assignService.submitLine(submitReq).getOrderId();
    }

    private static void assign(PlatformOrderAssignService assignService, Integer orderId) {
        PlatformAssignRequest req = new PlatformAssignRequest();
        req.setMarketId(MARKET_ID);
        req.setOrderId(orderId);
        req.setDisGoodsId(DIS_GOODS_ID);
        req.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
        req.setReasonCode("OTHER");
        req.setReasonNote("Round2B price test");
        req.setOperatorId(OPERATOR_ID);
        assignService.assign(req);
    }

    private static String normalizeZero(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.trim()).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static final int MARKET_ID = 1;
    private static final int NX_GOODS_ID = 100470;
    private static final int OPERATOR_ID = 1;

    private static void testCustomerListSort(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("isSelfOrder", -1);

        List<NxDepartmentEntity> deps = ordersService.queryOrderDepartmentList(map);
        assertTrue("customer list not empty", deps != null && !deps.isEmpty());

        boolean seenOwn = false;
        for (NxDepartmentEntity dep : deps) {
            if (dep.getIsPlatformCustomer() != null && dep.getIsPlatformCustomer() == 1) {
                assertTrue("platform customer has label", PlatformOrderDisplaySupport.PLATFORM_CUSTOMER_LABEL.equals(dep.getPlatformLabel()));
                assertEq("platform customer orderSource", PlatformOrderDisplaySupport.ORDER_SOURCE_PLATFORM, dep.getOrderSource());
                if (seenOwn) {
                    throw new AssertionError("platform customer must appear before own customers");
                }
            } else {
                seenOwn = true;
                assertEq("own customer orderSource", PlatformOrderDisplaySupport.ORDER_SOURCE_OWN, dep.getOrderSource());
            }
        }

        List<Integer> platformDepIds = ordersService.queryPlatformCustomerDepFatherIds(map);
        assertTrue("platform dep ids contains test dept", platformDepIds.contains(DEPARTMENT_FATHER_ID));
        System.out.println("[customer list sort] OK deps=" + deps.size() + " platformDeps=" + platformDepIds.size());
    }

    private static void testPrepareOutDepCataSort(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuOrderStatus", -2);
        map.put("orderGoodsType", 0);
        map.put("isSelfOrder", -1);

        List<NxDepartmentEntity> deps = ordersService.queryPureOrderNxDepartmentSimple(map);
        if (deps == null || deps.isEmpty()) {
            // 本地库若无 purType=0 且 purchase_status<4 的部门，跳过（客户列表排序已在上一项覆盖）
            System.out.println("[prepare out dep sort] SKIP no matching prepare-out departments");
            return;
        }

        boolean seenOwn = false;
        for (NxDepartmentEntity dep : deps) {
            if (dep.getIsPlatformCustomer() != null && dep.getIsPlatformCustomer() == 1) {
                if (seenOwn) {
                    throw new AssertionError("prepare out: platform customer must appear before own customers");
                }
            } else {
                seenOwn = true;
            }
        }
        System.out.println("[prepare out dep sort] OK deps=" + deps.size());
    }

    private static Map<String, Object> outGoodsQueryMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);
        map.put("isSelfOrder", -1);
        map.put("offset", 0);
        map.put("limit", 500);
        return map;
    }

    private static void testOutGoodsAggregation(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = outGoodsQueryMap();

        List<OutGoodsSimpleDTO> goodsList = ordersService.queryOutGoodsWithOrdersUltraSimple(map);
        assertTrue("out goods list not empty", goodsList != null && !goodsList.isEmpty());

        OutGoodsSimpleDTO target = null;
        for (OutGoodsSimpleDTO goods : goodsList) {
            if (DIS_GOODS_ID == goods.getNxDistributerGoodsId()) {
                target = goods;
                break;
            }
        }
        assertTrue("target goods card exists", target != null);
        List<OutOrderSimpleDTO> orders = target.getNxDepartmentOrdersEntities();
        assertTrue("target goods has order lines", orders != null && !orders.isEmpty());

        boolean seenOwnLine = false;
        boolean hasPlatform = false;
        boolean hasOwn = false;
        double platformQty = 0;
        double ownQty = 0;
        double totalQty = 0;

        for (OutOrderSimpleDTO order : orders) {
            double qty = parseQty(order.getNxDoQuantity());
            totalQty += qty;
            if (order.getIsPlatformOrder() != null && order.getIsPlatformOrder() == 1) {
                hasPlatform = true;
                platformQty += qty;
                assertEq("platform line label", PlatformOrderDisplaySupport.PLATFORM_LABEL, order.getPlatformLabel());
                assertEq("platform line priceEditable", Integer.valueOf(1), order.getPriceEditable());
                assertEq("platform line orderSource", PlatformOrderDisplaySupport.ORDER_SOURCE_PLATFORM, order.getOrderSource());
                if (order.getNxDoExpectPrice() != null) {
                    assertTrue("platform expectPrice present", !order.getNxDoExpectPrice().trim().isEmpty());
                }
                if (seenOwnLine) {
                    throw new AssertionError("platform order line must appear before own lines");
                }
            } else {
                hasOwn = true;
                ownQty += qty;
                seenOwnLine = true;
                assertEq("own line isPlatformOrder", Integer.valueOf(0), order.getIsPlatformOrder());
            }
        }

        assertTrue("goods card still single card for disGoodsId", target.getNxDistributerGoodsId() == DIS_GOODS_ID);
        double displayPlatform = parseQty(target.getPlatformQuantity());
        double displayOwn = parseQty(target.getOwnQuantity());
        assertEq("display platform+own = line total", formatQty(totalQty), formatQty(displayPlatform + displayOwn));
        System.out.println("[out goods aggregation] OK disGoodsId=" + DIS_GOODS_ID
                + " lines=" + orders.size() + " platformQty=" + target.getPlatformQuantity()
                + " ownQty=" + target.getOwnQuantity() + " hasPlatform=" + hasPlatform + " hasOwn=" + hasOwn);
    }

    private static void testAssignedFulfillmentStatus(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = outGoodsQueryMap();
        OutOrderSimpleDTO platformLine = null;
        for (OutGoodsSimpleDTO goods : ordersService.queryOutGoodsWithOrdersUltraSimple(map)) {
            if (goods.getNxDepartmentOrdersEntities() == null) {
                continue;
            }
            for (OutOrderSimpleDTO order : goods.getNxDepartmentOrdersEntities()) {
                if (order.getIsPlatformOrder() != null && order.getIsPlatformOrder() == 1
                        && order.getNxDoPurchaseStatus() != null && order.getNxDoPurchaseStatus() < 4) {
                    platformLine = order;
                    break;
                }
            }
            if (platformLine != null) {
                break;
            }
        }
        if (platformLine == null) {
            System.out.println("[ASSIGNED fulfillment on out list] SKIP no in-progress platform line");
            return;
        }
        assertEq("in-progress platform fulfillment", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED,
                platformLine.getPlatformFulfillmentStatus());
        System.out.println("[ASSIGNED fulfillment on out list] OK orderId=" + platformLine.getNxDepartmentOrdersId());
    }

    private static void testReadyFulfillmentStatusViaSql(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "select pof.nx_pof_fulfillment_status, "
                             + "case when poa.nx_poa_assign_status = 'ASSIGNED' then 1 else 0 end as is_platform_order "
                             + "from nx_department_orders dor "
                             + "inner join nx_platform_order_assign poa on poa.nx_poa_order_id = dor.nx_department_orders_id "
                             + "and poa.nx_poa_assign_mode = 'PLATFORM' and poa.nx_poa_assign_status = 'ASSIGNED' "
                             + "inner join nx_platform_order_fulfillment pof on pof.nx_pof_order_id = dor.nx_department_orders_id "
                             + "where dor.nx_DO_distributer_id = ? and pof.nx_pof_fulfillment_status = 'READY_FOR_PICKUP' "
                             + "limit 1")) {
            ps.setInt(1, DISTRIBUTER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[READY fulfillment SQL] SKIP no READY order");
                    return;
                }
                assertEq("READY fulfillment SQL", PlatformConstants.FULFILLMENT_STATUS_READY_FOR_PICKUP,
                        rs.getString(1));
                assertEq("READY still platform flag", 1, rs.getInt(2));
                System.out.println("[READY fulfillment SQL] OK");
            }
        }
    }

    private static void testPendingNotPlatform(NxDepartmentOrdersService ordersService, DataSource dataSource) throws Exception {
        Integer pendingOrderId = queryOneOrderId(dataSource,
                "select poa.nx_poa_order_id from nx_platform_order_assign poa "
                        + "where poa.nx_poa_assign_mode = 'PLATFORM' and poa.nx_poa_assign_status = 'PENDING' limit 1");
        if (pendingOrderId == null) {
            System.out.println("[PENDING not platform display] SKIP no PENDING order");
            return;
        }

        Map<String, Object> map = outGoodsQueryMap();

        OutOrderSimpleDTO line = findOrderLine(ordersService.queryOutGoodsWithOrdersUltraSimple(map), pendingOrderId);
        assertTrue("PENDING order should not appear in distributor out list", line == null);
        System.out.println("[PENDING not platform display] OK orderId=" + pendingOrderId);
    }

    private static void testLegacyOwnOrder(NxDepartmentOrdersService ordersService, DataSource dataSource) throws Exception {
        Integer ownOrderId = queryOneOrderId(dataSource,
                "select dor.nx_department_orders_id from nx_department_orders dor "
                        + "left join nx_platform_order_assign poa on poa.nx_poa_order_id = dor.nx_department_orders_id "
                        + "and poa.nx_poa_assign_mode = 'PLATFORM' and poa.nx_poa_assign_status = 'ASSIGNED' "
                        + "where dor.nx_DO_distributer_id = ? and poa.nx_poa_id is null "
                        + "and dor.nx_DO_purchase_status < 4 and dor.nx_DO_purchase_goods_id = -1 limit 1");
        if (ownOrderId == null) {
            System.out.println("[legacy own order] SKIP no own order");
            return;
        }

        Map<String, Object> map = outGoodsQueryMap();

        OutOrderSimpleDTO line = findOrderLine(ordersService.queryOutGoodsWithOrdersUltraSimple(map), ownOrderId);
        assertTrue("legacy own order line found", line != null);
        assertEq("legacy own isPlatformOrder", Integer.valueOf(0), line.getIsPlatformOrder());
        assertEq("legacy own orderSource", PlatformOrderDisplaySupport.ORDER_SOURCE_OWN, line.getOrderSource());
        System.out.println("[legacy own order] OK orderId=" + ownOrderId);
    }

    private static OutOrderSimpleDTO findOrderLine(List<OutGoodsSimpleDTO> goodsList, Integer orderId) {
        if (goodsList == null) {
            return null;
        }
        for (OutGoodsSimpleDTO goods : goodsList) {
            if (goods.getNxDepartmentOrdersEntities() == null) {
                continue;
            }
            for (OutOrderSimpleDTO order : goods.getNxDepartmentOrdersEntities()) {
                if (orderId.equals(order.getNxDepartmentOrdersId())) {
                    return order;
                }
            }
        }
        return null;
    }

    private static Integer queryOneOrderId(DataSource dataSource, String sql) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sql.contains("dor.nx_DO_distributer_id = ?")) {
                ps.setInt(1, DISTRIBUTER_ID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    private static double parseQty(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(quantity.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatQty(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.valueOf(value);
    }

    private static void assertEq(String label, Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
    }

    private static void assertTrue(String label, boolean condition) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
