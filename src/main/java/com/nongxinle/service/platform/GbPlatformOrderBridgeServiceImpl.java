package com.nongxinle.service.platform;

import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.dto.platform.GbPlatformOrderBridgeResult;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.GbPlatformOrderBridgeService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.SalesPriceUtils;
import com.nongxinle.utils.SalesPriceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class GbPlatformOrderBridgeServiceImpl implements GbPlatformOrderBridgeService {

    private static final Logger logger = LoggerFactory.getLogger(GbPlatformOrderBridgeServiceImpl.class);
    private static final int DEFAULT_MARKET_ID = 1;

    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private NxPlatformOrderFulfillmentDao nxPlatformOrderFulfillmentDao;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private PlatformOrderFulfillmentService platformOrderFulfillmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GbPlatformOrderBridgeResult onNxOrderCreatedFromGb(
            GbDepartmentOrdersEntity gbOrder,
            NxDepartmentOrdersEntity nxOrder) {

        validateBridgeInput(gbOrder, nxOrder);

        Integer nxOrderId = nxOrder.getNxDepartmentOrdersId();
        Integer gbOrderId = gbOrder.getGbDepartmentOrdersId();

        NxPlatformOrderAssignEntity existing = nxPlatformOrderAssignDao.queryByOrderId(nxOrderId);
        if (existing == null) {
            existing = nxPlatformOrderAssignDao.queryByGbDepartmentOrderId(gbOrderId);
        }
        if (existing != null) {
            logger.info("[onNxOrderCreatedFromGb] 已存在 platform assign nxOrderId={} gbOrderId={} poaId={}",
                    nxOrderId, gbOrderId, existing.getNxPoaId());
            return buildResult(gbOrderId, nxOrderId, existing, true);
        }

        NxPlatformOrderAssignEntity poa = buildNewAssign(gbOrder, nxOrder);
        try {
            nxPlatformOrderAssignDao.save(poa);
        } catch (DuplicateKeyException e) {
            NxPlatformOrderAssignEntity raced = nxPlatformOrderAssignDao.queryByGbDepartmentOrderId(gbOrderId);
            if (raced == null) {
                raced = nxPlatformOrderAssignDao.queryByOrderId(nxOrderId);
            }
            if (raced != null) {
                logger.warn("[onNxOrderCreatedFromGb] 唯一索引兜底命中 nxOrderId={} gbOrderId={} poaId={}",
                        nxOrderId, gbOrderId, raced.getNxPoaId());
                return buildResult(gbOrderId, nxOrderId, raced, true);
            }
            throw e;
        }

        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(nxOrder.getNxDoDisGoodsId());
        PlatformDisGoodsCostResolver.CostResolveResult costResult =
                PlatformDisGoodsCostResolver.resolve(disGoods, nxOrder);
        Integer operatorId = gbOrder.getGbDoOrderUserId();

        platformOrderFulfillmentService.ensureAssignedFulfillment(poa, nxOrder, costResult, operatorId);

        logger.info("[onNxOrderCreatedFromGb] 新建 platform assign nxOrderId={} gbOrderId={} poaId={}",
                nxOrderId, gbOrderId, poa.getNxPoaId());
        return buildResult(gbOrderId, nxOrderId, poa, false);
    }

    private GbPlatformOrderBridgeResult buildResult(
            Integer gbOrderId,
            Integer nxOrderId,
            NxPlatformOrderAssignEntity poa,
            boolean idempotent) {

        GbPlatformOrderBridgeResult result = new GbPlatformOrderBridgeResult();
        result.setGbDepartmentOrderId(gbOrderId);
        result.setNxDepartmentOrderId(nxOrderId);
        result.setPlatformAssignId(poa.getNxPoaId());
        result.setIdempotent(idempotent);

        NxPlatformOrderFulfillmentEntity pof = nxPlatformOrderFulfillmentDao.queryByOrderId(nxOrderId);
        if (pof != null) {
            result.setFulfillmentId(pof.getNxPofId());
        }
        return result;
    }

    private NxPlatformOrderAssignEntity buildNewAssign(
            GbDepartmentOrdersEntity gbOrder,
            NxDepartmentOrdersEntity nxOrder) {

        Integer distributerId = nxOrder.getNxDoDistributerId();
        Integer disGoodsId = nxOrder.getNxDoDisGoodsId();
        Integer nxGoodsId = nxOrder.getNxDoNxGoodsId() != null
                ? nxOrder.getNxDoNxGoodsId()
                : gbOrder.getGbDoNxGoodsId();

        Date now = new Date();
        NxPlatformOrderAssignEntity poa = new NxPlatformOrderAssignEntity();
        poa.setNxPoaMarketId(resolveMarketId(distributerId));
        poa.setNxPoaOrderId(nxOrder.getNxDepartmentOrdersId());
        poa.setNxPoaDepartmentId(resolvePlatformDepartmentId(gbOrder));
        poa.setNxPoaNxGoodsId(nxGoodsId);
        poa.setNxPoaAssignStatus(PlatformConstants.ASSIGN_STATUS_ASSIGNED);
        poa.setNxPoaAssignMode(PlatformConstants.ASSIGN_MODE_PLATFORM);
        poa.setNxPoaAssignSource(PlatformConstants.ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER);
        poa.setNxPoaSourceType(PlatformConstants.SOURCE_TYPE_GB);
        poa.setNxPoaGbDepartmentId(gbOrder.getGbDoDepartmentId());
        poa.setNxPoaGbDepartmentFatherId(gbOrder.getGbDoDepartmentFatherId());
        poa.setNxPoaGbDepartmentOrderId(gbOrder.getGbDepartmentOrdersId());
        poa.setNxPoaAssignedDistributerId(distributerId);
        poa.setNxPoaAssignedDisGoodsId(disGoodsId);
        poa.setNxPoaAssignedPrice(resolveAssignedPrice(nxOrder));
        poa.setNxPoaAssignedAt(now);
        poa.setNxPoaAssignedBy(gbOrder.getGbDoOrderUserId());
        return poa;
    }

    private void validateBridgeInput(GbDepartmentOrdersEntity gbOrder, NxDepartmentOrdersEntity nxOrder) {
        if (gbOrder == null || nxOrder == null) {
            throw new IllegalArgumentException("GB / NX 订单不能为空");
        }
        if (gbOrder.getGbDepartmentOrdersId() == null) {
            throw new IllegalStateException("GB 订单尚未保存");
        }
        if (nxOrder.getNxDepartmentOrdersId() == null) {
            throw new IllegalStateException("NX 订单尚未保存");
        }
        if (gbOrder.getGbDoNxDepartmentOrderId() == null
                || !gbOrder.getGbDoNxDepartmentOrderId().equals(nxOrder.getNxDepartmentOrdersId())) {
            throw new IllegalStateException("GB↔NX 双向关联未完成");
        }
        if (nxOrder.getNxDoGbDepartmentOrderId() == null
                || !nxOrder.getNxDoGbDepartmentOrderId().equals(gbOrder.getGbDepartmentOrdersId())) {
            throw new IllegalStateException("NX→GB 关联未完成");
        }
        if (nxOrder.getNxDoDistributerId() == null) {
            throw new IllegalStateException("nxDoDistributerId 未确定");
        }
        if (nxOrder.getNxDoDisGoodsId() == null) {
            throw new IllegalStateException("nxDoDisGoodsId 未确定");
        }
    }

    private Integer resolveMarketId(Integer distributerId) {
        NxDistributerEntity distributer = nxDistributerService.queryObject(distributerId);
        if (distributer != null && distributer.getNxDistributerSysMarketId() != null) {
            return distributer.getNxDistributerSysMarketId();
        }
        return DEFAULT_MARKET_ID;
    }

    private Integer resolvePlatformDepartmentId(GbDepartmentOrdersEntity gbOrder) {
        Integer fatherId = gbOrder.getGbDoDepartmentFatherId();
        if (fatherId != null && fatherId > 0) {
            return fatherId;
        }
        return gbOrder.getGbDoDepartmentId();
    }

    private BigDecimal resolveAssignedPrice(NxDepartmentOrdersEntity nxOrder) {
        String price = nxOrder.getNxDoPrice();
        if (!SalesPriceUtils.isValidSalesPrice(price)) {
            return null;
        }
        try {
            return new BigDecimal(price.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
