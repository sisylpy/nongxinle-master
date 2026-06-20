package com.nongxinle.service.platform;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dto.platform.GbBillPaymentRecalcResult;
import com.nongxinle.dto.platform.LineAmountConfirmResult;
import com.nongxinle.dto.platform.customer.PlatformCartLineItem;
import com.nongxinle.dto.platform.customer.PlatformCartLineDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCartLineItem;
import com.nongxinle.dto.platform.customer.PlatformCartLineUpdateRequest;
import com.nongxinle.dto.platform.customer.PlatformCartListRequest;
import com.nongxinle.dto.platform.customer.PlatformCartListResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutLineItem;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewResponse;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbPlatformOrderBridgeService;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.utils.GbBillPlatformConstants;
import com.nongxinle.utils.PlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatMonth;
import static com.nongxinle.utils.DateUtils.formatWhatYear;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.DateUtils.getWeekOfYear;
import static com.nongxinle.utils.GbTypeUtils.getGbDepBillNew;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusGouwu;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusNew;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusGouwu;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusNew;
import static com.nongxinle.utils.ParseObject.myRandom;

/**
 * 购物车 checkout 主链：preview 分组金额；confirm 新建 bill 并挂本批临时行（bill 封口，不可追加新商品）。
 */
@Service
public class PlatformCartCheckoutServiceImpl implements PlatformCartCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCartCheckoutServiceImpl.class);
    private static final String CHECKOUT_NOTICE =
            "部分商品需称重确认，确认后将进入本单；称重完成后可能需补款（非追加新商品）";

    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private PlatformLineAmountConfirmService platformLineAmountConfirmService;
    @Autowired
    private PlatformOutstandingBillService platformOutstandingBillService;
    @Autowired
    private GbBillPaymentRecalcService gbBillPaymentRecalcService;
    @Autowired
    private GbPlatformOrderBridgeService gbPlatformOrderBridgeService;
    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private PlatformGbNxOrderLineService platformGbNxOrderLineService;

    @Override
    public PlatformCartListResponse listCartLines(PlatformCartListRequest request) {
        validateListRequest(request);
        platformMarketDepartmentService.ensureActiveForGbCustomer(request.getMarketId(), request.getGbDepartmentId());

        Map<String, Object> params = new HashMap<>();
        params.put("gbDepId", request.getGbDepartmentId());
        params.put("equalStatus", getNxOrderStatusGouwu());
        List<NxDepartmentOrdersEntity> nxOrders = nxDepartmentOrdersDao.queryDisOrdersByParams(params);

        List<PlatformCartLineItem> lines = new ArrayList<>();
        if (nxOrders != null) {
            for (NxDepartmentOrdersEntity nxOrder : nxOrders) {
                if (nxOrder == null || nxOrder.getNxDepartmentOrdersId() == null) {
                    continue;
                }
                lines.add(toCartLineItem(nxOrder, resolveGbOrder(nxOrder), resolvePriceConfirm(nxOrder, resolveGbOrder(nxOrder))));
            }
        }

        PlatformCartListResponse response = new PlatformCartListResponse();
        response.setGbDepartmentId(request.getGbDepartmentId());
        response.setLineCount(lines.size());
        response.setLines(lines);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCartLineItem updateCartLine(PlatformCartLineUpdateRequest request) {
        validateCartLineMutationRequest(request == null ? null : request.getMarketId(),
                request == null ? null : request.getGbDepartmentId(),
                request == null ? null : request.getNxOrderId());
        if (StringUtils.isBlank(request.getQuantity())) {
            throw new IllegalArgumentException("quantity 不能为空");
        }
        if (StringUtils.isBlank(request.getStandard())) {
            throw new IllegalArgumentException("standard 不能为空");
        }

        NxDepartmentOrdersEntity nxOrder = loadEditableCartLine(
                request.getMarketId(), request.getGbDepartmentId(), request.getNxOrderId());
        GbDepartmentOrdersEntity gbOrder = resolveGbOrder(nxOrder);

        String quantity = request.getQuantity().trim();
        String standard = request.getStandard().trim();
        nxOrder.setNxDoQuantity(quantity);
        nxOrder.setNxDoStandard(standard);
        if (request.getRemark() != null) {
            nxOrder.setNxDoRemark(request.getRemark().trim());
        }

        String priceConfirmStatus;
        if (PlatformCartLineSupport.hasCustomerSelectedSupplier(nxOrder)) {
            NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(nxOrder.getNxDoDisGoodsId());
            if (goods == null) {
                throw new IllegalStateException("批发商商品不存在: nxDoDisGoodsId=" + nxOrder.getNxDoDisGoodsId());
            }
            LineAmountConfirmResult confirm = platformLineAmountConfirmService.isLineAmountConfirmable(
                    quantity, standard, goods);
            priceConfirmStatus = confirm.getPriceConfirmStatus();
            applySupplierCartPrice(nxOrder, goods, confirm);
            if (gbOrder != null) {
                gbOrder.setGbDoQuantity(quantity);
                gbOrder.setGbDoStandard(standard);
                gbOrder.setGbDoPrintStandard(standard);
                if (request.getRemark() != null) {
                    gbOrder.setGbDoRemark(request.getRemark().trim());
                }
                gbOrder.setGbDoPriceConfirmStatus(priceConfirmStatus);
                applySupplierCartPriceToGb(gbOrder, confirm);
                gbDepartmentOrdersService.update(gbOrder);
            }
        } else {
            priceConfirmStatus = GbBillPlatformConstants.PRICE_CONFIRM_PENDING;
            nxOrder.setNxDoSubtotal("0");
            nxOrder.setNxDoCostSubtotal("0");
            nxOrder.setNxDoProfitSubtotal("0");
            nxOrder.setNxDoPrice(null);
            nxOrder.setNxDoWeight(null);
        }

        nxDepartmentOrdersService.update(nxOrder);
        nxOrder = nxDepartmentOrdersService.queryObject(nxOrder.getNxDepartmentOrdersId());
        if (gbOrder != null) {
            gbOrder = gbDepartmentOrdersService.queryObject(gbOrder.getGbDepartmentOrdersId());
        }
        log.info("[platform/cart/lines/update] nxOrderId={} priceConfirmStatus={}",
                nxOrder.getNxDepartmentOrdersId(), priceConfirmStatus);
        return toCartLineItem(nxOrder, gbOrder, priceConfirmStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartLine(PlatformCartLineDeleteRequest request) {
        validateCartLineMutationRequest(request == null ? null : request.getMarketId(),
                request == null ? null : request.getGbDepartmentId(),
                request == null ? null : request.getNxOrderId());

        NxDepartmentOrdersEntity nxOrder = loadEditableCartLine(
                request.getMarketId(), request.getGbDepartmentId(), request.getNxOrderId());
        GbDepartmentOrdersEntity gbOrder = resolveGbOrder(nxOrder);

        if (gbOrder != null && gbOrder.getGbDepartmentOrdersId() != null) {
            gbDepartmentOrdersService.delete(gbOrder.getGbDepartmentOrdersId());
        }
        nxDepartmentOrdersService.delete(nxOrder.getNxDepartmentOrdersId());
        log.info("[platform/cart/lines/delete] nxOrderId={}", nxOrder.getNxDepartmentOrdersId());
    }

    @Override
    public PlatformCheckoutPreviewResponse checkoutPreview(PlatformCheckoutPreviewRequest request) {
        List<ResolvedCartLine> resolved = loadAndValidateCartLines(request.getMarketId(), request.getGbDepartmentId(),
                request.getOrderIds(), false);

        List<PlatformCheckoutLineItem> confirmedLines = new ArrayList<>();
        List<PlatformCheckoutLineItem> pendingPriceLines = new ArrayList<>();
        BigDecimal knownTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (ResolvedCartLine line : resolved) {
            PlatformCheckoutLineItem item = toCheckoutLineItem(line, null);
            if (GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(line.priceConfirmStatus)) {
                confirmedLines.add(item);
                knownTotal = knownTotal.add(parseMoney(line.lineSubtotal));
            } else {
                pendingPriceLines.add(item);
            }
        }

        PlatformCheckoutPreviewResponse response = new PlatformCheckoutPreviewResponse();
        response.setConfirmedLines(confirmedLines);
        response.setPendingPriceLines(pendingPriceLines);
        response.setConfirmedItemCount(confirmedLines.size());
        response.setPendingPriceItemCount(pendingPriceLines.size());
        response.setKnownTotal(formatMoney(knownTotal));
        response.setPayAmount(formatMoney(knownTotal));
        response.setNotice(pendingPriceLines.isEmpty() ? null : CHECKOUT_NOTICE);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCheckoutConfirmResponse checkoutConfirm(PlatformCheckoutConfirmRequest request) {
        validateConfirmRequest(request);
        platformMarketDepartmentService.ensureActiveForGbCustomer(request.getMarketId(), request.getGbDepartmentId());

        String token = request.getCheckoutToken().trim();
        GbDepartmentBillEntity existing = gbDepartmentBillService.queryByPlatformSubmitToken(token);
        if (existing != null) {
            return buildConfirmResponse(existing, true);
        }

        platformOutstandingBillService.assertNotBlockedForNewSubmit(request.getGbDepartmentId());

        try {
            List<ResolvedCartLine> resolved = loadAndValidateCartLines(request.getMarketId(),
                    request.getGbDepartmentId(), request.getOrderIds(), true);

            GbDepartmentBillEntity bill = createPlatformCashBill(request, resolved.size());
            BigDecimal knownTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            int pendingPriceCount = 0;
            List<PlatformCheckoutLineItem> confirmLines = new ArrayList<>();

            for (ResolvedCartLine line : resolved) {
                finalizeCartLineForCheckout(line, bill.getGbDepartmentBillId(), request);
                if (GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(line.priceConfirmStatus)) {
                    knownTotal = knownTotal.add(parseMoney(line.lineSubtotal));
                } else {
                    pendingPriceCount++;
                }
                String assignStatus = PlatformCartLineSupport.hasCustomerSelectedSupplier(line.nxOrder)
                        ? PlatformConstants.ASSIGN_STATUS_ASSIGNED
                        : PlatformConstants.ASSIGN_STATUS_PENDING;
                confirmLines.add(toCheckoutLineItem(line, assignStatus));
            }

            bill.setGbDbKnownTotal(knownTotal);
            bill.setGbDbPendingItemCount(pendingPriceCount);
            bill.setGbDbPaidTotal(knownTotal);
            bill.setGbDbOrderAmount(resolved.size());
            gbDepartmentBillService.update(bill);

            assertAllLinesAttachedToBill(bill.getGbDepartmentBillId(), resolved.size());

            GbBillPaymentRecalcResult recalc = gbBillPaymentRecalcService.recalcBillPaymentState(bill.getGbDepartmentBillId());
            GbDepartmentBillEntity reloaded = gbDepartmentBillService.queryObject(bill.getGbDepartmentBillId());
            PlatformCheckoutConfirmResponse response = buildConfirmResponse(reloaded, false);
            response.setLines(confirmLines);
            enrichFromRecalc(response, recalc);
            log.info("[platform/cart/checkoutConfirm] billId={} payStatus={} knownTotal={} pendingPriceItemCount={}",
                    reloaded.getGbDepartmentBillId(),
                    response.getPayStatus(),
                    response.getKnownTotal(),
                    response.getPendingPriceItemCount());
            return response;
        } catch (DuplicateKeyException ex) {
            GbDepartmentBillEntity raced = gbDepartmentBillService.queryByPlatformSubmitToken(token);
            if (raced != null) {
                return buildConfirmResponse(raced, true);
            }
            throw ex;
        }
    }

    private void finalizeCartLineForCheckout(ResolvedCartLine line, Integer billId,
                                             PlatformCheckoutConfirmRequest request) {
        NxDepartmentOrdersEntity nxOrder = line.nxOrder;
        GbDepartmentOrdersEntity gbOrder = line.gbOrder;

        if (gbOrder == null) {
            gbOrder = createPlatformGbStubFromNxCartLine(nxOrder, line, request);
            line.gbOrder = gbOrder;
        }

        platformGbNxOrderLineService.formalizeCartLineAtCheckout(gbOrder, nxOrder);
        gbOrder = gbDepartmentOrdersService.queryObject(gbOrder.getGbDepartmentOrdersId());
        line.gbOrder = gbOrder;

        gbOrder.setGbDoBillId(billId);
        gbOrder.setGbDoPriceConfirmStatus(line.priceConfirmStatus);
        gbOrder.setGbDoStatus(getGbOrderStatusNew());
        if (GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(line.priceConfirmStatus)) {
            gbOrder.setGbDoSubtotal(formatMoney(parseMoney(line.lineSubtotal)));
        } else {
            gbOrder.setGbDoSubtotal("0");
        }
        gbDepartmentOrdersService.update(gbOrder);

        nxOrder.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(nxOrder);

        if (PlatformCartLineSupport.hasCustomerSelectedSupplier(nxOrder)) {
            if (nxPlatformOrderAssignDao.queryByOrderId(nxOrder.getNxDepartmentOrdersId()) == null) {
                gbPlatformOrderBridgeService.onNxOrderCreatedFromGb(gbOrder, nxOrder);
            }
        } else {
            savePendingAssign(request.getMarketId(), nxOrder, gbOrder);
        }
    }

    private void savePendingAssign(Integer marketId, NxDepartmentOrdersEntity nxOrder,
                                   GbDepartmentOrdersEntity gbOrder) {
        NxPlatformOrderAssignEntity assign = new NxPlatformOrderAssignEntity();
        assign.setNxPoaMarketId(marketId);
        assign.setNxPoaOrderId(nxOrder.getNxDepartmentOrdersId());
        assign.setNxPoaDepartmentId(gbOrder.getGbDoDepartmentId());
        assign.setNxPoaNxGoodsId(nxOrder.getNxDoNxGoodsId());
        assign.setNxPoaAssignStatus(PlatformConstants.ASSIGN_STATUS_PENDING);
        assign.setNxPoaAssignMode(PlatformConstants.ASSIGN_MODE_PLATFORM);
        assign.setNxPoaSourceType(PlatformConstants.SOURCE_TYPE_GB);
        assign.setNxPoaGbDepartmentId(gbOrder.getGbDoDepartmentId());
        assign.setNxPoaGbDepartmentFatherId(gbOrder.getGbDoDepartmentFatherId());
        assign.setNxPoaGbDepartmentOrderId(gbOrder.getGbDepartmentOrdersId());
        nxPlatformOrderAssignDao.save(assign);
    }

    private GbDepartmentOrdersEntity createPlatformGbStubFromNxCartLine(
            NxDepartmentOrdersEntity nxOrder, ResolvedCartLine line, PlatformCheckoutConfirmRequest request) {

        Integer depFatherId = request.getGbDepartmentFatherId();
        if (depFatherId == null || depFatherId <= 0) {
            depFatherId = nxOrder.getNxDoGbDepartmentFatherId();
        }
        if (depFatherId == null || depFatherId <= 0) {
            depFatherId = request.getGbDepartmentId();
        }

        GbDepartmentOrdersEntity gbOrder = new GbDepartmentOrdersEntity();
        gbOrder.setGbDoDepartmentId(request.getGbDepartmentId());
        gbOrder.setGbDoDepartmentFatherId(depFatherId);
        gbOrder.setGbDoToDepartmentId(request.getGbDepartmentId());
        gbOrder.setGbDoDistributerId(request.getGbDistributerId() != null
                ? request.getGbDistributerId()
                : nxOrder.getNxDoGbDistributerId());
        gbOrder.setGbDoOrderUserId(request.getGbOrderUserId());
        gbOrder.setGbDoNxGoodsId(nxOrder.getNxDoNxGoodsId());
        gbOrder.setGbDoNxGoodsFatherId(nxOrder.getNxDoNxGoodsFatherId());
        gbOrder.setGbDoQuantity(nxOrder.getNxDoQuantity());
        gbOrder.setGbDoStandard(nxOrder.getNxDoStandard());
        gbOrder.setGbDoRemark(nxOrder.getNxDoRemark());
        gbOrder.setGbDoApplyArriveDate(request.getDeliveryDate());
        gbOrder.setGbDoApplyDate(formatWhatDay(0));
        gbOrder.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbOrder.setGbDoOrderType(5);
        gbOrder.setGbDoCostPriceLevel(1);
        gbOrder.setGbDoPrintStandard(nxOrder.getNxDoStandard());
        gbOrder.setGbDoDsStandardScale("-1");
        gbOrder.setGbDoPriceConfirmStatus(line.priceConfirmStatus);
        gbOrder.setGbDoStatus(getGbOrderStatusNew());
        gbOrder.setGbDoNxDepartmentOrderId(nxOrder.getNxDepartmentOrdersId());
        if (GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(line.priceConfirmStatus)) {
            gbOrder.setGbDoSubtotal(formatMoney(parseMoney(line.lineSubtotal)));
        } else {
            gbOrder.setGbDoSubtotal("0");
        }
        gbDepartmentOrdersService.save(gbOrder);

        nxOrder.setNxDoGbDepartmentOrderId(gbOrder.getGbDepartmentOrdersId());
        nxDepartmentOrdersService.update(nxOrder);
        return gbOrder;
    }

    private GbDepartmentBillEntity createPlatformCashBill(PlatformCheckoutConfirmRequest request, int lineCount) {
        Integer depFatherId = request.getGbDepartmentFatherId();
        if (depFatherId == null || depFatherId <= 0) {
            depFatherId = request.getGbDepartmentId();
        }

        GbDepartmentBillEntity bill = new GbDepartmentBillEntity();
        bill.setGbDbDepId(request.getGbDepartmentId());
        bill.setGbDbDepFatherId(depFatherId);
        bill.setGbDbDisId(request.getGbDistributerId());
        bill.setGbDbBillSource(GbBillPlatformConstants.BILL_SOURCE_PLATFORM_CASH);
        bill.setGbDbPlatformSubmitToken(request.getCheckoutToken().trim());
        bill.setGbDbStatus(getGbDepBillNew());
        bill.setGbDbPaidTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        bill.setGbDbSupplementDue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        bill.setGbDbPendingItemCount(0);
        bill.setGbDbDate(formatWhatDay(0));
        bill.setGbDbTime(formatWhatYearDayTime(0));
        bill.setGbDbMonth(formatWhatMonth(0));
        bill.setGbDbYear(formatWhatYear(0));
        bill.setGbDbWeek(getWeekOfYear(0).toString());
        bill.setGbDbDay(getWeek(0));
        bill.setGbDbTradeNo(formatWhatDay(0) + myRandom());
        bill.setGbDbPrintTimes(0);
        bill.setGbDbIssueOrderType(5);
        bill.setGbDbOrderAmount(lineCount);
        gbDepartmentBillService.save(bill);
        return bill;
    }

    private List<ResolvedCartLine> loadAndValidateCartLines(Integer marketId, Integer gbDepartmentId,
                                                            List<Integer> orderIds, boolean recalcPrice) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException("orderIds 不能为空");
        }
        Set<Integer> uniqueIds = new HashSet<>(orderIds);
        if (uniqueIds.size() != orderIds.size()) {
            throw new IllegalArgumentException("orderIds 存在重复");
        }

        Map<Integer, ResolvedCartLine> loaded = new LinkedHashMap<>();
        for (Integer orderId : orderIds) {
            NxDepartmentOrdersEntity nxOrder = nxDepartmentOrdersService.queryObject(orderId);
            if (nxOrder == null) {
                throw new IllegalArgumentException("购物车行不存在: orderId=" + orderId);
            }
            if (!gbDepartmentId.equals(nxOrder.getNxDoGbDepartmentId())) {
                throw new IllegalArgumentException("订单不属于当前饭店: orderId=" + orderId);
            }
            if (!getNxOrderStatusGouwu().equals(nxOrder.getNxDoStatus())) {
                throw new IllegalArgumentException("订单不是购物车状态: orderId=" + orderId);
            }
            GbDepartmentOrdersEntity gbOrder = resolveGbOrder(nxOrder);
            if (gbOrder != null && gbOrder.getGbDoBillId() != null) {
                throw new IllegalArgumentException("订单已挂 bill: orderId=" + orderId);
            }
            ResolvedCartLine line = new ResolvedCartLine();
            line.nxOrder = nxOrder;
            line.gbOrder = gbOrder;
            if (recalcPrice) {
                line.priceConfirmStatus = resolvePriceConfirmRecalc(nxOrder, gbOrder);
                line.lineSubtotal = resolveLineSubtotal(line.priceConfirmStatus, nxOrder, gbOrder);
            } else {
                line.priceConfirmStatus = resolvePriceConfirm(nxOrder, gbOrder);
                line.lineSubtotal = resolveLineSubtotal(line.priceConfirmStatus, nxOrder, gbOrder);
            }
            loaded.put(orderId, line);
        }
        return new ArrayList<>(loaded.values());
    }

    private String resolvePriceConfirmRecalc(NxDepartmentOrdersEntity nxOrder, GbDepartmentOrdersEntity gbOrder) {
        if (PlatformCartLineSupport.hasCustomerSelectedSupplier(nxOrder)) {
            NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(nxOrder.getNxDoDisGoodsId());
            LineAmountConfirmResult confirm = platformLineAmountConfirmService.isLineAmountConfirmable(
                    nxOrder.getNxDoQuantity(), nxOrder.getNxDoStandard(), goods);
            return confirm.getPriceConfirmStatus();
        }
        return GbBillPlatformConstants.PRICE_CONFIRM_PENDING;
    }

    private String resolvePriceConfirm(NxDepartmentOrdersEntity nxOrder, GbDepartmentOrdersEntity gbOrder) {
        if (gbOrder != null && StringUtils.isNotBlank(gbOrder.getGbDoPriceConfirmStatus())) {
            return gbOrder.getGbDoPriceConfirmStatus();
        }
        return GbBillPlatformConstants.PRICE_CONFIRM_PENDING;
    }

    private String resolveLineSubtotal(String priceConfirmStatus, NxDepartmentOrdersEntity nxOrder,
                                         GbDepartmentOrdersEntity gbOrder) {
        if (!GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(priceConfirmStatus)) {
            return null;
        }
        if (gbOrder != null && StringUtils.isNotBlank(gbOrder.getGbDoSubtotal())) {
            return formatMoney(parseMoney(gbOrder.getGbDoSubtotal()));
        }
        if (StringUtils.isNotBlank(nxOrder.getNxDoSubtotal())) {
            return formatMoney(parseMoney(nxOrder.getNxDoSubtotal()));
        }
        return "0.00";
    }

    private void assertAllLinesAttachedToBill(Integer billId, int expectedLineCount) {
        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<GbDepartmentOrdersEntity> gbLines = gbDepartmentOrdersService.queryDisOrdersByParams(map);
        int actual = gbLines == null ? 0 : gbLines.size();
        if (actual != expectedLineCount) {
            throw new IllegalStateException(
                    "bill 挂行数不一致: billId=" + billId + " expected=" + expectedLineCount + " actual=" + actual);
        }
    }

    private GbDepartmentOrdersEntity resolveGbOrder(NxDepartmentOrdersEntity nxOrder) {
        if (nxOrder.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbOrder = gbDepartmentOrdersService.queryObject(nxOrder.getNxDoGbDepartmentOrderId());
            if (gbOrder != null) {
                return gbOrder;
            }
        }
        return gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxOrder.getNxDepartmentOrdersId());
    }

    private NxDepartmentOrdersEntity loadEditableCartLine(Integer marketId, Integer gbDepartmentId, Integer nxOrderId) {
        platformMarketDepartmentService.ensureActiveForGbCustomer(marketId, gbDepartmentId);
        NxDepartmentOrdersEntity nxOrder = nxDepartmentOrdersService.queryObject(nxOrderId);
        if (nxOrder == null) {
            throw new IllegalArgumentException("购物车行不存在: nxOrderId=" + nxOrderId);
        }
        if (!gbDepartmentId.equals(nxOrder.getNxDoGbDepartmentId())) {
            throw new IllegalArgumentException("购物车行不属于当前饭店: nxOrderId=" + nxOrderId);
        }
        if (!getNxOrderStatusGouwu().equals(nxOrder.getNxDoStatus())) {
            throw new IllegalArgumentException("仅可修改/删除购物车临时行: nxOrderId=" + nxOrderId);
        }
        if (nxOrder.getNxDoBillId() != null) {
            throw new IllegalArgumentException("订单已挂 bill，不可修改/删除: nxOrderId=" + nxOrderId);
        }
        if (nxPlatformOrderAssignDao.queryByOrderId(nxOrderId) != null) {
            throw new IllegalArgumentException("订单已 checkout，不可修改/删除: nxOrderId=" + nxOrderId);
        }
        GbDepartmentOrdersEntity gbOrder = resolveGbOrder(nxOrder);
        if (gbOrder != null) {
            if (gbOrder.getGbDoBillId() != null) {
                throw new IllegalArgumentException("GB 行已挂 bill，不可修改/删除: nxOrderId=" + nxOrderId);
            }
            if (!getGbOrderStatusGouwu().equals(gbOrder.getGbDoStatus())) {
                throw new IllegalArgumentException("GB 行已正式化，不可修改/删除: nxOrderId=" + nxOrderId);
            }
        }
        return nxOrder;
    }

    private void validateCartLineMutationRequest(Integer marketId, Integer gbDepartmentId, Integer nxOrderId) {
        if (marketId == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (gbDepartmentId == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
        if (nxOrderId == null) {
            throw new IllegalArgumentException("nxOrderId 不能为空");
        }
    }

    private void applySupplierCartPrice(NxDepartmentOrdersEntity nxOrder, NxDistributerGoodsEntity goods,
                                        LineAmountConfirmResult confirm) {
        if (confirm.isConfirmable()) {
            nxOrder.setNxDoPrice(confirm.getResolvedUnitPrice().toPlainString());
            nxOrder.setNxDoWeight(nxOrder.getNxDoQuantity());
            nxOrder.setNxDoSubtotal(confirm.getLineSubtotal().toPlainString());
            nxOrder.setNxDoCostPriceLevel("1");
            if (StringUtils.isNotBlank(goods.getNxDgBuyingPriceOne())) {
                nxOrder.setNxDoCostPrice(goods.getNxDgBuyingPriceOne());
                nxOrder.setNxDoCostPriceUpdate(goods.getNxDgBuyingPriceOneUpdate());
            }
            BigDecimal buySub = parseDecimal(nxOrder.getNxDoCostPrice())
                    .multiply(parseDecimal(nxOrder.getNxDoQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            nxOrder.setNxDoCostSubtotal(buySub.toPlainString());
        } else {
            nxOrder.setNxDoPrice(null);
            nxOrder.setNxDoWeight(null);
            nxOrder.setNxDoSubtotal("0");
            nxOrder.setNxDoCostSubtotal("0");
            nxOrder.setNxDoProfitSubtotal("0");
        }
    }

    private void applySupplierCartPriceToGb(GbDepartmentOrdersEntity gbOrder, LineAmountConfirmResult confirm) {
        if (confirm.isConfirmable()) {
            gbOrder.setGbDoPrice(confirm.getResolvedUnitPrice().toPlainString());
            gbOrder.setGbDoWeight(gbOrder.getGbDoQuantity());
            gbOrder.setGbDoSubtotal(confirm.getLineSubtotal().toPlainString());
        } else {
            gbOrder.setGbDoPrice(null);
            gbOrder.setGbDoWeight(null);
            gbOrder.setGbDoSubtotal("0");
        }
    }

    private static BigDecimal parseDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private PlatformCartLineItem toCartLineItem(NxDepartmentOrdersEntity nxOrder, GbDepartmentOrdersEntity gbOrder,
                                                String priceConfirmStatus) {
        PlatformCartLineItem item = new PlatformCartLineItem();
        item.setNxOrderId(nxOrder.getNxDepartmentOrdersId());
        if (gbOrder != null) {
            item.setGbOrderId(gbOrder.getGbDepartmentOrdersId());
        }
        item.setNxGoodsId(nxOrder.getNxDoNxGoodsId());
        item.setNxDistributerId(nxOrder.getNxDoDistributerId());
        item.setNxDistributerGoodsId(nxOrder.getNxDoDisGoodsId());
        item.setGoodsName(nxOrder.getNxDoGoodsName());
        item.setQuantity(nxOrder.getNxDoQuantity());
        item.setStandard(nxOrder.getNxDoStandard());
        item.setPriceConfirmStatus(priceConfirmStatus);
        item.setLineSubtotal(resolveLineSubtotal(priceConfirmStatus, nxOrder, gbOrder));
        return item;
    }

    private PlatformCheckoutLineItem toCheckoutLineItem(ResolvedCartLine line, String assignStatus) {
        PlatformCheckoutLineItem item = new PlatformCheckoutLineItem();
        item.setNxOrderId(line.nxOrder.getNxDepartmentOrdersId());
        if (line.gbOrder != null) {
            item.setGbOrderId(line.gbOrder.getGbDepartmentOrdersId());
        }
        item.setNxGoodsId(line.nxOrder.getNxDoNxGoodsId());
        item.setNxDistributerId(line.nxOrder.getNxDoDistributerId());
        item.setNxDistributerGoodsId(line.nxOrder.getNxDoDisGoodsId());
        item.setGoodsName(line.nxOrder.getNxDoGoodsName());
        item.setQuantity(line.nxOrder.getNxDoQuantity());
        item.setStandard(line.nxOrder.getNxDoStandard());
        item.setPriceConfirmStatus(line.priceConfirmStatus);
        item.setLineSubtotal(line.lineSubtotal);
        item.setAssignStatus(assignStatus);
        return item;
    }

    private PlatformCheckoutConfirmResponse buildConfirmResponse(GbDepartmentBillEntity bill, boolean idempotent) {
        PlatformCheckoutConfirmResponse response = new PlatformCheckoutConfirmResponse();
        response.setBillId(bill.getGbDepartmentBillId());
        response.setBillTradeNo(bill.getGbDbTradeNo());
        response.setKnownTotal(formatMoney(bill.getGbDbKnownTotal()));
        response.setPaidTotal(formatMoney(bill.getGbDbPaidTotal()));
        response.setPendingPriceItemCount(bill.getGbDbPendingItemCount());
        response.setSupplementDue(formatMoney(bill.getGbDbSupplementDue()));
        response.setPayStatus(bill.getGbDbPayStatus());
        response.setIdempotent(idempotent);
        response.setMessage(buildMessage(bill));
        return response;
    }

    private void enrichFromRecalc(PlatformCheckoutConfirmResponse response, GbBillPaymentRecalcResult recalc) {
        if (recalc == null || recalc.isNoOp()) {
            return;
        }
        response.setPayStatus(recalc.getPayStatus());
        response.setKnownTotal(formatMoney(recalc.getKnownTotal()));
        response.setPaidTotal(formatMoney(recalc.getPaidTotal()));
        response.setSupplementDue(formatMoney(recalc.getSupplementDue()));
        response.setPendingPriceItemCount(recalc.getPendingItemCount());
        response.setMessage(buildMessageFromRecalc(recalc));
    }

    private String buildMessage(GbDepartmentBillEntity bill) {
        if (GbBillPlatformConstants.PAY_STATUS_NONE.equals(bill.getGbDbPayStatus())) {
            return "采购单已生成，等待确认价格";
        }
        if (bill.getGbDbPendingItemCount() != null && bill.getGbDbPendingItemCount() > 0) {
            return "已知价部分已确认；" + bill.getGbDbPendingItemCount() + " 件商品待确认价格";
        }
        return "采购单已生成";
    }

    private String buildMessageFromRecalc(GbBillPaymentRecalcResult recalc) {
        GbDepartmentBillEntity pseudo = new GbDepartmentBillEntity();
        pseudo.setGbDbPayStatus(recalc.getPayStatus());
        pseudo.setGbDbPendingItemCount(recalc.getPendingItemCount());
        return buildMessage(pseudo);
    }

    private void validateListRequest(PlatformCartListRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getGbDepartmentId() == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
    }

    private void validateConfirmRequest(PlatformCheckoutConfirmRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        if (StringUtils.isBlank(request.getCheckoutToken())) {
            throw new IllegalArgumentException("checkoutToken 不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getGbDepartmentId() == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
        if (request.getGbDistributerId() == null) {
            throw new IllegalArgumentException("gbDistributerId 不能为空");
        }
        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new IllegalArgumentException("orderIds 不能为空");
        }
    }

    private static BigDecimal parseMoney(String value) {
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private static BigDecimal parseMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatMoney(BigDecimal value) {
        return parseMoney(value).toPlainString();
    }

    private static final class ResolvedCartLine {
        private NxDepartmentOrdersEntity nxOrder;
        private GbDepartmentOrdersEntity gbOrder;
        private String priceConfirmStatus;
        private String lineSubtotal;
    }
}
