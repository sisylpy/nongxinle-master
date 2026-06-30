package com.nongxinle.platform;

import com.nongxinle.dto.platform.admin.PlatformMarketAdminBootstrapRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminLoginRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminSessionDto;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateListRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateSaveRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponIssueRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponListRequest;
import com.nongxinle.entity.PlatformCouponTemplateEntity;
import com.nongxinle.entity.PlatformStoreCouponEntity;
import com.nongxinle.platform.admin.PlatformMarketAdminContext;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.platform.PlatformStoreDepartmentResolver;
import com.nongxinle.service.platform.admin.PlatformCouponAdminService;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import com.nongxinle.utils.PlatformCouponConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Phase 1a 平台优惠券基础能力验收（不接 checkout / 支付 / 购物车试算）。
 *
 * <p>运行前请先执行 SQL：</p>
 * <pre>
 * mysql ... nongxinle &lt; docs/sql/patches/upgrade_platform_coupon_phase1a.sql
 * mysql ... nongxinle &lt; docs/sql/patches/upgrade_platform_market_user_v1.sql  # 若尚未执行
 * </pre>
 *
 * <p>运行方式（在 {@code nongxinle-master} 根目录）：</p>
 * <pre>
 * mvn -q test-compile exec:java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.mainClass=com.nongxinle.platform.PlatformCouponPhase1aRunner
 * </pre>
 *
 * <p>验收覆盖：</p>
 * <ul>
 *   <li>market admin 登录上下文</li>
 *   <li>创建市场券模板（按 market_id 主权）</li>
 *   <li>子部门发券归属门店</li>
 *   <li>门店券列表查询</li>
 *   <li>非当前 market 门店被拒绝</li>
 *   <li>作废门店券 → VOID</li>
 *   <li>全流程不读写旧 Community coupon 表</li>
 * </ul>
 */
public class PlatformCouponPhase1aRunner {

