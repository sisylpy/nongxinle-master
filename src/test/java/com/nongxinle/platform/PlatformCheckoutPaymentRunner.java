package com.nongxinle.platform;

import com.nongxinle.dto.platform.customer.PlatformCartSubmitItemRequest;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitRequest;
import com.nongxinle.dto.platform.customer.PlatformCartAddWithSupplierResponse;
import com.nongxinle.dto.platform.customer.PlatformCartLineUpdateRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPayRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentCancelRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest;
import com.nongxinle.service.platform.PlatformCartCheckoutService;
import com.nongxinle.service.platform.PlatformCartSubmitService;
import com.nongxinle.service.platform.PlatformCheckoutPaymentService;
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
 * checkoutPay 支付闭环验收：PENDING 锁定 → cancel/超时释放 → finalize → 幂等。
 *
 * <p>SQL：upgrade_platform_checkout_payment.sql → v2 → v3</p>
 *
 * <pre>
 * mvn -q test-compile exec:java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.mainClass=com.nongxinle.platform.PlatformCheckoutPaymentRunner
 * </pre>
 */
public class PlatformCheckoutPaymentRunner {

    private static final int GB_DIS_ID = 12;
    private static final int NX_DIS_ID = 56;
    private static final int DEP_ID = 58;
    private static final int DEP_FATHER_ID = 57;
    private static final int MARKET_ID = 1;
    private static final int GREEN_CABBAGE_NX_DIS_GOODS = 22151;
    private static final int GREEN_CABBAGE_NX_GOODS = 101837;

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-gb-p11-test.xml");
        try {
            PlatformCartSubmitService submitService = ctx.getBean(PlatformCartSubmitService.class);
            PlatformCartCheckoutService checkoutService = ctx.getBean(PlatformCartCheckoutService.class);
            PlatformCheckoutPaymentService paymentService = ctx.getBean(PlatformCheckoutPaymentService.class);
            DataSource dataSource = ctx.getBean("dataSource", DataSource.class);

            int tableExists = queryInt(dataSource,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema=DATABASE() AND table_name='platform_checkout_payment'");
            if (tableExists != 1) {
                throw new IllegalStateException(
                        "platform_checkout_payment 表不存在。请在当前库执行："
                                + " docs/sql/patches/upgrade_platform_checkout_payment.sql");
            }
            System.out.println("[OK] platform_checkout_payment table");

            PlatformCartAddWithSupplierResponse cart = submitService.submitBySupplier(buildSupplierRequest(
                    item(GREEN_CABBAGE_NX_DIS_GOODS, GREEN_CABBAGE_NX_GOODS, "绿甘蓝", "3", "斤")));
            Integer nxOrderId = cart.getLines().get(0).getNxOrderId();

            String checkoutToken = uuid("pay-runner");
            PlatformCheckoutPayRequest payReq = buildPayRequest(checkoutToken, nxOrderId);
            Integer paymentId = paymentService.createPaymentIntentSnapshot(payReq);

            assertEq("payment PENDING", GbBillPlatformConstants.PAYMENT_STATUS_PENDING, queryString(dataSource,
                    "SELECT pcp_status FROM platform_checkout_payment WHERE pcp_id=?", paymentId));
            assertEq("no bill before finalize", 0, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_bill WHERE gb_db_platform_submit_token=?", checkoutToken));

            PlatformCheckoutPaymentStatusResponse statusPending = paymentService.queryPaymentStatus(statusReq(paymentId));
            assertEq("status PENDING", GbBillPlatformConstants.PAYMENT_STATUS_PENDING, statusPending.getStatus());
            assertTrue("status locked", Boolean.TRUE.equals(statusPending.getLocked()));
            assertNull("status billId before pay", statusPending.getBillId());

            assertBlocked("update locked cart line", () -> updateLine(checkoutService, nxOrderId, "2"));
            assertBlocked("checkoutPreview while PENDING", () -> preview(checkoutService, nxOrderId));

            PlatformCheckoutPaymentStatusResponse cancelled =
                    paymentService.cancelPayment(cancelReq(paymentId));
            assertEq("cancel status", GbBillPlatformConstants.PAYMENT_STATUS_CANCELLED, cancelled.getStatus());
            assertTrue("cancel unlocked", !Boolean.TRUE.equals(cancelled.getLocked()));
            assertEq("cancel no bill", 0, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_bill WHERE gb_db_platform_submit_token=?", checkoutToken));
            assertEq("cancel nx still cart", -1, queryInt(dataSource,
                    "SELECT nx_DO_status FROM nx_department_orders WHERE nx_department_orders_id=?", nxOrderId));

            updateLine(checkoutService, nxOrderId, "2");
            System.out.println("[OK] update restored after cancel");

            String checkoutToken2 = uuid("pay-runner-2");
            Integer paymentId2 = paymentService.createPaymentIntentSnapshot(buildPayRequest(checkoutToken2, nxOrderId));
            assertBlocked("update locked again", () -> updateLine(checkoutService, nxOrderId, "3"));

            PlatformCheckoutPaymentStatusResponse finalized =
                    paymentService.completePaymentSuccessForRunner(paymentId2, "RUNNER_TXN_2");
            assertEq("finalize status", GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS, finalized.getStatus());
            assertTrue("finalize billId", finalized.getBillId() != null);
            assertEq("single bill for token2", 1, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_bill WHERE gb_db_platform_submit_token=?", checkoutToken2));

            paymentService.completePaymentSuccessForRunner(paymentId2, "RUNNER_TXN_2");
            assertEq("idempotent bill count", 1, queryInt(dataSource,
                    "SELECT COUNT(*) FROM gb_department_bill WHERE gb_db_platform_submit_token=?", checkoutToken2));

            String assignStatus = queryString(dataSource,
                    "SELECT nx_poa_assign_status FROM nx_platform_order_assign WHERE nx_poa_order_id=?",
                    nxOrderId);
            assertEq("assign ASSIGNED", PlatformConstants.ASSIGN_STATUS_ASSIGNED, assignStatus);

            System.out.println("[PASS] PlatformCheckoutPaymentRunner OK paymentId=" + paymentId2
                    + " billId=" + finalized.getBillId());
        } finally {
            ctx.close();
        }
    }

    private static PlatformCheckoutPayRequest buildPayRequest(String checkoutToken, Integer nxOrderId) {
        PlatformCheckoutPayRequest payReq = new PlatformCheckoutPayRequest();
        payReq.setMarketId(MARKET_ID);
        payReq.setGbDepartmentId(DEP_ID);
        payReq.setGbDepartmentFatherId(DEP_FATHER_ID);
        payReq.setGbDistributerId(GB_DIS_ID);
        payReq.setDeliveryDate("2026-06-21");
        payReq.setCheckoutToken(checkoutToken);
        payReq.setOrderIds(Arrays.asList(nxOrderId));
        payReq.setOpenId("runner-open-id");
        return payReq;
    }

    private static PlatformCheckoutPaymentStatusRequest statusReq(Integer paymentId) {
        PlatformCheckoutPaymentStatusRequest req = new PlatformCheckoutPaymentStatusRequest();
        req.setPaymentId(paymentId);
        req.setGbDepartmentId(DEP_ID);
        return req;
    }

    private static PlatformCheckoutPaymentCancelRequest cancelReq(Integer paymentId) {
        PlatformCheckoutPaymentCancelRequest req = new PlatformCheckoutPaymentCancelRequest();
        req.setPaymentId(paymentId);
        req.setGbDepartmentId(DEP_ID);
        return req;
    }

    private static void updateLine(PlatformCartCheckoutService checkoutService, Integer nxOrderId, String qty) {
        PlatformCartLineUpdateRequest update = new PlatformCartLineUpdateRequest();
        update.setMarketId(MARKET_ID);
        update.setGbDepartmentId(DEP_ID);
        update.setNxOrderId(nxOrderId);
        update.setQuantity(qty);
        update.setStandard("斤");
        checkoutService.updateCartLine(update);
    }

    private static void preview(PlatformCartCheckoutService checkoutService, Integer nxOrderId) {
        PlatformCheckoutPreviewRequest previewReq = new PlatformCheckoutPreviewRequest();
        previewReq.setMarketId(MARKET_ID);
        previewReq.setGbDepartmentId(DEP_ID);
        previewReq.setOrderIds(Arrays.asList(nxOrderId));
        checkoutService.checkoutPreview(previewReq);
    }

    private static void assertBlocked(String label, Runnable action) {
        try {
            action.run();
            throw new AssertionError("FAIL " + label + " expected exception");
        } catch (IllegalArgumentException ex) {
            System.out.println("[OK] " + label + " blocked: " + ex.getMessage());
        }
    }

    private static PlatformCartSubmitRequest buildSupplierRequest(PlatformCartSubmitItemRequest... items) {
        PlatformCartSubmitRequest req = new PlatformCartSubmitRequest();
        req.setMarketId(MARKET_ID);
        req.setGbDepartmentId(DEP_ID);
        req.setGbDepartmentFatherId(DEP_FATHER_ID);
        req.setGbDistributerId(GB_DIS_ID);
        req.setNxDistributerId(NX_DIS_ID);
        req.setDeliveryDate("2026-06-21");
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
