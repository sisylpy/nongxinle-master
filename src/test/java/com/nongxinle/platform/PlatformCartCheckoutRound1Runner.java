package com.nongxinle.platform;

import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitItemRequest;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmResponse;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderLinesRequest;
import com.nongxinle.dto.platform.customer.PlatformCartAddWithSupplierResponse;
import com.nongxinle.service.platform.PlatformCartCheckoutService;
import com.nongxinle.service.platform.PlatformCartSubmitService;
import com.nongxinle.service.platform.PlatformCustomerSubmitLineService;
import com.nongxinle.service.platform.PlatformDistributerOrderService;
import com.nongxinle.utils.GbBillPlatformConstants;
import com.nongxinle.utils.PlatformConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.UUID;

/**
 * 购物车 / checkout 第一轮最小验收（design §1.5 + §12 混合单骨架）。
 *
 * <p>运行方式（在 {@code nongxinle-master} 根目录，需 {@code spring-jdbc.xml} 可连本地库）：</p>
 * <pre>
 * mvn -q test-compile exec:java \
 *   -Dexec.mainClass=com.nongxinle.platform.PlatformCartCheckoutRound1Runner
 * </pre>
 * <p>注意：Runner 在 {@code src/test/java}，必须先 {@code test-compile}；不要用仅 {@code compile}。</p>
 */
public class PlatformCartCheckoutRound1Runner {

    private static final int GB_DIS_ID = 12;
    private static final int NX_DIS_ID = 56;
    private static final int DEP_ID = 58;
    private static final int DEP_FATHER_ID = 57;
    private static final int MARKET_ID = 1;

    private static final int GREEN_CABBAGE_NX_DIS_GOODS = 22151;
    private static final int GREEN_CABBAGE_NX_GOODS = 101837;
    private static final int POTATO_NX_GOODS = 100088;

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-gb-p11-test.xml");
        try {
            PlatformCartSubmitService submitService = ctx.getBean(PlatformCartSubmitService.class);
            PlatformCustomerSubmitLineService addWithoutSupplier = ctx.getBean(PlatformCustomerSubmitLineService.class);
            PlatformCartCheckoutService checkoutService = ctx.getBean(PlatformCartCheckoutService.class);
            PlatformDistributerOrderService distributerOrderService =
                    ctx.getBean(PlatformDistributerOrderService.class);
            DataSource dataSource = ctx.getBean("dataSource", DataSource.class);

            // 1) 已选配送商写入购物车（临时阶段）：status=-1，无 bill、无 assign
            PlatformCartAddWithSupplierResponse withSupplier = submitService.submitBySupplier(buildSupplierRequest(
                    item(GREEN_CABBAGE_NX_DIS_GOODS, GREEN_CABBAGE_NX_GOODS, "绿甘蓝", "3", "斤")));
            Integer supplierNxOrderId = withSupplier.getLines().get(0).getNxOrderId();
            Integer supplierGbOrderId = withSupplier.getLines().get(0).getGbOrderId();
            assertEq("supplier nx status=-1", -1, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?",
                    supplierNxOrderId));
            assertEq("supplier gb status=-1", -1, queryInt(dataSource,
                    "SELECT gb_DO_status FROM gb_department_orders WHERE gb_department_orders_id=?",
                    supplierGbOrderId));
            assertNull("supplier gb billId before checkout", queryInteger(dataSource,
                    "SELECT gb_DO_bill_id FROM gb_department_orders WHERE gb_department_orders_id=?",
                    supplierGbOrderId));
            assertEq("supplier no assign", 0, queryInt(dataSource,
                    "SELECT COUNT(*) FROM nx_platform_order_assign WHERE nx_poa_order_id=?", supplierNxOrderId));
            assertEq("supplier invisible to distributer", 0,
                    countDistributerVisibleLines(distributerOrderService, supplierNxOrderId));
            System.out.println("[OK] 已选配送商购物车临时行 nxOrderId=" + supplierNxOrderId);

