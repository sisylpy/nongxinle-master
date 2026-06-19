package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentNxGoodsDefaultDao;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dao.NxSupplierSwitchLogDao;
import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformAssignResponse;
import com.nongxinle.dto.platform.PlatformDefaultRecommendInfo;
import com.nongxinle.dto.platform.PlatformDistributerSummaryItem;
import com.nongxinle.dto.platform.PlatformOrderDetailLine;
import com.nongxinle.dto.platform.PlatformOrderDetailRequest;
import com.nongxinle.dto.platform.PlatformOrderDetailResponse;
import com.nongxinle.dto.platform.PlatformOrderDetailRow;
import com.nongxinle.dto.platform.PlatformPendingCustomerItem;
import com.nongxinle.dto.platform.PlatformPendingGroupRow;
import com.nongxinle.dto.platform.PlatformPendingRequest;
import com.nongxinle.dto.platform.PlatformPendingResponse;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineResponse;
import com.nongxinle.entity.NxDepartmentDisGoodsEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentNxGoodsDefaultEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.entity.NxMarketDepartmentEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.entity.NxSupplierSwitchLogEntity;
import com.nongxinle.service.NxDepartmentDisGoodsService;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.service.PlatformOrderFulfillmentService;
import com.nongxinle.service.platform.PlatformDisGoodsCostResolver;
import com.nongxinle.service.platform.PlatformDisGoodsValidator;
import com.nongxinle.service.platform.PlatformDistributerIdResolver;
import com.nongxinle.utils.SalesPriceUtils;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.PlatformOrderPriceSupport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDate;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatTime;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.DateUtils.getWeekOfYear;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;

