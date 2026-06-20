package com.nongxinle.platform;

import com.nongxinle.controller.GbDepartmentOrdersController;
import com.nongxinle.dto.platform.GbPlatformOrderBridgeResult;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.service.GbPlatformOrderBridgeService;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.R;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * P1.1 真实入口联调：直接调用 Controller.saveOrdersGbJjAndSaveDepGoodsSx（与 HTTP POST 同方法体），
 * 请求体字段与 jingcaiMarket nxDistributerGoods._saveOrder → saveOrdersGbJjAndSaveDepGoodsSx 一致。
 */
public class GbPlatformOrderBridgeP11Runner {

    private static final int GB_DIS_GOODS_ID = 157;
    private static final int NX_DIS_GOODS_ID = 25334;
    private static final int NX_DIS_ID = 56;
    private static final int NX_GOODS_ID = 101507;
    private static final int NX_GOODS_FATHER_ID = 10231;
    private static final int GB_DIS_ID = 12;
    private static final int DEP_ID = 58;
    private static final int DEP_FATHER_ID = 57;
    private static final int TO_DEP_ID = 57;
    private static final int ORDER_USER_ID = 9;

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-gb-p11-test.xml");
        try {
            GbDepartmentOrdersController ordersController = ctx.getBean(GbDepartmentOrdersController.class);
            GbPlatformOrderBridgeService bridgeService = ctx.getBean(GbPlatformOrderBridgeService.class);
            DataSource dataSource = ctx.getBean("dataSource", DataSource.class);

            int assignBefore = count(dataSource, "SELECT COUNT(*) FROM nx_platform_order_assign");
            int fulfillmentBefore = count(dataSource, "SELECT COUNT(*) FROM nx_platform_order_fulfillment");
            System.out.println("[baseline] assign=" + assignBefore + " fulfillment=" + fulfillmentBefore);

            GbDepartmentOrdersEntity req = buildMiniappDepGoodsSxRequest();
            System.out.println("[invoke] GbDepartmentOrdersController.saveOrdersGbJjAndSaveDepGoodsSx");
            System.out.println("[request] gbDoDisGoodsId=" + req.getGbDoDisGoodsId()
                    + " gbDoNxDistributerGoodsId=" + req.getGbDoNxDistributerGoodsId()
                    + " qty=" + req.getGbDoQuantity() + " standard=" + req.getGbDoStandard());

            R response = ordersController.saveOrdersGbJjAndSaveDepGoodsSx(req);
            assertEq("controller code", 0, response.get("code"));
            NxDepartmentOrdersEntity nxOrder = (NxDepartmentOrdersEntity) response.get("data");
            assertTrue("nx order returned", nxOrder != null);
            assertTrue("nxDepartmentOrdersId", nxOrder.getNxDepartmentOrdersId() != null);

            Integer nxOrderId = nxOrder.getNxDepartmentOrdersId();
            Integer gbOrderId = nxOrder.getNxDoGbDepartmentOrderId();
            assertTrue("gbDepartmentOrderId from nx", gbOrderId != null);

            System.out.println("[response] nxDepartmentOrderId=" + nxOrderId + " gbDepartmentOrderId=" + gbOrderId);

            printQuery(dataSource,
                    "SELECT gb.gb_department_orders_id, gb.gb_DO_nx_department_order_id, "
                            + "nx.nx_department_orders_id, nx.nx_DO_gb_department_order_id, "
                            + "nx.nx_DO_distributer_id, nx.nx_DO_dis_goods_id, nx.nx_DO_nx_goods_id, nx.nx_DO_price "
                            + "FROM gb_department_orders gb "
                            + "LEFT JOIN nx_department_orders nx ON nx.nx_department_orders_id = gb.gb_DO_nx_department_order_id "
                            + "WHERE gb.gb_department_orders_id = " + gbOrderId,
                    "GB↔NX link");

            assertEq("bidirectional gb→nx", nxOrderId, queryInt(dataSource,
                    "SELECT gb_DO_nx_department_order_id FROM gb_department_orders WHERE gb_department_orders_id = "
                            + gbOrderId));
            assertEq("bidirectional nx→gb", gbOrderId, queryInt(dataSource,
                    "SELECT nx_DO_gb_department_order_id FROM nx_department_orders WHERE nx_department_orders_id = "
                            + nxOrderId));

            printQuery(dataSource,
                    "SELECT poa.nx_poa_id, poa.nx_poa_order_id, poa.nx_poa_assign_status, poa.nx_poa_assign_mode, "
                            + "poa.nx_poa_assign_source, poa.nx_poa_source_type, poa.nx_poa_gb_department_id, "
                            + "poa.nx_poa_gb_department_father_id, poa.nx_poa_gb_department_order_id, "
                            + "poa.nx_poa_assigned_distributer_id, poa.nx_poa_assigned_dis_goods_id, poa.nx_poa_assigned_price "
                            + "FROM nx_platform_order_assign poa "
                            + "WHERE poa.nx_poa_gb_department_order_id = " + gbOrderId
                            + " OR poa.nx_poa_order_id = " + nxOrderId,
                    "platform assign");

            Integer platformAssignId = queryInt(dataSource,
                    "SELECT nx_poa_id FROM nx_platform_order_assign WHERE nx_poa_order_id = " + nxOrderId);
            assertTrue("platform assign created", platformAssignId != null);
            assertEq("assignStatus", PlatformConstants.ASSIGN_STATUS_ASSIGNED, queryString(dataSource,
                    "SELECT nx_poa_assign_status FROM nx_platform_order_assign WHERE nx_poa_id = " + platformAssignId));
            assertEq("assignMode", PlatformConstants.ASSIGN_MODE_PLATFORM, queryString(dataSource,
                    "SELECT nx_poa_assign_mode FROM nx_platform_order_assign WHERE nx_poa_id = " + platformAssignId));
            assertEq("assignSource", PlatformConstants.ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER, queryString(dataSource,
                    "SELECT nx_poa_assign_source FROM nx_platform_order_assign WHERE nx_poa_id = " + platformAssignId));
            assertEq("sourceType", PlatformConstants.SOURCE_TYPE_GB, queryString(dataSource,
                    "SELECT nx_poa_source_type FROM nx_platform_order_assign WHERE nx_poa_id = " + platformAssignId));

            printQuery(dataSource,
                    "SELECT pof.nx_pof_id, pof.nx_pof_order_id, pof.nx_pof_platform_assign_id, "
                            + "pof.nx_pof_fulfillment_status, pof.nx_pof_distributer_id, pof.nx_pof_dis_goods_id "
                            + "FROM nx_platform_order_fulfillment pof WHERE pof.nx_pof_order_id = " + nxOrderId,
                    "fulfillment");

            Integer fulfillmentId = queryInt(dataSource,
                    "SELECT nx_pof_id FROM nx_platform_order_fulfillment WHERE nx_pof_order_id = " + nxOrderId);
            assertTrue("fulfillment created", fulfillmentId != null);
            assertEq("fulfillmentStatus", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, queryString(dataSource,
                    "SELECT nx_pof_fulfillment_status FROM nx_platform_order_fulfillment WHERE nx_pof_id = "
                            + fulfillmentId));

            int pendingHit = count(dataSource,
                    "SELECT COUNT(*) FROM nx_platform_order_assign WHERE nx_poa_gb_department_order_id = "
                            + gbOrderId + " AND nx_poa_assign_status = 'PENDING'");
            assertEq("not in pending", 0, pendingHit);

            int defaultChanged = count(dataSource,
                    "SELECT COUNT(*) FROM nx_department_nx_goods_default WHERE nx_dngd_last_order_id = "
                            + nxOrderId);
            assertEq("default unchanged", 0, defaultChanged);

            int assignAfter = count(dataSource, "SELECT COUNT(*) FROM nx_platform_order_assign");
            int fulfillmentAfter = count(dataSource, "SELECT COUNT(*) FROM nx_platform_order_fulfillment");
            assertEq("assign +1", assignBefore + 1, assignAfter);
            assertEq("fulfillment +1", fulfillmentBefore + 1, fulfillmentAfter);

            GbDepartmentOrdersEntity gbOrder = loadGbOrder(dataSource, gbOrderId);
            NxDepartmentOrdersEntity nxReload = loadNxOrder(dataSource, nxOrderId);

            GbPlatformOrderBridgeResult idem1 = bridgeService.onNxOrderCreatedFromGb(gbOrder, nxReload);
            GbPlatformOrderBridgeResult idem2 = bridgeService.onNxOrderCreatedFromGb(gbOrder, nxReload);
            assertEq("bridge idempotent 1", true, idem1.isIdempotent());
            assertEq("bridge idempotent 2", true, idem2.isIdempotent());
            assertEq("same platformAssignId", platformAssignId, idem1.getPlatformAssignId());
            assertEq("same fulfillmentId", fulfillmentId, idem1.getFulfillmentId());
            assertEq("assign count unchanged after idem", assignAfter, count(dataSource,
                    "SELECT COUNT(*) FROM nx_platform_order_assign"));
            assertEq("fulfillment count unchanged after idem", fulfillmentAfter, count(dataSource,
                    "SELECT COUNT(*) FROM nx_platform_order_fulfillment"));

            System.out.println("[PASS] P1.1 saveOrdersGbJjAndSaveDepGoodsSx real entry OK");
            System.out.println("[RESULT] gbDepartmentOrderId=" + gbOrderId
                    + " nxDepartmentOrderId=" + nxOrderId
                    + " platformAssignId=" + platformAssignId
                    + " fulfillmentId=" + fulfillmentId);
            System.out.println("[NOTE] saveOrdersGbJjAndSaveGoodsSx 需 itemDis=null 首次建 GB 商品场景，"
                    + "会 postDgnGbGoods 写入新 gb_distributer_goods；本地未跑以免污染商品主数据。");
        } finally {
            ctx.close();
        }
    }

    /** 与 jingcaiMarket nxDistributerGoods._saveOrder 中 depGoods=null、itemDis!=null 分支一致 */
    private static GbDepartmentOrdersEntity buildMiniappDepGoodsSxRequest() {
        GbDepartmentOrdersEntity dg = new GbDepartmentOrdersEntity();
        dg.setGbDoOrderUserId(ORDER_USER_ID);
        dg.setGbDoDepDisGoodsId(-1);
        dg.setGbDoDisGoodsId(GB_DIS_GOODS_ID);
        dg.setGbDoDisGoodsFatherId(null);
        dg.setGbDoDepartmentId(DEP_ID);
        dg.setGbDoToDepartmentId(TO_DEP_ID);
        dg.setGbDoDistributerId(GB_DIS_ID);
        dg.setGbDoDepartmentFatherId(DEP_FATHER_ID);
        dg.setGbDoQuantity("5");
        dg.setGbDoPrice("0.1");
        dg.setGbDoWeight("5");
        dg.setGbDoSubtotal("0.5");
        dg.setGbDoStandard("斤");
        dg.setGbDoRemark("P1.1 integration test");
        dg.setGbDoIsAgent(0);
        dg.setGbDoArriveDate("2026-06-20");
        dg.setGbDoArriveWeeksYear(25);
        dg.setGbDoArriveOnlyDate("06-20");
        dg.setGbDoArriveWhatDay("6");
        dg.setGbDoNxGoodsId(NX_GOODS_ID);
        dg.setGbDoNxGoodsFatherId(NX_GOODS_FATHER_ID);
        dg.setGbDoNxDistributerGoodsId(NX_DIS_GOODS_ID);
        dg.setGbDoNxDistributerId(NX_DIS_ID);
        dg.setGbDoGoodsType(1);
        dg.setGbDoOrderType(5);
        dg.setGbDoDsStandardScale("-1");
        dg.setGbDoPrintStandard("斤");
        dg.setGbDoCostPriceLevel(1);
        return dg;
    }

    private static GbDepartmentOrdersEntity loadGbOrder(DataSource ds, int gbOrderId) throws Exception {
        GbDepartmentOrdersEntity e = new GbDepartmentOrdersEntity();
        e.setGbDepartmentOrdersId(gbOrderId);
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT gb_DO_nx_department_order_id FROM gb_department_orders WHERE gb_department_orders_id = ?")) {
            ps.setInt(1, gbOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setGbDoNxDepartmentOrderId(rs.getInt(1));
                }
            }
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT gb_DO_department_id, gb_DO_department_father_id, gb_DO_order_user_id "
                             + "FROM gb_department_orders WHERE gb_department_orders_id = ?")) {
            ps.setInt(1, gbOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setGbDoDepartmentId(rs.getInt(1));
                    e.setGbDoDepartmentFatherId(rs.getInt(2));
                    e.setGbDoOrderUserId(rs.getInt(3));
                }
            }
        }
        return e;
    }

    private static NxDepartmentOrdersEntity loadNxOrder(DataSource ds, int nxOrderId) throws Exception {
        NxDepartmentOrdersEntity e = new NxDepartmentOrdersEntity();
        e.setNxDepartmentOrdersId(nxOrderId);
        String sql = "SELECT nx_DO_gb_department_order_id, nx_DO_distributer_id, nx_DO_dis_goods_id, "
                + "nx_DO_nx_goods_id, nx_DO_price FROM nx_department_orders WHERE nx_department_orders_id = ?";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nxOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setNxDoGbDepartmentOrderId(rs.getInt(1));
                    e.setNxDoDistributerId(rs.getInt(2));
                    e.setNxDoDisGoodsId(rs.getInt(3));
                    e.setNxDoNxGoodsId(rs.getInt(4));
                    e.setNxDoPrice(rs.getString(5));
                }
            }
        }
        return e;
    }

    private static void printQuery(DataSource ds, String sql, String label) throws Exception {
        System.out.println("--- " + label + " ---");
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= cols; i++) {
                headers.add(meta.getColumnLabel(i));
            }
            System.out.println(String.join("\t", headers));
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= cols; i++) {
                    row.add(String.valueOf(rs.getObject(i)));
                }
                System.out.println(String.join("\t", row));
            }
        }
    }

    private static int count(DataSource ds, String sql) throws Exception {
        return queryInt(ds, sql);
    }

    private static Integer queryInt(DataSource ds, String sql) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private static String queryString(DataSource ds, String sql) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }

    private static void assertTrue(String label, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL " + label);
        }
        System.out.println("[OK] " + label);
    }

    private static void assertEq(String label, Object expected, Object actual) {
        if (expected == null && actual == null) {
            System.out.println("[OK] " + label);
            return;
        }
        if (expected != null && expected.equals(actual)) {
            System.out.println("[OK] " + label);
            return;
        }
        throw new AssertionError("FAIL " + label + " expected=" + expected + " actual=" + actual);
    }
}
