package com.nongxinle.service.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;
import com.nongxinle.dao.PlatformCheckoutPaymentDao;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPayRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPayResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentCancelRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewResponse;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.PlatformCheckoutPaymentEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.PlatformConstants.DEFAULT_CHECKOUT_WECHAT_NOTIFY_URL;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusGouwu;

@Service
public class PlatformCheckoutPaymentServiceImpl implements PlatformCheckoutPaymentService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCheckoutPaymentServiceImpl.class);

    @Value("${platform.checkout.wechat.notify-url:" + DEFAULT_CHECKOUT_WECHAT_NOTIFY_URL + "}")
    private String wechatNotifyUrl;

    @Autowired
    private PlatformCheckoutPaymentDao platformCheckoutPaymentDao;
    @Autowired
    private PlatformCartCheckoutService platformCartCheckoutService;
    @Autowired
    private PlatformOutstandingBillService platformOutstandingBillService;
    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;
    @Autowired
    private PlatformCheckoutPaymentLockService platformCheckoutPaymentLockService;
    @Autowired
    private PlatformCheckoutPaymentLifecycleService platformCheckoutPaymentLifecycleService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private SysCityMarketService sysCityMarketService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCheckoutPayResponse checkoutPay(PlatformCheckoutPayRequest request) {
        validatePayRequest(request);
        platformMarketDepartmentService.ensureActiveForGbCustomer(request.getMarketId(), request.getGbDepartmentId());
        platformCheckoutPaymentLifecycleService.releaseExpiredPendingPayments(request.getGbDepartmentId());

        String token = request.getCheckoutToken().trim();
        PlatformCheckoutPaymentEntity existingPayment = platformCheckoutPaymentDao.queryByCheckoutToken(token);
        if (existingPayment != null) {
            existingPayment = platformCheckoutPaymentLifecycleService.refreshAndCloseIfExpired(existingPayment);
            return buildPayResponseFromExisting(existingPayment, request.getMarketId());
        }

        GbDepartmentBillEntity existingBill = gbDepartmentBillService.queryByPlatformSubmitToken(token);
        if (existingBill != null) {
            return buildPayResponseFromBill(existingBill, token, true);
        }

        platformOutstandingBillService.assertNotBlockedForNewSubmit(request.getGbDepartmentId());
        platformCheckoutPaymentLockService.assertOrderIdsNotLockedByPendingPayment(
                request.getGbDepartmentId(), request.getOrderIds());

        PlatformCheckoutPreviewResponse preview = platformCartCheckoutService.checkoutPreview(toPreviewRequest(request));
        BigDecimal knownTotal = parseMoney(preview.getKnownTotal());
        if (knownTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("knownTotal 必须大于 0 才可发起微信支付");
        }

        String outTradeNo = buildCheckoutOutTradeNo();
        PlatformCheckoutPaymentEntity payment = buildPendingPaymentEntity(request, preview, token, outTradeNo);

        try {
            platformCheckoutPaymentDao.save(payment);
        } catch (DuplicateKeyException ex) {
            PlatformCheckoutPaymentEntity raced = platformCheckoutPaymentDao.queryByCheckoutToken(token);
            if (raced != null) {
                return buildPayResponseFromExisting(raced, request.getMarketId());
            }
            throw ex;
        }

        Map<String, String> wxPayParams = createOrReuseWxPayParams(payment, request.getMarketId());
        payment.setPcpWxPrepayId(wxPayParams.get("prepayId"));
        payment.setPcpUpdatedAt(formatWhatYearDayTime(0));
        platformCheckoutPaymentDao.update(payment);

        log.info("[platform/cart/checkoutPay] paymentId={} outTradeNo={} knownTotal={} pendingPriceItemCount={}",
                payment.getPcpId(), payment.getPcpOutTradeNo(), formatMoney(knownTotal),
                payment.getPcpPendingPriceItemCount());
        return buildPendingPayResponse(payment, wxPayParams, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createPaymentIntentSnapshot(PlatformCheckoutPayRequest request) {
        validatePayRequest(request);
        platformMarketDepartmentService.ensureActiveForGbCustomer(request.getMarketId(), request.getGbDepartmentId());
        platformCheckoutPaymentLifecycleService.releaseExpiredPendingPayments(request.getGbDepartmentId());

        String token = request.getCheckoutToken().trim();
        PlatformCheckoutPaymentEntity existingPayment = platformCheckoutPaymentDao.queryByCheckoutToken(token);
        if (existingPayment != null) {
            existingPayment = platformCheckoutPaymentLifecycleService.refreshAndCloseIfExpired(existingPayment);
            if (platformCheckoutPaymentLifecycleService.isActiveLock(existingPayment)) {
                return existingPayment.getPcpId();
            }
            throw new IllegalArgumentException("checkoutToken 已失效（" + existingPayment.getPcpStatus()
                    + "），请使用新的 checkoutToken");
        }

        platformOutstandingBillService.assertNotBlockedForNewSubmit(request.getGbDepartmentId());
        platformCheckoutPaymentLockService.assertOrderIdsNotLockedByPendingPayment(
                request.getGbDepartmentId(), request.getOrderIds());

        PlatformCheckoutPreviewResponse preview = platformCartCheckoutService.checkoutPreview(toPreviewRequest(request));
        BigDecimal knownTotal = parseMoney(preview.getKnownTotal());
        if (knownTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("knownTotal 必须大于 0 才可发起 checkout 支付");
        }

        PlatformCheckoutPaymentEntity payment = buildPendingPaymentEntity(
                request, preview, token, buildCheckoutOutTradeNo());
        platformCheckoutPaymentDao.save(payment);
        return payment.getPcpId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCheckoutPaymentStatusResponse cancelPayment(PlatformCheckoutPaymentCancelRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        if (request.getGbDepartmentId() == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
        PlatformCheckoutPaymentEntity payment = loadPaymentForCancel(request);
        payment = platformCheckoutPaymentLifecycleService.refreshAndCloseIfExpired(payment);

        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getPcpStatus())) {
            throw new IllegalArgumentException("支付已成功，不可取消");
        }
        if (!GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())) {
            return buildStatusResponse(payment);
        }
        if (!platformCheckoutPaymentLifecycleService.isActiveLock(payment)) {
            platformCheckoutPaymentLifecycleService.closeIfPending(
                    payment.getPcpId(), GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED);
            return buildStatusResponse(platformCheckoutPaymentDao.queryObject(payment.getPcpId()));
        }

        boolean closed = platformCheckoutPaymentLifecycleService.closeIfPending(
                payment.getPcpId(), GbBillPlatformConstants.PAYMENT_STATUS_CANCELLED);
        if (!closed) {
            payment = platformCheckoutPaymentDao.queryObject(payment.getPcpId());
            return buildStatusResponse(payment);
        }
        log.info("[platform/cart/checkoutPay/cancel] paymentId={}", payment.getPcpId());
        return buildStatusResponse(platformCheckoutPaymentDao.queryObject(payment.getPcpId()));
    }

    @Override
    public PlatformCheckoutPaymentStatusResponse queryPaymentStatus(PlatformCheckoutPaymentStatusRequest request) {
        PlatformCheckoutPaymentEntity payment = loadPaymentForStatusQuery(request);
        platformCheckoutPaymentLifecycleService.releaseExpiredPendingPayments(payment.getPcpGbDepartmentId());
        payment = platformCheckoutPaymentLifecycleService.refreshAndCloseIfExpired(payment);
        return buildStatusResponse(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWechatNotify(String notifyXml) {
        try {
            Map<String, String> notifyData = WXPayUtil.xmlToMap(notifyXml);
            String outTradeNo = notifyData.get("out_trade_no");
            PlatformCheckoutPaymentEntity payment = platformCheckoutPaymentDao.queryByOutTradeNo(outTradeNo);
            if (payment == null) {
                log.warn("[platform/cart/checkoutPayNotify] unknown outTradeNo={}", outTradeNo);
                return successXml();
            }

            WXPayConfig config = PlatformWechatPayHelper.resolvePayConfig(payment.getPcpMarketId(), sysCityMarketService);
            if (!PlatformWechatPayHelper.verifyNotifySignature(notifyData, config)) {
                return failXml("签名失败");
            }
            validateNotifyMerchant(notifyData, config);

            if (!"SUCCESS".equals(notifyData.get("return_code"))
                    || !"SUCCESS".equals(notifyData.get("result_code"))) {
                markFailedIfPending(payment, notifyXml);
                return successXml();
            }

            payment = platformCheckoutPaymentLifecycleService.refreshAndCloseIfExpired(payment);
            if (!GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())
                    && !(GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getPcpStatus())
                    && payment.getPcpBillId() != null)) {
                log.warn("[platform/cart/checkoutPayNotify] ignore notify for status={} paymentId={}",
                        payment.getPcpStatus(), payment.getPcpId());
                return successXml();
            }

            processPaymentSuccess(payment, notifyData, notifyXml, config);
            return successXml();
        } catch (Exception ex) {
            log.error("[platform/cart/checkoutPayNotify] failed", ex);
            return failXml(ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCheckoutPaymentStatusResponse completePaymentSuccessForRunner(Integer paymentId,
                                                                               String transactionId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId 不能为空");
        }
        PlatformCheckoutPaymentEntity payment = platformCheckoutPaymentDao.queryObject(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("payment 不存在: " + paymentId);
        }
        Map<String, String> fakeNotify = new HashMap<>();
        fakeNotify.put("out_trade_no", payment.getPcpOutTradeNo());
        fakeNotify.put("transaction_id", StringUtils.isBlank(transactionId) ? "RUNNER_TXN_" + paymentId : transactionId);
        fakeNotify.put("total_fee", toWxTotalFee(payment.getPcpKnownTotal()));
        processPaymentSuccess(payment, fakeNotify, "RUNNER_NOTIFY", null);
        PlatformCheckoutPaymentEntity reloaded = platformCheckoutPaymentDao.queryObject(paymentId);
        return buildStatusResponse(reloaded);
    }

    private PlatformCheckoutPaymentEntity buildPendingPaymentEntity(PlatformCheckoutPayRequest request,
                                                                    PlatformCheckoutPreviewResponse preview,
                                                                    String token,
                                                                    String outTradeNo) {
        PlatformCheckoutPaymentEntity payment = new PlatformCheckoutPaymentEntity();
        payment.setPcpCheckoutToken(token);
        payment.setPcpMarketId(request.getMarketId());
        payment.setPcpGbDepartmentId(request.getGbDepartmentId());
        payment.setPcpGbDepartmentFatherId(request.getGbDepartmentFatherId());
        payment.setPcpGbDistributerId(request.getGbDistributerId());
        payment.setPcpGbOrderUserId(request.getGbOrderUserId());
        payment.setPcpDeliveryDate(request.getDeliveryDate());
        payment.setPcpRemark(request.getRemark());
        payment.setPcpOrderIdsJson(JSON.toJSONString(new ArrayList<>(new HashSet<>(request.getOrderIds()))));
        payment.setPcpKnownTotal(parseMoney(preview.getKnownTotal()));
        payment.setPcpPendingPriceItemCount(preview.getPendingPriceItemCount());
        payment.setPcpOutTradeNo(outTradeNo);
        payment.setPcpOpenId(request.getOpenId().trim());
        payment.setPcpStatus(GbBillPlatformConstants.PAYMENT_STATUS_PENDING);
        payment.setPcpExpireAt(platformCheckoutPaymentLifecycleService.computeExpireAt());
        payment.setPcpCreatedAt(formatWhatYearDayTime(0));
        payment.setPcpUpdatedAt(formatWhatYearDayTime(0));
        return payment;
    }

    private PlatformCheckoutPaymentEntity loadPaymentForCancel(PlatformCheckoutPaymentCancelRequest request) {
        PlatformCheckoutPaymentEntity payment = null;
        if (request.getPaymentId() != null) {
            payment = platformCheckoutPaymentDao.queryObject(request.getPaymentId());
        } else if (StringUtils.isNotBlank(request.getOutTradeNo())) {
            payment = platformCheckoutPaymentDao.queryByOutTradeNo(request.getOutTradeNo().trim());
        }
        if (payment == null) {
            throw new IllegalArgumentException("payment 不存在");
        }
        if (!request.getGbDepartmentId().equals(payment.getPcpGbDepartmentId())) {
            throw new IllegalArgumentException("payment 不属于当前饭店");
        }
        return payment;
    }

    private void processPaymentSuccess(PlatformCheckoutPaymentEntity payment,
                                       Map<String, String> notifyData,
                                       String notifyXml,
                                       WXPayConfig config) {
        payment = platformCheckoutPaymentDao.queryObject(payment.getPcpId());
        if (payment == null) {
            throw new IllegalStateException("payment 不存在");
        }

        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getPcpStatus())
                && payment.getPcpBillId() != null) {
            log.info("[platform/cart/checkoutPayFinalize] idempotent paymentId={} billId={}",
                    payment.getPcpId(), payment.getPcpBillId());
            return;
        }

        validateNotifyOutTradeNo(notifyData, payment);
        validateNotifyAmount(notifyData, payment);
        assertSnapshotOrdersStillCart(payment);

        GbDepartmentBillEntity existingBill =
                gbDepartmentBillService.queryByPlatformSubmitToken(payment.getPcpCheckoutToken());
        if (existingBill != null) {
            linkPaymentFinalized(payment, existingBill.getGbDepartmentBillId(),
                    notifyData.get("transaction_id"), notifyXml);
            return;
        }

        if (!GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())) {
            throw new IllegalStateException("payment 状态不可 finalize: " + payment.getPcpStatus());
        }

        PlatformCheckoutConfirmRequest confirmRequest = toConfirmRequest(payment);
        PlatformCheckoutConfirmResponse confirmResponse = platformCartCheckoutService
                .finalizeCheckoutAfterWechatPayment(
                        confirmRequest,
                        payment.getPcpOutTradeNo(),
                        notifyData.get("transaction_id"),
                        notifyXml);

        linkPaymentFinalized(payment, confirmResponse.getBillId(),
                notifyData.get("transaction_id"), notifyXml);

        log.info("[platform/cart/checkoutPayFinalize] paymentId={} billId={} payStatus={}",
                payment.getPcpId(), confirmResponse.getBillId(), confirmResponse.getPayStatus());
    }

    private void linkPaymentFinalized(PlatformCheckoutPaymentEntity payment, Integer billId,
                                      String transactionId, String notifyRaw) {
        if (billId == null) {
            throw new IllegalStateException("finalize 成功但 billId 为空");
        }
        PlatformCheckoutPaymentEntity reloaded = platformCheckoutPaymentDao.queryObject(payment.getPcpId());
        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(reloaded.getPcpStatus())
                && billId.equals(reloaded.getPcpBillId())) {
            return;
        }

        PlatformCheckoutPaymentEntity patch = new PlatformCheckoutPaymentEntity();
        patch.setPcpId(reloaded.getPcpId());
        patch.setPcpStatus(GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS);
        patch.setPcpBillId(billId);
        patch.setPcpTransactionId(transactionId);
        patch.setPcpNotifyRaw(notifyRaw);
        patch.setPcpPaidAt(formatWhatYearDayTime(0));
        patch.setPcpUpdatedAt(formatWhatYearDayTime(0));

        int updated = platformCheckoutPaymentDao.finalizeSuccessIfPending(patch);
        if (updated == 1) {
            return;
        }

        reloaded = platformCheckoutPaymentDao.queryObject(payment.getPcpId());
        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(reloaded.getPcpStatus())
                && billId.equals(reloaded.getPcpBillId())) {
            return;
        }
        throw new IllegalStateException("payment finalize 竞争失败: paymentId=" + payment.getPcpId());
    }

    private void markFailedIfPending(PlatformCheckoutPaymentEntity payment, String notifyXml) {
        if (!GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())) {
            return;
        }
        payment.setPcpStatus(GbBillPlatformConstants.PAYMENT_STATUS_FAILED);
        payment.setPcpNotifyRaw(notifyXml);
        payment.setPcpUpdatedAt(formatWhatYearDayTime(0));
        platformCheckoutPaymentDao.update(payment);
    }

    private void validateNotifyOutTradeNo(Map<String, String> notifyData, PlatformCheckoutPaymentEntity payment) {
        String notifyOutTradeNo = notifyData.get("out_trade_no");
        if (StringUtils.isBlank(notifyOutTradeNo)
                || !notifyOutTradeNo.equals(payment.getPcpOutTradeNo())) {
            throw new IllegalStateException("回调 outTradeNo 与 payment 不一致");
        }
    }

    private void validateNotifyMerchant(Map<String, String> notifyData, WXPayConfig config) {
        String appId = notifyData.get("appid");
        if (StringUtils.isNotBlank(appId) && !appId.equals(config.getAppID())) {
            throw new IllegalStateException("回调 appid 不匹配");
        }
        String mchId = notifyData.get("mch_id");
        if (StringUtils.isNotBlank(mchId) && !mchId.equals(config.getMchID())) {
            throw new IllegalStateException("回调 mch_id 不匹配");
        }
    }

    private void validateNotifyAmount(Map<String, String> notifyData, PlatformCheckoutPaymentEntity payment) {
        String totalFeeRaw = notifyData.get("total_fee");
        if (StringUtils.isBlank(totalFeeRaw)) {
            throw new IllegalStateException("回调缺少 total_fee");
        }
        int expectedFen = toWxTotalFeeInt(payment.getPcpKnownTotal());
        int actualFen;
        try {
            actualFen = Integer.parseInt(totalFeeRaw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("回调 total_fee 非法: " + totalFeeRaw);
        }
        if (expectedFen != actualFen) {
            throw new IllegalStateException("回调金额与 knownTotal 不一致: wxFen=" + actualFen
                    + " expectedFen=" + expectedFen + " knownTotalYuan=" + payment.getPcpKnownTotal());
        }
    }

    private void assertSnapshotOrdersStillCart(PlatformCheckoutPaymentEntity payment) {
        List<Integer> orderIds = JSON.parseObject(payment.getPcpOrderIdsJson(), new TypeReference<List<Integer>>() {
        });
        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalStateException("payment 缺少 orderIds 快照");
        }
        Set<Integer> snapshot = new HashSet<>(orderIds);
        for (Integer orderId : snapshot) {
            NxDepartmentOrdersEntity nxOrder = nxDepartmentOrdersService.queryObject(orderId);
            if (nxOrder == null) {
                throw new IllegalStateException("快照 order 不存在: orderId=" + orderId);
            }
            if (!payment.getPcpGbDepartmentId().equals(nxOrder.getNxDoGbDepartmentId())) {
                throw new IllegalStateException("快照 order 不属于 payment 饭店: orderId=" + orderId);
            }
            if (!getNxOrderStatusGouwu().equals(nxOrder.getNxDoStatus())) {
                throw new IllegalStateException("快照 order 已不是购物车状态: orderId=" + orderId);
            }
        }
    }

    private PlatformCheckoutPaymentEntity loadPaymentForStatusQuery(PlatformCheckoutPaymentStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        PlatformCheckoutPaymentEntity payment = null;
        if (request.getPaymentId() != null) {
            payment = platformCheckoutPaymentDao.queryObject(request.getPaymentId());
        } else if (StringUtils.isNotBlank(request.getOutTradeNo())) {
            payment = platformCheckoutPaymentDao.queryByOutTradeNo(request.getOutTradeNo().trim());
        } else if (StringUtils.isNotBlank(request.getCheckoutToken())) {
            payment = platformCheckoutPaymentDao.queryByCheckoutToken(request.getCheckoutToken().trim());
        }
        if (payment == null) {
            throw new IllegalArgumentException("payment 不存在");
        }
        if (request.getGbDepartmentId() != null
                && !request.getGbDepartmentId().equals(payment.getPcpGbDepartmentId())) {
            throw new IllegalArgumentException("payment 不属于当前饭店");
        }
        return payment;
    }

    private PlatformCheckoutPaymentStatusResponse buildStatusResponse(PlatformCheckoutPaymentEntity payment) {
        PlatformCheckoutPaymentStatusResponse response = new PlatformCheckoutPaymentStatusResponse();
        response.setPaymentId(payment.getPcpId());
        response.setCheckoutToken(payment.getPcpCheckoutToken());
        response.setOutTradeNo(payment.getPcpOutTradeNo());
        response.setStatus(payment.getPcpStatus());
        response.setLocked(platformCheckoutPaymentLifecycleService.isActiveLock(payment));
        response.setExpired(GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED.equals(payment.getPcpStatus()));
        response.setKnownTotal(formatMoney(payment.getPcpKnownTotal()));
        response.setPendingPriceItemCount(payment.getPcpPendingPriceItemCount());
        response.setBillId(payment.getPcpBillId());
        response.setTransactionId(payment.getPcpTransactionId());
        response.setPaidAt(payment.getPcpPaidAt());
        response.setOrderIds(JSON.parseObject(payment.getPcpOrderIdsJson(), new TypeReference<List<Integer>>() {
        }));
        if (payment.getPcpBillId() != null) {
            GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(payment.getPcpBillId());
            if (bill != null) {
                response.setBillPayStatus(bill.getGbDbPayStatus());
            }
        }
        response.setMessage(resolveStatusMessage(payment));
        return response;
    }

    private String resolveStatusMessage(PlatformCheckoutPaymentEntity payment) {
        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getPcpStatus())
                && payment.getPcpBillId() != null) {
            return "支付成功，采购单已生成";
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())) {
            if (platformCheckoutPaymentLifecycleService.isActiveLock(payment)) {
                return "等待微信支付";
            }
            return "支付等待已超时，请重新 checkout";
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_CANCELLED.equals(payment.getPcpStatus())) {
            return "支付已取消，可重新 checkout";
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED.equals(payment.getPcpStatus())) {
            return "支付已超时关闭，可重新 checkout";
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_FAILED.equals(payment.getPcpStatus())) {
            return "支付失败，购物车行仍可继续操作";
        }
        return "paymentStatus=" + payment.getPcpStatus();
    }

    private com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest toPreviewRequest(
            PlatformCheckoutPayRequest request) {
        com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest previewRequest =
                new com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest();
        previewRequest.setMarketId(request.getMarketId());
        previewRequest.setGbDepartmentId(request.getGbDepartmentId());
        previewRequest.setOrderIds(request.getOrderIds());
        return previewRequest;
    }

    private PlatformCheckoutPayResponse buildPayResponseFromExisting(PlatformCheckoutPaymentEntity payment,
                                                                     Integer marketId) {
        if (payment == null) {
            throw new IllegalStateException("payment 不存在");
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getPcpStatus())) {
            if (payment.getPcpBillId() != null) {
                GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(payment.getPcpBillId());
                if (bill != null) {
                    return buildPayResponseFromBill(bill, payment.getPcpCheckoutToken(), true);
                }
            }
            GbDepartmentBillEntity bill =
                    gbDepartmentBillService.queryByPlatformSubmitToken(payment.getPcpCheckoutToken());
            if (bill != null) {
                return buildPayResponseFromBill(bill, payment.getPcpCheckoutToken(), true);
            }
        }
        if (platformCheckoutPaymentLifecycleService.isActiveLock(payment)) {
            Map<String, String> wxPayParams = createOrReuseWxPayParams(payment, marketId);
            return buildPendingPayResponse(payment, wxPayParams, true);
        }
        throw new IllegalArgumentException("checkoutToken 已失效（" + payment.getPcpStatus()
                + "），请重新 checkout 并使用新的 checkoutToken");
    }

    private Map<String, String> createOrReuseWxPayParams(PlatformCheckoutPaymentEntity payment, Integer marketId) {
        try {
            WXPayConfig config = PlatformWechatPayHelper.resolvePayConfig(marketId, sysCityMarketService);
            if (StringUtils.isNotBlank(payment.getPcpWxPrepayId())) {
                Map<String, String> resigned = PlatformWechatPayHelper.resignJsapiParams(
                        config, payment.getPcpWxPrepayId(), UUID.randomUUID().toString().replace("-", ""));
                resigned.put("prepayId", payment.getPcpWxPrepayId());
                return resigned;
            }
            return PlatformWechatPayHelper.createJsapiPrepay(
                    config,
                    payment.getPcpOutTradeNo(),
                    payment.getPcpKnownTotal(),
                    payment.getPcpOpenId(),
                    wechatNotifyUrl,
                    "京采市场采购单");
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("微信 JSAPI 下单失败: " + ex.getMessage(), ex);
        }
    }

    private PlatformCheckoutPayResponse buildPendingPayResponse(PlatformCheckoutPaymentEntity payment,
                                                                Map<String, String> wxPayParams,
                                                                boolean idempotent) {
        PlatformCheckoutPayResponse response = new PlatformCheckoutPayResponse();
        response.setPaymentId(payment.getPcpId());
        response.setCheckoutToken(payment.getPcpCheckoutToken());
        response.setKnownTotal(formatMoney(payment.getPcpKnownTotal()));
        response.setPayAmount(formatMoney(payment.getPcpKnownTotal()));
        response.setOutTradeNo(payment.getPcpOutTradeNo());
        response.setMockPay(false);
        response.setIdempotent(idempotent);
        response.setMessage("请完成微信支付；支付成功后将生成正式采购单");
        Map<String, String> clientParams = new HashMap<>(wxPayParams);
        clientParams.remove("prepayId");
        response.setWxPayParams(clientParams);
        return response;
    }

    private PlatformCheckoutPayResponse buildPayResponseFromBill(GbDepartmentBillEntity bill,
                                                                 String checkoutToken,
                                                                 boolean idempotent) {
        PlatformCheckoutPayResponse response = new PlatformCheckoutPayResponse();
        response.setCheckoutToken(checkoutToken);
        response.setBillId(bill.getGbDepartmentBillId());
        response.setKnownTotal(formatMoney(bill.getGbDbKnownTotal()));
        response.setPayAmount(formatMoney(bill.getGbDbKnownTotal()));
        response.setOutTradeNo(bill.getGbDbTradeNo());
        response.setPayStatus(bill.getGbDbPayStatus());
        response.setMockPay(false);
        response.setIdempotent(idempotent);
        response.setMessage("checkout 已完成");
        return response;
    }

    private PlatformCheckoutConfirmRequest toConfirmRequest(PlatformCheckoutPaymentEntity payment) {
        PlatformCheckoutConfirmRequest request = new PlatformCheckoutConfirmRequest();
        request.setMarketId(payment.getPcpMarketId());
        request.setGbDepartmentId(payment.getPcpGbDepartmentId());
        request.setGbDepartmentFatherId(payment.getPcpGbDepartmentFatherId());
        request.setGbDistributerId(payment.getPcpGbDistributerId());
        request.setGbOrderUserId(payment.getPcpGbOrderUserId());
        request.setDeliveryDate(payment.getPcpDeliveryDate());
        request.setRemark(payment.getPcpRemark());
        request.setCheckoutToken(payment.getPcpCheckoutToken());
        request.setOrderIds(JSON.parseObject(payment.getPcpOrderIdsJson(), new TypeReference<List<Integer>>() {
        }));
        return request;
    }

    private static void validatePayRequest(PlatformCheckoutPayRequest request) {
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
        if (StringUtils.isBlank(request.getOpenId())) {
            throw new IllegalArgumentException("openId 不能为空");
        }
    }

    private static String buildCheckoutOutTradeNo() {
        String timePart = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String outTradeNo = "PCP" + timePart + randomPart;
        if (outTradeNo.getBytes().length > 32) {
            outTradeNo = outTradeNo.substring(0, 32);
        }
        return outTradeNo;
    }

    private static String toWxTotalFee(BigDecimal amountYuan) {
        return String.valueOf(toWxTotalFeeInt(amountYuan));
    }

    private static int toWxTotalFeeInt(BigDecimal amountYuan) {
        return scaleMoney(amountYuan).multiply(new BigDecimal(100)).intValueExact();
    }

    private static BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
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

    private static String formatMoney(BigDecimal value) {
        return scaleMoney(value).toPlainString();
    }

    private static String successXml() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    private static String failXml(String message) {
        String msg = message == null ? "FAIL" : message;
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
    }
}
