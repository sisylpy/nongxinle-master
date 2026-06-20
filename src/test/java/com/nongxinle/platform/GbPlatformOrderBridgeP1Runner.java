package com.nongxinle.platform;

import com.nongxinle.dao.GbDepartmentOrdersDao;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.dto.platform.GbPlatformOrderBridgeResult;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.GbPlatformOrderBridgeService;
import com.nongxinle.utils.PlatformConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * P1 GB→平台 ASSIGNED 本地验收 Runner（localhost only）。
 *
 * 用法：
 *   java ... GbPlatformOrderBridgeP1Runner
 *   java ... GbPlatformOrderBridgeP1Runner &lt;gbDepartmentOrderId&gt; &lt;nxDepartmentOrderId&gt;
 */
public class GbPlatformOrderBridgeP1Runner {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-fulfillment-test.xml");
        try {
            GbPlatformOrderBridgeService bridge = ctx.getBean(GbPlatformOrderBridgeService.class);
            GbDepartmentOrdersDao gbOrdersDao = ctx.getBean(GbDepartmentOrdersDao.class);
            NxDepartmentOrdersDao nxOrdersDao = ctx.getBean(NxDepartmentOrdersDao.class);
            NxPlatformOrderAssignDao assignDao = ctx.getBean(NxPlatformOrderAssignDao.class);
            NxPlatformOrderFulfillmentDao fulfillmentDao = ctx.getBean(NxPlatformOrderFulfillmentDao.class);
            DataSource dataSource = ctx.getBean("dataSource", DataSource.class);

            int assignBaseline = countAssignRows(dataSource);
            int fulfillmentBaseline = fulfillmentDao.countAll();
            System.out.println("[baseline] assign=" + assignBaseline + " fulfillment=" + fulfillmentBaseline);

            int gbOrderId;
            int nxOrderId;
            if (args.length >= 2) {
                gbOrderId = Integer.parseInt(args[0]);
                nxOrderId = Integer.parseInt(args[1]);
            } else {
                int[] pair = findUnbridgedPair(dataSource);
                if (pair == null) {
                    System.out.println("[SKIP] 未找到无 platform assign 的 GB↔NX 订单对；"
                            + "请先通过 saveOrdersGbJjAndSaveDepGoodsSx 下单，或传入 gbOrderId nxOrderId");
                    return;
                }
                gbOrderId = pair[0];
                nxOrderId = pair[1];
            }

            GbDepartmentOrdersEntity gbOrder = gbOrdersDao.queryObject(gbOrderId);
            NxDepartmentOrdersEntity nxOrder = nxOrdersDao.queryObject(nxOrderId);
            assertTrue("gbOrder exists", gbOrder != null);
            assertTrue("nxOrder exists", nxOrder != null);
            assertEq("gb→nx link", nxOrderId, gbOrder.getGbDoNxDepartmentOrderId());
            assertEq("nx→gb link", gbOrderId, nxOrder.getNxDoGbDepartmentOrderId());

            System.out.println("[candidate] gbOrderId=" + gbOrderId + " nxOrderId=" + nxOrderId);

            GbPlatformOrderBridgeResult first = bridge.onNxOrderCreatedFromGb(gbOrder, nxOrder);
            assertTrue("first platformAssignId", first.getPlatformAssignId() != null);
            assertTrue("first fulfillmentId", first.getFulfillmentId() != null);
            assertEq("first not idempotent", false, first.isIdempotent());
            System.out.println("[first bridge] " + first);

            NxPlatformOrderAssignEntity poa = assignDao.queryObject(first.getPlatformAssignId());
            assertEq("assignStatus", PlatformConstants.ASSIGN_STATUS_ASSIGNED, poa.getNxPoaAssignStatus());
            assertEq("assignMode", PlatformConstants.ASSIGN_MODE_PLATFORM, poa.getNxPoaAssignMode());
            assertEq("assignSource", PlatformConstants.ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER, poa.getNxPoaAssignSource());
            assertEq("sourceType", PlatformConstants.SOURCE_TYPE_GB, poa.getNxPoaSourceType());
            assertEq("gbDepartmentOrderId", gbOrderId, poa.getNxPoaGbDepartmentOrderId());
            assertTrue("not PENDING", !PlatformConstants.ASSIGN_STATUS_PENDING.equals(poa.getNxPoaAssignStatus()));

            NxPlatformOrderFulfillmentEntity pof = fulfillmentDao.queryByOrderId(nxOrderId);
            assertEq("fulfillmentStatus", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, pof.getNxPofFulfillmentStatus());
            System.out.println("[assign row] " + poa);
            System.out.println("[fulfillment row] " + pof);

            GbPlatformOrderBridgeResult second = bridge.onNxOrderCreatedFromGb(gbOrder, nxOrder);
            assertEq("second idempotent", true, second.isIdempotent());
            assertEq("same platformAssignId", first.getPlatformAssignId(), second.getPlatformAssignId());
            assertEq("same fulfillmentId", first.getFulfillmentId(), second.getFulfillmentId());
            System.out.println("[second bridge idempotent] " + second);

            int assignAfter = countAssignRows(dataSource);
            int fulfillmentAfter = fulfillmentDao.countAll();
            assertEq("assign count +1", assignBaseline + 1, assignAfter);
            assertEq("fulfillment count +1", fulfillmentBaseline + 1, fulfillmentAfter);

            int pendingGb = countPendingGbSource(dataSource);
            assertEq("GB source not in pending", 0, pendingGb);

            System.out.println("[PASS] P1 bridge local acceptance OK");
            System.out.println("[RESULT] gbDepartmentOrderId=" + gbOrderId
                    + " nxDepartmentOrderId=" + nxOrderId
                    + " platformAssignId=" + first.getPlatformAssignId()
                    + " fulfillmentId=" + first.getFulfillmentId());
        } finally {
            ctx.close();
        }
    }

    private static int[] findUnbridgedPair(DataSource dataSource) throws Exception {
        String sql = "SELECT g.gb_department_orders_id, g.gb_do_nx_department_order_id "
                + "FROM gb_department_orders g "
                + "WHERE g.gb_do_nx_department_order_id IS NOT NULL AND g.gb_do_nx_department_order_id > 0 "
                + "AND NOT EXISTS (SELECT 1 FROM nx_platform_order_assign p WHERE p.nx_poa_order_id = g.gb_do_nx_department_order_id) "
                + "AND NOT EXISTS (SELECT 1 FROM nx_platform_order_assign p WHERE p.nx_poa_gb_department_order_id = g.gb_department_orders_id) "
                + "ORDER BY g.gb_department_orders_id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new int[]{rs.getInt(1), rs.getInt(2)};
        }
    }

    private static int countAssignRows(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM nx_platform_order_assign");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int countPendingGbSource(DataSource dataSource) throws Exception {
        String sql = "SELECT COUNT(*) FROM nx_platform_order_assign "
                + "WHERE nx_poa_assign_status = 'PENDING' AND nx_poa_source_type = 'GB'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
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
