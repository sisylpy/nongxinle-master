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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore("依赖本地库测试数据，远程库上不存在")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-platform-fulfillment-test.xml")
public class PlatformOrderFulfillmentRound1Test {

    private static final int MARKET_ID = 1;
    private static final int DEPARTMENT_ID = 1436;
    private static final int NX_GOODS_ID = 100470;
    private static final int DIS_GOODS_ID = 31235;
    private static final int OPERATOR_ID = 1;

    @Autowired
    private PlatformOrderAssignService platformOrderAssignService;
    @Autowired
    private PlatformOrderFulfillmentService platformOrderFulfillmentService;
    @Autowired
    private NxPlatformOrderFulfillmentDao nxPlatformOrderFulfillmentDao;
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;

    @Test
    public void assignCreatesFulfillmentIdempotently() {
        int baselineCount = countFulfillment();

        PlatformSubmitLineRequest submitReq = new PlatformSubmitLineRequest();
        submitReq.setMarketId(MARKET_ID);
        submitReq.setDepartmentId(DEPARTMENT_ID);
        submitReq.setNxGoodsId(NX_GOODS_ID);
        submitReq.setQuantity("2");
        submitReq.setStandard("斤");
        submitReq.setOrderUserId(OPERATOR_ID);

        PlatformSubmitLineResponse submitResp = platformOrderAssignService.submitLine(submitReq);
        Integer orderId = submitResp.getOrderId();
        Assert.assertNotNull(orderId);
        Assert.assertEquals(PlatformConstants.ASSIGN_STATUS_PENDING, submitResp.getAssignStatus());
        Assert.assertNull(platformOrderFulfillmentService.queryByOrderId(orderId));

        PlatformAssignRequest assignReq = buildAssignRequest(orderId);
        PlatformAssignResponse assignResp = platformOrderAssignService.assign(assignReq);

        Assert.assertEquals(PlatformConstants.ASSIGN_STATUS_ASSIGNED, assignResp.getAssignStatus());
        Assert.assertNotNull(assignResp.getPlatformAssignId());
        Assert.assertNotNull(assignResp.getNxDoDistributerId());
        Assert.assertNotNull(assignResp.getNxDoDisGoodsId());
        Assert.assertNotNull(assignResp.getNxDoPrice());
        Assert.assertNotNull(assignResp.getNxDoSubtotal());
        Assert.assertEquals(PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, assignResp.getFulfillmentStatus());
        Assert.assertEquals(Integer.valueOf(1), assignResp.getCostMissing());

        NxPlatformOrderFulfillmentEntity pof = platformOrderFulfillmentService.queryByOrderId(orderId);
        Assert.assertNotNull(pof);
        Assert.assertEquals(PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, pof.getNxPofFulfillmentStatus());
        Assert.assertEquals(orderId, pof.getNxPofOrderId());
        Assert.assertEquals(assignResp.getPlatformAssignId(), pof.getNxPofPlatformAssignId());
        Assert.assertEquals(Integer.valueOf(1), pof.getNxPofCostMissing());

        NxPlatformOrderAssignEntity poa = platformOrderAssignService.queryByOrderId(orderId);
        NxDepartmentOrdersEntity order = nxDepartmentOrdersDao.queryObject(orderId);
        NxPlatformOrderFulfillmentEntity second = platformOrderFulfillmentService.ensureAssignedFulfillment(
                poa, order, PlatformDisGoodsCostResolver.CostResolveResult.missing(
                        PlatformDisGoodsCostResolver.REASON_NO_VALID_BUYING_PRICE), OPERATOR_ID);
        Assert.assertEquals(pof.getNxPofId(), second.getNxPofId());
        Assert.assertEquals(baselineCount + 1, countFulfillment());

        try {
            platformOrderAssignService.assign(assignReq);
            Assert.fail("重复 assign 应失败");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("PENDING"));
        }
        Assert.assertEquals(baselineCount + 1, countFulfillment());

        for (int historicalOrderId : new int[]{200170, 200171, 200172, 200175}) {
            NxPlatformOrderFulfillmentEntity historical = platformOrderFulfillmentService.queryByOrderId(historicalOrderId);
            Assert.assertNotNull("历史 fulfillment 应保留 orderId=" + historicalOrderId, historical);
            Assert.assertEquals(PlatformConstants.FULFILLMENT_STATUS_ASSIGNED, historical.getNxPofFulfillmentStatus());
        }
    }

    private PlatformAssignRequest buildAssignRequest(Integer orderId) {
        PlatformAssignRequest req = new PlatformAssignRequest();
        req.setMarketId(MARKET_ID);
        req.setOrderId(orderId);
        req.setDisGoodsId(DIS_GOODS_ID);
        req.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
        req.setReasonCode("OTHER");
        req.setReasonNote("Round1 fulfillment test");
        req.setOperatorId(OPERATOR_ID);
        return req;
    }

    private int countFulfillment() {
        return nxPlatformOrderFulfillmentDao.countAll();
    }
}