@Service("platformOrderAssignService")
public class PlatformOrderAssignServiceImpl implements PlatformOrderAssignService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformOrderAssignServiceImpl.class);

    private static final int NX_DO_GOODS_NAME_DB_MAX_CHARS = 200;

    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private NxDepartmentNxGoodsDefaultDao nxDepartmentNxGoodsDefaultDao;
    @Autowired
    private NxSupplierSwitchLogDao nxSupplierSwitchLogDao;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private PlatformDistributerIdResolver platformDistributerIdResolver;
    @Autowired
    private PlatformOrderFulfillmentService platformOrderFulfillmentService;

    @Override
    @Transactional
    public PlatformSubmitLineResponse submitLine(PlatformSubmitLineRequest request) {
        validateSubmitRequest(request);

        NxMarketDepartmentEntity marketDepartment = platformMarketDepartmentService.queryActive(
                request.getMarketId(), request.getDepartmentId());
        if (marketDepartment == null) {
            throw new IllegalArgumentException("客户不属于该市场或未激活: marketId="
                    + request.getMarketId() + ", departmentId=" + request.getDepartmentId());
        }

        NxDepartmentEntity department = nxDepartmentService.queryObject(request.getDepartmentId());
        if (department == null) {
            throw new IllegalArgumentException("客户不存在: departmentId=" + request.getDepartmentId());
        }

        NxGoodsEntity goods = nxGoodsService.queryObject(request.getNxGoodsId());
        if (goods == null) {
            throw new IllegalArgumentException("标准商品不存在: nxGoodsId=" + request.getNxGoodsId());
        }

        String goodsName = StringUtils.isNotBlank(request.getGoodsName())
                ? request.getGoodsName().trim()
                : goods.getNxGoodsName();

        NxDepartmentOrdersEntity order = buildPendingPlatformOrder(
                request, department, goods, goodsName);

        nxDepartmentOrdersDao.save(order);

        NxPlatformOrderAssignEntity assign = new NxPlatformOrderAssignEntity();
        assign.setNxPoaMarketId(request.getMarketId());
        assign.setNxPoaOrderId(order.getNxDepartmentOrdersId());
        assign.setNxPoaDepartmentId(request.getDepartmentId());
        assign.setNxPoaNxGoodsId(request.getNxGoodsId());
        assign.setNxPoaAssignStatus(PlatformConstants.ASSIGN_STATUS_PENDING);
        assign.setNxPoaAssignMode(PlatformConstants.ASSIGN_MODE_PLATFORM);
        nxPlatformOrderAssignDao.save(assign);

        return buildSubmitLineResponse(order, assign);
    }

    @Override
    public PlatformPendingResponse listPending(PlatformPendingRequest request) {
        if (request == null || request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        String applyDate = StringUtils.isNotBlank(request.getApplyDate())
                ? request.getApplyDate().trim()
                : formatWhatDay(0);

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", request.getMarketId());
        params.put("applyDate", applyDate);

        List<PlatformPendingGroupRow> rows = nxPlatformOrderAssignDao.queryPendingGroupedByDepartment(params);
        List<PlatformPendingCustomerItem> customers = new ArrayList<>();
        for (PlatformPendingGroupRow row : rows) {
            PlatformPendingCustomerItem item = new PlatformPendingCustomerItem();
            item.setDepartmentId(row.getDepartmentId());
            item.setDepartmentName(row.getDepartmentName());
            item.setDepartmentOrderCode(row.getDepartmentOrderCode());
            item.setPendingLineCount(row.getPendingLineCount());
            item.setOrderIds(parseOrderIds(row.getOrderIdsCsv()));
            item.setFirstPendingAt(row.getFirstPendingAt());
            item.setLastPendingAt(row.getLastPendingAt());
            customers.add(item);
        }

        PlatformPendingResponse response = new PlatformPendingResponse();
        response.setMarketId(request.getMarketId());
        response.setApplyDate(applyDate);
        response.setCustomers(customers);
        response.setTotalPendingLines(nxPlatformOrderAssignDao.countPendingByMarket(params));
        return response;
    }

    @Override
    public PlatformOrderDetailResponse getDetail(PlatformOrderDetailRequest request) {
        if (request == null || request.getMarketId() == null || request.getDepartmentId() == null) {
            throw new IllegalArgumentException("marketId、departmentId 不能为空");
        }
        requireActiveMarketDepartment(request.getMarketId(), request.getDepartmentId());

        String applyDate = StringUtils.isNotBlank(request.getApplyDate())
                ? request.getApplyDate().trim()
                : formatWhatDay(0);

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", request.getMarketId());
        params.put("departmentId", request.getDepartmentId());
        params.put("applyDate", applyDate);
        if (request.getOrderIds() != null && !request.getOrderIds().isEmpty()) {
            params.put("orderIds", request.getOrderIds());
        }

        List<PlatformOrderDetailRow> rows = nxPlatformOrderAssignDao.queryPlatformOrderDetailLines(params);
        List<PlatformOrderDetailLine> lines = new ArrayList<>();
        Map<Integer, PlatformDistributerSummaryItem> summaryMap = new LinkedHashMap<>();

        for (PlatformOrderDetailRow row : rows) {
            PlatformOrderDetailLine line = new PlatformOrderDetailLine();
            line.setOrderId(row.getOrderId());
            line.setPlatformAssignId(row.getPlatformAssignId());
            line.setNxGoodsId(row.getNxGoodsId());
            line.setGoodsName(row.getGoodsName());
            line.setQuantity(row.getQuantity());
            line.setStandard(row.getStandard());
            line.setRemark(row.getRemark());
            line.setAssignStatus(row.getAssignStatus());
            line.setAssignMode(row.getAssignMode());
            line.setAssignedDistributerId(row.getAssignedDistributerId());
            line.setAssignedDisGoodsId(row.getAssignedDisGoodsId());
            line.setOrderPrice(row.getOrderPrice());
            line.setOrderSubtotal(row.getOrderSubtotal());
            if (row.getDefaultId() != null) {
                PlatformDefaultRecommendInfo def = new PlatformDefaultRecommendInfo();
                def.setDefaultId(row.getDefaultId());
                def.setDefaultDistributerId(row.getDefaultDistributerId());
                def.setDefaultDisGoodsId(row.getDefaultDisGoodsId());
                def.setSource(row.getDefaultSource());
                line.setDefaultRecommend(def);
            }
            lines.add(line);

            if (PlatformConstants.ASSIGN_STATUS_ASSIGNED.equals(row.getAssignStatus())
                    && row.getAssignedDistributerId() != null) {
                accumulateDistributerSummary(summaryMap, row);
            }
        }

        NxDepartmentEntity department = nxDepartmentService.queryObject(request.getDepartmentId());
        PlatformOrderDetailResponse response = new PlatformOrderDetailResponse();
        response.setMarketId(request.getMarketId());
        response.setDepartmentId(request.getDepartmentId());
        response.setDepartmentName(department != null ? department.getNxDepartmentName() : null);
        response.setApplyDate(applyDate);
        response.setLines(lines);
        response.setDistributerSummary(new ArrayList<>(summaryMap.values()));
        return response;
    }

    @Override
    @Transactional
    public PlatformAssignResponse assign(PlatformAssignRequest request) {
        validateAssignRequest(request);

        NxPlatformOrderAssignEntity poa = nxPlatformOrderAssignDao.queryByOrderId(request.getOrderId());
        if (poa == null) {
            throw new IllegalArgumentException("平台分配记录不存在: orderId=" + request.getOrderId());
        }
        if (!request.getMarketId().equals(poa.getNxPoaMarketId())) {
            throw new IllegalArgumentException("marketId 与平台分配记录不匹配");
        }
        if (!PlatformConstants.ASSIGN_MODE_PLATFORM.equals(poa.getNxPoaAssignMode())) {
            throw new IllegalArgumentException("非平台订单，不可通过 platform assign 分配");
        }
        if (!PlatformConstants.ASSIGN_STATUS_PENDING.equals(poa.getNxPoaAssignStatus())) {
            throw new IllegalArgumentException("订单不是 PENDING 状态，当前: " + poa.getNxPoaAssignStatus());
        }

        NxDepartmentOrdersEntity order = nxDepartmentOrdersDao.queryObject(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: orderId=" + request.getOrderId());
        }

        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryDisGoodsDetail(request.getDisGoodsId());
        validateDisGoodsForMarket(request.getMarketId(), disGoods, order);

        Integer targetDisId = disGoods.getNxDgDistributerId();
        applyDisGoodsToPlatformOrder(order, disGoods);

        AssignPricingTrace pricingTrace = applyPlatformAssignPricing(order, disGoods);
        assertValidAssignOrderPrice(order, pricingTrace);

        PlatformDisGoodsCostResolver.CostResolveResult costResult =
                PlatformDisGoodsCostResolver.resolve(disGoods, order);
        applyPlatformAssignCost(order, costResult);

        nxDepartmentOrdersDao.update(order);

        Integer defaultId;
        Integer switchLogId;
        AssignSideEffect sideEffect = upsertDefaultAndSwitchLog(request, poa, order, disGoods, targetDisId);
        defaultId = sideEffect.defaultId;
        switchLogId = sideEffect.switchLogId;

        poa.setNxPoaAssignStatus(PlatformConstants.ASSIGN_STATUS_ASSIGNED);
        poa.setNxPoaAssignedDistributerId(targetDisId);
        poa.setNxPoaAssignedDisGoodsId(disGoods.getNxDistributerGoodsId());
        poa.setNxPoaAssignedPrice(parsePriceDecimal(order.getNxDoPrice()));
        poa.setNxPoaAssignedAt(new Date());
        poa.setNxPoaAssignedBy(request.getOperatorId());
        poa.setNxPoaDefaultId(defaultId);
        poa.setNxPoaSwitchLogId(switchLogId);
        nxPlatformOrderAssignDao.update(poa);

        NxPlatformOrderFulfillmentEntity fulfillment = platformOrderFulfillmentService.ensureAssignedFulfillment(
                poa, order, costResult, request.getOperatorId());

        PlatformAssignResponse response = new PlatformAssignResponse();
        response.setOrderId(order.getNxDepartmentOrdersId());
        response.setPlatformAssignId(poa.getNxPoaId());
        response.setAssignStatus(poa.getNxPoaAssignStatus());
        response.setNxDoDistributerId(order.getNxDoDistributerId());
        response.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        response.setNxDoCollaborativeNxDisId(order.getNxDoCollaborativeNxDisId());
        response.setNxDoPrice(order.getNxDoPrice());
        response.setNxDoExpectPrice(order.getNxDoExpectPrice());
        response.setNxDoPriceDifferent(order.getNxDoPriceDifferent());
        response.setNxDoSubtotal(order.getNxDoSubtotal());
        response.setDefaultId(defaultId);
        response.setSwitchLogId(switchLogId);
        response.setFulfillmentStatus(fulfillment.getNxPofFulfillmentStatus());
        response.setCostMissing(fulfillment.getNxPofCostMissing());
        return response;
    }

    @Override
    public NxPlatformOrderAssignEntity queryByOrderId(Integer orderId) {
        return nxPlatformOrderAssignDao.queryByOrderId(orderId);
    }

    private static final class AssignSideEffect {
        private Integer switchLogId;
        private Integer defaultId;
    }

    private AssignSideEffect upsertDefaultAndSwitchLog(
            PlatformAssignRequest request,
            NxPlatformOrderAssignEntity poa,
            NxDepartmentOrdersEntity order,
            NxDistributerGoodsEntity disGoods,
            Integer targetDisId) {

        AssignSideEffect result = new AssignSideEffect();

        NxSupplierSwitchLogEntity log = new NxSupplierSwitchLogEntity();
        log.setNxSslMarketId(request.getMarketId());
        log.setNxSslDepartmentId(poa.getNxPoaDepartmentId());
        log.setNxSslNxGoodsId(order.getNxDoNxGoodsId());
        log.setNxSslOrderId(order.getNxDepartmentOrdersId());
        log.setNxSslFromDistributerId(null);
        log.setNxSslFromDisGoodsId(null);
        log.setNxSslToDistributerId(targetDisId);
        log.setNxSslToDisGoodsId(disGoods.getNxDistributerGoodsId());
        log.setNxSslSwitchScope(request.getSwitchScope());
        log.setNxSslReasonCode(StringUtils.isNotBlank(request.getReasonCode())
                ? request.getReasonCode().trim() : "OTHER");
        log.setNxSslReasonNote(request.getReasonNote());
        log.setNxSslSnapshotAction("NONE");
        log.setNxSslOperatorId(request.getOperatorId());
        nxSupplierSwitchLogDao.save(log);
        result.switchLogId = log.getNxSslId();

        if (PlatformConstants.SWITCH_SCOPE_ORDER_AND_DEFAULT.equals(request.getSwitchScope())) {
            result.defaultId = upsertDefaultRelation(
                    request.getMarketId(),
                    poa.getNxPoaDepartmentId(),
                    order.getNxDoNxGoodsId(),
                    targetDisId,
                    disGoods.getNxDistributerGoodsId(),
                    order.getNxDepartmentOrdersId(),
                    log.getNxSslId(),
                    request.getOperatorId());
        }
        return result;
    }

    private Integer upsertDefaultRelation(
            Integer marketId,
            Integer departmentId,
            Integer nxGoodsId,
            Integer distributerId,
            Integer disGoodsId,
            Integer orderId,
            Integer switchLogId,
            Integer operatorId) {

        Map<String, Object> query = new HashMap<>();
        query.put("marketId", marketId);
        query.put("departmentId", departmentId);
        query.put("nxGoodsId", nxGoodsId);
        NxDepartmentNxGoodsDefaultEntity existing = nxDepartmentNxGoodsDefaultDao.queryActiveByMarketDepGoods(query);

        if (existing == null) {
            NxDepartmentNxGoodsDefaultEntity created = new NxDepartmentNxGoodsDefaultEntity();
            created.setNxDngdMarketId(marketId);
            created.setNxDngdDepartmentId(departmentId);
            created.setNxDngdNxGoodsId(nxGoodsId);
            created.setNxDngdDefaultDistributerId(distributerId);
            created.setNxDngdDefaultDisGoodsId(disGoodsId);
            created.setNxDngdSource("AUTO_SWITCH");
            created.setNxDngdStatus(PlatformConstants.MARKET_DEP_STATUS_ACTIVE);
            created.setNxDngdLastOrderId(orderId);
            created.setNxDngdLastSwitchLogId(switchLogId);
            created.setNxDngdCreatedBy(operatorId);
            created.setNxDngdUpdatedBy(operatorId);
            nxDepartmentNxGoodsDefaultDao.save(created);
            return created.getNxDngdId();
        }

        existing.setNxDngdDefaultDistributerId(distributerId);
        existing.setNxDngdDefaultDisGoodsId(disGoodsId);
        existing.setNxDngdSource("AUTO_SWITCH");
        existing.setNxDngdLastOrderId(orderId);
        existing.setNxDngdLastSwitchLogId(switchLogId);
        existing.setNxDngdUpdatedBy(operatorId);
        nxDepartmentNxGoodsDefaultDao.update(existing);
        return existing.getNxDngdId();
    }

    private void applyDisGoodsToPlatformOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoods) {
        order.setNxDoDistributerId(disGoods.getNxDgDistributerId());
        order.setNxDoDisGoodsId(disGoods.getNxDistributerGoodsId());
        order.setNxDoNxGoodsId(disGoods.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoods.getNxDgNxFatherId());
        order.setNxDoDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
        order.setNxDoCollaborativeNxDisId(-1);
        order.setNxDoGoodsType(disGoods.getNxDgPurchaseAuto());
        order.setNxDoCostPriceLevel("1");
    }

    private void validateDisGoodsForMarket(
            Integer marketId, NxDistributerGoodsEntity disGoods, NxDepartmentOrdersEntity order) {
        if (disGoods == null) {
            throw new IllegalArgumentException("配送商商品不存在");
        }
        if (disGoods.getNxDgNxGoodsId() == null || disGoods.getNxDgNxGoodsId() <= 0
                || disGoods.getNxDgDfgGoodsFatherId() == null) {
            throw new IllegalArgumentException("配送商商品不可分配：临时或未关联标准商品");
        }
        if (disGoods.getNxDgPullOff() != null && disGoods.getNxDgPullOff() != 0) {
            throw new IllegalArgumentException("配送商商品已下架");
        }
        if (disGoods.getNxDgGoodsIsHidden() != null && disGoods.getNxDgGoodsIsHidden() != 0) {
            throw new IllegalArgumentException("配送商商品不可售或已隐藏");
        }
        if (!PlatformDisGoodsValidator.hasValidSalesQuote(disGoods)) {
            throw new IllegalArgumentException(PlatformDisGoodsValidator.MSG_INVALID_DIS_GOODS_SALES_PRICE);
        }
        NxDistributerEntity distributer = nxDistributerService.queryObject(disGoods.getNxDgDistributerId());
        if (distributer == null || distributer.getNxDistributerSysMarketId() == null
                || !marketId.equals(distributer.getNxDistributerSysMarketId())) {
            throw new IllegalArgumentException("配送商不属于 marketId=" + marketId);
        }
        if (order.getNxDoNxGoodsId() != null && disGoods.getNxDgNxGoodsId() != null
                && !order.getNxDoNxGoodsId().equals(disGoods.getNxDgNxGoodsId())) {
            throw new IllegalArgumentException("配送商商品标准商品与订单 nxGoodsId 不一致");
        }
    }

    private static final class AssignPricingTrace {
        private Integer disGoodsId;
        private Integer nxGoodsId;
        private String orderStandard;
        private String currentQuotePrice;
        private String customerHistoryPrice;
        private String afterProcessOrderPrice;
        private String afterApplyDepPrice;
        private String finalNxDoPrice;
    }

    /**
     * 平台 assign 定价：复用 processOrderPrice + applyDepartmentGoodsPriceIfFound，
     * 再按「有效历史价 &gt; 有效当前报价」兜底，避免 0.1/空历史价覆盖有效报价。
     */
    private AssignPricingTrace applyPlatformAssignPricing(
            NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoods) {

        AssignPricingTrace trace = new AssignPricingTrace();
        trace.disGoodsId = disGoods.getNxDistributerGoodsId();
        trace.nxGoodsId = disGoods.getNxDgNxGoodsId();
        trace.orderStandard = order.getNxDoStandard();
        trace.currentQuotePrice = PlatformDisGoodsValidator.resolveCurrentQuotePrice(disGoods);
        trace.customerHistoryPrice = resolveCustomerHistoryPrice(order, disGoods);

        nxDepartmentOrdersService.processOrderPrice(order, disGoods);
        trace.afterProcessOrderPrice = order.getNxDoPrice();

        nxDepartmentOrdersService.applyDepartmentGoodsPriceIfFound(order, disGoods);
        trace.afterApplyDepPrice = order.getNxDoPrice();

        if (SalesPriceUtils.isValidSalesPrice(trace.customerHistoryPrice)) {
            order.setNxDoPrice(trace.customerHistoryPrice.trim());
        } else if (!SalesPriceUtils.isValidSalesPrice(order.getNxDoPrice())
                && SalesPriceUtils.isValidSalesPrice(trace.currentQuotePrice)) {
            order.setNxDoPrice(trace.currentQuotePrice);
        }

        recalculateAssignSubtotal(order);
        trace.finalNxDoPrice = order.getNxDoPrice();
        PlatformOrderPriceSupport.applyAssignReferencePrice(order);

        logger.info("[platformAssignPricing] disGoodsId={} nxGoodsId={} standard={} currentQuotePrice={} "
                        + "customerHistoryPrice={} afterProcessOrderPrice={} afterApplyDepPrice={} finalNxDoPrice={}",
                trace.disGoodsId, trace.nxGoodsId, trace.orderStandard,
                trace.currentQuotePrice, trace.customerHistoryPrice,
                trace.afterProcessOrderPrice, trace.afterApplyDepPrice, trace.finalNxDoPrice);
        return trace;
    }

    private String resolveCustomerHistoryPrice(
            NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoods) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", order.getNxDoDistributerId());
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoods.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        NxDepartmentDisGoodsEntity depGoods = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (depGoods == null) {
            map.put("standard", null);
            depGoods = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        }
        if (depGoods == null || depGoods.getNxDdgOrderPrice() == null) {
            return null;
        }
        return depGoods.getNxDdgOrderPrice().trim();
    }

    private void applyPlatformAssignCost(
            NxDepartmentOrdersEntity order, PlatformDisGoodsCostResolver.CostResolveResult costResult) {
        if (costResult == null || costResult.isCostMissing()) {
            return;
        }
        order.setNxDoCostPrice(costResult.getCostPrice());
        if (costResult.getCostPriceUpdate() != null) {
            order.setNxDoCostPriceUpdate(costResult.getCostPriceUpdate());
        }
        if (costResult.getCostPriceLevel() != null) {
            order.setNxDoCostPriceLevel(costResult.getCostPriceLevel());
        }
        recalculateCostSubtotal(order);
    }

    private void recalculateCostSubtotal(NxDepartmentOrdersEntity order) {
        if (!SalesPriceUtils.isValidSalesPrice(order.getNxDoCostPrice())) {
            return;
        }
        String multiplier = order.getNxDoWeight();
        if (StringUtils.isBlank(multiplier)) {
            multiplier = order.getNxDoQuantity();
        }
        if (StringUtils.isBlank(multiplier)) {
            return;
        }
        try {
            BigDecimal qty = new BigDecimal(multiplier.trim());
            BigDecimal price = new BigDecimal(order.getNxDoCostPrice().trim());
            order.setNxDoCostSubtotal(qty.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
        } catch (NumberFormatException e) {
            logger.warn("[recalculateCostSubtotal] 计算失败 qty={} costPrice={}", multiplier, order.getNxDoCostPrice());
        }
    }

    private void recalculateAssignSubtotal(NxDepartmentOrdersEntity order) {
        if (!SalesPriceUtils.isValidSalesPrice(order.getNxDoPrice())) {
            return;
        }
        String multiplier = order.getNxDoWeight();
        if (StringUtils.isBlank(multiplier)) {
            multiplier = order.getNxDoQuantity();
        }
        if (StringUtils.isBlank(multiplier)) {
            return;
        }
        try {
            BigDecimal qty = new BigDecimal(multiplier.trim());
            BigDecimal price = new BigDecimal(order.getNxDoPrice().trim());
            order.setNxDoSubtotal(qty.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
        } catch (NumberFormatException e) {
            logger.warn("[recalculateAssignSubtotal] 计算失败 qty={} price={}", multiplier, order.getNxDoPrice());
        }
    }

    private void assertValidAssignOrderPrice(NxDepartmentOrdersEntity order, AssignPricingTrace trace) {
        if (PlatformDisGoodsValidator.isValidOrderPrice(order.getNxDoPrice())) {
            return;
        }
        throw new IllegalArgumentException(PlatformDisGoodsValidator.MSG_INVALID_ORDER_PRICE_AFTER_PRICING
                + " [disGoodsId=" + trace.disGoodsId
                + ", nxGoodsId=" + trace.nxGoodsId
                + ", orderStandard=" + trace.orderStandard
                + ", currentQuotePrice=" + trace.currentQuotePrice
                + ", customerHistoryPrice=" + trace.customerHistoryPrice
                + ", afterProcessOrderPrice=" + trace.afterProcessOrderPrice
                + ", afterApplyDepPrice=" + trace.afterApplyDepPrice
                + ", finalNxDoPrice=" + trace.finalNxDoPrice + "]");
    }

    private void validateAssignRequest(PlatformAssignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (request.getDisGoodsId() == null) {
            throw new IllegalArgumentException("disGoodsId 不能为空");
        }
        if (request.getOperatorId() == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        if (StringUtils.isBlank(request.getSwitchScope())) {
            throw new IllegalArgumentException("switchScope 不能为空");
        }
        String scope = request.getSwitchScope().trim();
        if (!PlatformConstants.SWITCH_SCOPE_ORDER_ONLY.equals(scope)
                && !PlatformConstants.SWITCH_SCOPE_ORDER_AND_DEFAULT.equals(scope)) {
            throw new IllegalArgumentException("switchScope 仅支持 ORDER_ONLY 或 ORDER_AND_DEFAULT");
        }
        request.setSwitchScope(scope);
    }

    private void requireActiveMarketDepartment(Integer marketId, Integer departmentId) {
        NxMarketDepartmentEntity md = platformMarketDepartmentService.queryActive(marketId, departmentId);
        if (md == null) {
            throw new IllegalArgumentException("客户不属于该市场或未激活");
        }
    }

    private void accumulateDistributerSummary(
            Map<Integer, PlatformDistributerSummaryItem> summaryMap,
            PlatformOrderDetailRow row) {
        Integer disId = row.getAssignedDistributerId();
        PlatformDistributerSummaryItem item = summaryMap.get(disId);
        if (item == null) {
            item = new PlatformDistributerSummaryItem();
            item.setDistributerId(disId);
            NxDistributerEntity dis = nxDistributerService.queryObject(disId);
            if (dis != null) {
                item.setDistributerName(StringUtils.isNotBlank(dis.getNxDistributerShowName())
                        ? dis.getNxDistributerShowName()
                        : dis.getNxDistributerName());
            }
            item.setLineCount(0);
            item.setSubtotalSum(BigDecimal.ZERO);
            summaryMap.put(disId, item);
        }
        item.setLineCount(item.getLineCount() + 1);
        if (StringUtils.isNotBlank(row.getOrderSubtotal())) {
            try {
                item.setSubtotalSum(item.getSubtotalSum().add(new BigDecimal(row.getOrderSubtotal().trim())));
            } catch (NumberFormatException ignored) {
                // skip invalid subtotal
            }
        }
    }

    private List<Integer> parseOrderIds(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (StringUtils.isBlank(csv)) {
            return ids;
        }
        for (String part : csv.split(",")) {
            if (StringUtils.isNotBlank(part)) {
                ids.add(Integer.valueOf(part.trim()));
            }
        }
        return ids;
    }

    private BigDecimal parsePriceDecimal(String price) {
        if (StringUtils.isBlank(price)) {
            return null;
        }
        try {
            return new BigDecimal(price.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private PlatformSubmitLineResponse buildSubmitLineResponse(
            NxDepartmentOrdersEntity order, NxPlatformOrderAssignEntity assign) {
        PlatformSubmitLineResponse response = new PlatformSubmitLineResponse();
        response.setOrderId(order.getNxDepartmentOrdersId());
        response.setPlatformAssignId(assign.getNxPoaId());
        response.setAssignStatus(assign.getNxPoaAssignStatus());
        response.setAssignMode(assign.getNxPoaAssignMode());
        response.setNxDoDistributerId(order.getNxDoDistributerId());
        response.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        response.setNxDoCollaborativeNxDisId(order.getNxDoCollaborativeNxDisId());
        return response;
    }

    private void validateSubmitRequest(PlatformSubmitLineRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId 不能为空");
        }
        if (request.getNxGoodsId() == null) {
            throw new IllegalArgumentException("nxGoodsId 不能为空");
        }
        if (StringUtils.isBlank(request.getQuantity())) {
            throw new IllegalArgumentException("quantity 不能为空");
        }
    }

    private NxDepartmentOrdersEntity buildPendingPlatformOrder(
            PlatformSubmitLineRequest request,
            NxDepartmentEntity department,
            NxGoodsEntity goods,
            String goodsName) {

        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
        order.setNxDoNxGoodsId(request.getNxGoodsId());
        order.setNxDoNxGoodsFatherId(goods.getNxGoodsFatherId());
        order.setNxDoDisGoodsId(null);
        order.setNxDoDepDisGoodsId(null);
        order.setNxDoQuantity(request.getQuantity().trim());
        order.setNxDoStandard(StringUtils.isNotBlank(request.getStandard()) ? request.getStandard().trim() : "");
        order.setNxDoRemark(StringUtils.isNotBlank(request.getRemark()) ? request.getRemark().trim() : "");
        order.setNxDoGoodsName(truncateGoodsName(goodsName));
        order.setNxDoGoodsOriginalName(truncateGoodsName(goodsName));

        order.setNxDoDepartmentId(request.getDepartmentId());
        Integer depFatherId = department.getNxDepartmentFatherId();
        if (depFatherId == null || depFatherId <= 0) {
            depFatherId = request.getDepartmentId();
        }
        order.setNxDoDepartmentFatherId(depFatherId);

        order.setNxDoDistributerId(platformDistributerIdResolver.resolvePendingDistributerId(request.getMarketId()));
        order.setNxDoStatus(0);
        order.setNxDoOrderUserId(request.getOrderUserId());
        order.setNxDoCollaborativeNxDisId(-1);

        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoCostPriceLevel("1");
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoTodayOrder(nextTodayOrder(request.getDepartmentId()));

        return order;
    }

    private int nextTodayOrder(Integer departmentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("depId", departmentId);
        params.put("status", 3);
        params.put("todayOrder", 1);
        return nxDepartmentOrdersDao.queryDepOrdersAcount(params) + 1;
    }

    private String truncateGoodsName(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() <= NX_DO_GOODS_NAME_DB_MAX_CHARS) {
            return name;
        }
        return name.substring(0, NX_DO_GOODS_NAME_DB_MAX_CHARS);
    }
}
