package com.nongxinle.platform;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformAssignResponse;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineResponse;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.service.platform.PlatformDisGoodsCostResolver;
import com.nongxinle.utils.PlatformConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Phase 2b Round 1 本地验收 Runner（不依赖 Tomcat / Surefire）。
 */
public class PlatformOrderFulfillmentRound1Runner {

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
            NxPlatformOrderFulfillmentDao fulfillmentDao = ctx.getBean(NxPlatformOrderFulfillmentDao.class);
            NxDepartmentOrdersDao ordersDao = ctx.getBean(NxDepartmentOrdersDao.class);

            int baseline = fulfillmentDao.countAll();
            System.out.println("[baseline] fulfillment count = " + baseline);

            PlatformSubmitLineRequest submitReq = new PlatformSubmitLineRequest();
            submitReq.setMarketId(MARKET_ID);
            submitReq.setDepartmentId(DEPARTMENT_ID);
            submitReq.setNxGoodsId(NX_GOODS_ID);
            submitReq.setQuantity("2");
            submitReq.setStandard("斤");
            submitReq.setOrderUserId(OPERATOR_ID);

            PlatformSubmitLineResponse submitResp = assignService.submitLine(submitReq);
            Integer orderId = submitResp.getOrderId();
            assertTrue("submitLine orderId", orderId != null);
            assertEq("submit assignStatus", PlatformConstants.ASSIGN_STATUS_PENDING, submitResp.getAssignStatus());
            assertTrue("PENDING 无 fulfillment", fulfillmentService.queryByOrderId(orderId) == null);
            System.out.println("[submitLine] orderId=" + orderId + " status=PENDING fulfillment=null OK");

            PlatformAssignRequest assignReq = new PlatformAssignRequest();
            assignReq.setMarketId(MARKET_ID);
            assignReq.setOrderId(orderId);
            assignReq.setDisGoodsId(DIS_GOODS_ID);
            assignReq.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
            assignReq.setReasonCode("OTHER");
            assignReq.setReasonNote("Round1 fulfillment runner");
            assignReq.setOperatorId(OPERATOR_ID);

            PlatformAssignResponse assignResp = assignService.assign(assignReq);
            assertEq("assign assignStatus", PlatformConstants.ASSIGN_STATUS_ASSIGNED, assignResp.getAssignStatus());
            assertEq("assign fulfillmentStatus", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, assignResp.getFulfillmentStatus());
            assertTrue("assign nxDoDistributerId", assignResp.getNxDoDistributerId() != null);
            assertTrue("assign nxDoDisGoodsId", assignResp.getNxDoDisGoodsId() != null);
            assertTrue("assign nxDoPrice", assignResp.getNxDoPrice() != null);
            assertTrue("assign nxDoSubtotal", assignResp.getNxDoSubtotal() != null);
            System.out.println("[assign] response=" + assignResp);

            NxPlatformOrderFulfillmentEntity pof = fulfillmentService.queryByOrderId(orderId);
            assertTrue("fulfillment exists", pof != null);
            assertEq("pof status", PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, pof.getNxPofFulfillmentStatus());
            System.out.println("[fulfillment] " + pof);

            NxPlatformOrderAssignEntity poa = assignService.queryByOrderId(orderId);
            NxDepartmentOrdersEntity order = ordersDao.queryObject(orderId);
            NxPlatformOrderFulfillmentEntity again = fulfillmentService.ensureAssignedFulfillment(
                    poa, order,
                    PlatformDisGoodsCostResolver.CostResolveResult.missing(
                            PlatformDisGoodsCostResolver.REASON_NO_VALID_BUYING_PRICE),
                    OPERATOR_ID);
            assertEq("idempotent pofId", pof.getNxPofId(), again.getNxPofId());
            assertEq("count +1 only", baseline + 1, fulfillmentDao.countAll());
            System.out.println("[idempotent] pofId unchanged, count=" + fulfillmentDao.countAll());

            try {
                assignService.assign(assignReq);
                throw new RuntimeException("重复 assign 应失败");
            } catch (IllegalArgumentException ex) {
                System.out.println("[repeat assign] rejected: " + ex.getMessage());
            }
            assertEq("count after repeat assign", baseline + 1, fulfillmentDao.countAll());

            for (int historicalOrderId : new int[]{200170, 200171, 200172, 200175}) {
                NxPlatformOrderFulfillmentEntity historical = fulfillmentService.queryByOrderId(historicalOrderId);
                assertTrue("historical fulfillment orderId=" + historicalOrderId, historical != null);
                assertEq("historical status orderId=" + historicalOrderId,
                        PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, historical.getNxPofFulfillmentStatus());
            }
            System.out.println("[historical] 200170/171/172/175 fulfillment OK");

            System.out.println("=== Phase 2b Java Round 1 ALL PASSED ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
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