            // 2) 未选配送商写入购物车（临时阶段）：status=-1，无 assign
            PlatformSubmitLineRequest withoutReq = new PlatformSubmitLineRequest();
            withoutReq.setMarketId(MARKET_ID);
            withoutReq.setDepartmentId(DEP_ID);
            withoutReq.setNxGoodsId(POTATO_NX_GOODS);
            withoutReq.setGoodsName("大土豆");
            withoutReq.setQuantity("1");
            withoutReq.setStandard("个");
            Integer pendingNxOrderId = addWithoutSupplier.submitLine(withoutReq).getOrderId();
            assertEq("pending nx status=-1", -1, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?",
                    pendingNxOrderId));
            assertEq("pending no gb yet", 0, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_orders WHERE gb_DO_nx_department_order_id=?",
                    pendingNxOrderId));
            assertEq("pending no assign", 0, queryInt(dataSource,
                    "SELECT COUNT(*) FROM nx_platform_order_assign WHERE nx_poa_order_id=?", pendingNxOrderId));
            System.out.println("[OK] 未选配送商购物车临时行 nxOrderId=" + pendingNxOrderId);

            // 3) checkoutPreview 分组
            PlatformCheckoutPreviewRequest previewReq = new PlatformCheckoutPreviewRequest();
            previewReq.setMarketId(MARKET_ID);
            previewReq.setGbDepartmentId(DEP_ID);
            previewReq.setOrderIds(Arrays.asList(supplierNxOrderId, pendingNxOrderId));
            PlatformCheckoutPreviewResponse preview = checkoutService.checkoutPreview(previewReq);
            assertEq("preview confirmed count", 1, preview.getConfirmedItemCount());
            assertEq("preview pending price count", 1, preview.getPendingPriceItemCount());
            assertEq("preview knownTotal", "3.90", preview.getKnownTotal());
            assertEq("preview payAmount", "3.90", preview.getPayAmount());
            assertEq("preview confirmed price", GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED,
                    preview.getConfirmedLines().get(0).getPriceConfirmStatus());
            assertEq("preview pending price", GbBillPlatformConstants.PRICE_CONFIRM_PENDING,
                    preview.getPendingPriceLines().get(0).getPriceConfirmStatus());
            System.out.println("[OK] checkoutPreview 分组与金额");

            // 4) checkoutConfirm：bill + 全部挂行 + assign 分支
            PlatformCheckoutConfirmRequest confirmReq = new PlatformCheckoutConfirmRequest();
            confirmReq.setMarketId(MARKET_ID);
            confirmReq.setGbDepartmentId(DEP_ID);
            confirmReq.setGbDepartmentFatherId(DEP_FATHER_ID);
            confirmReq.setGbDistributerId(GB_DIS_ID);
            confirmReq.setDeliveryDate("2026-06-20");
            confirmReq.setCheckoutToken(uuid("round1-checkout"));
            confirmReq.setOrderIds(Arrays.asList(supplierNxOrderId, pendingNxOrderId));
            PlatformCheckoutConfirmResponse confirm = checkoutService.checkoutConfirm(confirmReq);
            assertTrue("confirm billId", confirm.getBillId() != null);
            assertEq("confirm gb line count", 2, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_orders WHERE gb_DO_bill_id=?", confirm.getBillId()));
            assertEq("confirm supplier nx formal", 0, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?",
                    supplierNxOrderId));
            assertEq("confirm pending nx formal", 0, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?",
                    pendingNxOrderId));
            assertEq("supplier gb billId after checkout", confirm.getBillId(), queryInteger(dataSource,
                    "SELECT gb_DO_bill_id FROM gb_department_orders WHERE gb_department_orders_id=?",
                    supplierGbOrderId));
            assertEq("pending gb billId after checkout", confirm.getBillId(), queryInteger(dataSource,
                    "SELECT gb_DO_bill_id FROM gb_department_orders WHERE gb_DO_nx_department_order_id=?",
                    pendingNxOrderId));

            String supplierAssign = queryString(dataSource,
                    "SELECT nx_poa_assign_status FROM nx_platform_order_assign WHERE nx_poa_order_id=?",
                    supplierNxOrderId);
            String pendingAssign = queryString(dataSource,
                    "SELECT nx_poa_assign_status FROM nx_platform_order_assign WHERE nx_poa_order_id=?",
                    pendingNxOrderId);
            assertEq("supplier assign", PlatformConstants.ASSIGN_STATUS_ASSIGNED, supplierAssign);
            assertEq("pending assign", PlatformConstants.ASSIGN_STATUS_PENDING, pendingAssign);
            assertEq("confirm payStatus", GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID, confirm.getPayStatus());
            System.out.println("[OK] checkoutConfirm billId=" + confirm.getBillId()
                    + " supplierAssign=" + supplierAssign + " pendingAssign=" + pendingAssign);

            // 5) 配送商端看不到 status=-1（再建一条 cart 行验证）
            PlatformCartAddWithSupplierResponse cartProbe = submitService.submitBySupplier(buildSupplierRequest(
                    item(GREEN_CABBAGE_NX_DIS_GOODS, GREEN_CABBAGE_NX_GOODS, "绿甘蓝", "1", "斤")));
            Integer probeNxOrderId = cartProbe.getLines().get(0).getNxOrderId();
            assertEq("probe cart status", -1, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?",
                    probeNxOrderId));
            assertNull("probe gb billId before checkout", queryInteger(dataSource,
                    "SELECT gb_DO_bill_id FROM gb_department_orders WHERE gb_DO_nx_department_order_id=?",
                    probeNxOrderId));
            assertEq("probe cart invisible", 0, countDistributerVisibleLines(distributerOrderService, probeNxOrderId));
            assertTrue("confirmed supplier visible after checkout",
                    countDistributerVisibleLines(distributerOrderService, supplierNxOrderId) >= 1);

            System.out.println("[PASS] PlatformCartCheckoutRound1Runner OK");
        } finally {
            ctx.close();
        }
    }

    private static int countDistributerVisibleLines(
            PlatformDistributerOrderService distributerOrderService, Integer nxOrderId) throws Exception {
        PlatformDistributerOrderLinesRequest req = new PlatformDistributerOrderLinesRequest();
        req.setDisId(NX_DIS_ID);
        req.setGbDepFatherId(DEP_FATHER_ID);
        return (int) distributerOrderService.listOrderLines(req).stream()
                .filter(line -> line != null && nxOrderId.equals(line.getNxDepartmentOrdersId()))
                .count();
    }

    private static PlatformCartSubmitRequest buildSupplierRequest(PlatformCartSubmitItemRequest... items) {
        PlatformCartSubmitRequest req = new PlatformCartSubmitRequest();
        req.setMarketId(MARKET_ID);
        req.setGbDepartmentId(DEP_ID);
        req.setGbDepartmentFatherId(DEP_FATHER_ID);
        req.setGbDistributerId(GB_DIS_ID);
        req.setNxDistributerId(NX_DIS_ID);
        req.setDeliveryDate("2026-06-20");
        req.setItems(Arrays.asList(items));
        return req;
    }

    private static PlatformCartSubmitItemRequest item(
            int nxDisGoodsId, Integer nxGoodsId, String name, String qty, String standard) {
        PlatformCartSubmitItemRequest item = new PlatformCartSubmitItemRequest();
        item.setNxDistributerGoodsId(nxDisGoodsId);
        item.setNxGoodsId(nxGoodsId);
        item.setGoodsName(name);
        item.setQuantity(qty);
        item.setStandard(standard);
        return item;
    }

    private static String uuid(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private static Integer queryInteger(DataSource ds, String sql, Object... params) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt(1);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return value;
                }
            }
        }
        return null;
    }

    private static int queryInt(DataSource ds, String sql, Object... params) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -999;
    }

    private static String queryString(DataSource ds, String sql, Object... params) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private static void bind(PreparedStatement ps, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static void assertNull(String label, Object actual) {
        if (actual != null) {
            throw new AssertionError("FAIL " + label + " expected=null actual=" + actual);
        }
        System.out.println("[OK] " + label);
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