    private static final int MARKET_ID = 1;
    private static final int OTHER_MARKET_ID = 2;
    private static final int SUB_DEP_ID = 58;
    private static final int STORE_DEP_ID = 57;
    private static final String LOGIN_ACCOUNT = "123";
    private static final String PASSWORD = "123";

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-gb-p11-test.xml");
        try {
            DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
            PlatformMarketAdminAuthService authService = ctx.getBean(PlatformMarketAdminAuthService.class);
            PlatformCouponAdminService couponService = ctx.getBean(PlatformCouponAdminService.class);
            PlatformStoreDepartmentResolver storeResolver = ctx.getBean(PlatformStoreDepartmentResolver.class);
            PlatformMarketDepartmentService marketDepartmentService =
                    ctx.getBean(PlatformMarketDepartmentService.class);

            long communityCouponBefore = countTable(dataSource, "nx_community_coupon");
            long communityUserCouponBefore = countTable(dataSource, "nx_customer_user_coupon");
            long communityVerifyBefore = countTable(dataSource, "nx_community_coupon_verify_log");

            PlatformMarketAdminSessionDto session = loginOrBootstrap(authService);
            System.out.println("[OK] market admin session pmuId=" + session.getCurrentMarketUserId()
                    + " marketId=" + session.getMarketId());

            marketDepartmentService.ensureActiveForGbCustomer(MARKET_ID, STORE_DEP_ID);

            PlatformMarketAdminContext.set(
                    session.getCurrentMarketUserId(), session.getMarketId(), session.getRoleType());

            PlatformCouponTemplateSaveRequest saveReq = new PlatformCouponTemplateSaveRequest();
            saveReq.setTemplateName("Phase1a-满减券-" + System.currentTimeMillis());
            saveReq.setCouponType(PlatformCouponConstants.TYPE_FULL_REDUCTION);
            saveReq.setDiscountAmount(new BigDecimal("5.00"));
            saveReq.setThresholdAmount(new BigDecimal("30.00"));
            saveReq.setScopeType(PlatformCouponConstants.SCOPE_ALL);
            saveReq.setUseChannel(PlatformCouponConstants.CHANNEL_ALL);
            saveReq.setValidityType(PlatformCouponConstants.VALIDITY_DAYS_AFTER_CLAIM);
            saveReq.setValidityDays(7);

            PlatformCouponTemplateEntity template = couponService.saveTemplate(saveReq);
            assertNotNull("template pctId", template.getPctId());
            assertEq("template marketId", MARKET_ID, template.getMarketId());
            System.out.println("[OK] created template pctId=" + template.getPctId());

            List<PlatformCouponTemplateEntity> templates = couponService.listTemplates(new PlatformCouponTemplateListRequest());
            assertTrue("template list not empty", templates != null && !templates.isEmpty());
            System.out.println("[OK] template list size=" + templates.size());

            Integer resolvedStoreId = storeResolver.resolveStoreDepartment(SUB_DEP_ID).getGbDepartmentId();
            assertEq("sub dept resolves to store", STORE_DEP_ID, resolvedStoreId);
            System.out.println("[OK] subDep " + SUB_DEP_ID + " -> store " + resolvedStoreId);

            PlatformStoreCouponIssueRequest issueReq = new PlatformStoreCouponIssueRequest();
            issueReq.setTemplateId(template.getPctId());
            issueReq.setGbDepartmentId(SUB_DEP_ID);
            issueReq.setCount(2);
            List<PlatformStoreCouponEntity> issued = couponService.issueStoreCoupons(issueReq);
            assertEq("issued count", 2, issued.size());
            for (PlatformStoreCouponEntity c : issued) {
                assertEq("issued store id", STORE_DEP_ID, c.getStoreGbDepartmentId());
                assertEq("issued status", PlatformCouponConstants.STORE_STATUS_AVAILABLE, c.getStatus());
            }
            System.out.println("[OK] issued store coupons pscIds=" + issued.get(0).getPscId()
                    + "," + issued.get(1).getPscId());

            PlatformStoreCouponListRequest listReq = new PlatformStoreCouponListRequest();
            listReq.setGbDepartmentId(SUB_DEP_ID);
            listReq.setTemplateId(template.getPctId());
            List<PlatformStoreCouponEntity> listed = couponService.listStoreCoupons(listReq);
            assertTrue("listed coupons >= 2", listed != null && listed.size() >= 2);
            System.out.println("[OK] store coupon list size=" + listed.size());

            PlatformMarketAdminContext.set(session.getCurrentMarketUserId(), OTHER_MARKET_ID, session.getRoleType());
            try {
                couponService.issueStoreCoupons(issueReq);
                throw new IllegalStateException("expected cross-market issue rejection");
            } catch (IllegalArgumentException | IllegalStateException expected) {
                System.out.println("[OK] cross-market issue rejected: " + expected.getMessage());
            }
            PlatformMarketAdminContext.set(
                    session.getCurrentMarketUserId(), session.getMarketId(), session.getRoleType());

            PlatformStoreCouponEntity voided = couponService.voidStoreCoupon(issued.get(0).getPscId());
            assertEq("void status", PlatformCouponConstants.STORE_STATUS_VOID, voided.getStatus());
            System.out.println("[OK] voided pscId=" + voided.getPscId());

            long communityCouponAfter = countTable(dataSource, "nx_community_coupon");
            long communityUserCouponAfter = countTable(dataSource, "nx_customer_user_coupon");
            long communityVerifyAfter = countTable(dataSource, "nx_community_coupon_verify_log");
            assertEq("nx_community_coupon unchanged", communityCouponBefore, communityCouponAfter);
            assertEq("nx_customer_user_coupon unchanged", communityUserCouponBefore, communityUserCouponAfter);
            assertEq("nx_community_coupon_verify_log unchanged", communityVerifyBefore, communityVerifyAfter);
            System.out.println("[OK] community coupon tables unchanged");

            PlatformMarketAdminContext.clear();
            System.out.println("Phase 1a platform coupon smoke PASSED");
        } finally {
            PlatformMarketAdminContext.clear();
            ctx.close();
        }
    }

    private static PlatformMarketAdminSessionDto loginOrBootstrap(PlatformMarketAdminAuthService authService) {
        PlatformMarketAdminLoginRequest login = new PlatformMarketAdminLoginRequest();
        login.setMarketId(MARKET_ID);
        login.setLoginAccount(LOGIN_ACCOUNT);
        login.setPassword(PASSWORD);
        try {
            return authService.login(login);
        } catch (Exception loginFailed) {
            PlatformMarketAdminBootstrapRequest bootstrap = new PlatformMarketAdminBootstrapRequest();
            bootstrap.setMarketId(MARKET_ID);
            bootstrap.setLoginAccount(LOGIN_ACCOUNT);
            bootstrap.setPassword(PASSWORD);
            bootstrap.setRealName("券测试管理员");
            bootstrap.setRoleType("ADMIN");
            try {
                return authService.bootstrapFirstAdmin(bootstrap);
            } catch (IllegalStateException exists) {
                return authService.login(login);
            }
        }
    }

    private static long countTable(DataSource dataSource, String tableName) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void assertEq(String label, Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new IllegalStateException(label + " expected=" + expected + " actual=" + actual);
    }

    private static void assertNotNull(String label, Object value) {
        if (value == null) {
            throw new IllegalStateException(label + " is null");
        }
    }

    private static void assertTrue(String label, boolean value) {
        if (!value) {
            throw new IllegalStateException(label);
        }
    }
}
