package com.nongxinle.service.platform;

import com.nongxinle.dao.GbDepartmentBillPaymentDao;
import com.nongxinle.dto.platform.customer.PlatformBillPayFirstRequest;
import com.nongxinle.dto.platform.customer.PlatformBillPaySupplementRequest;
import com.nongxinle.dto.platform.customer.PlatformBillPaymentIntentResponse;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.GbDepartmentBillPaymentEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.ParseObject.myRandom;

@Service
public class PlatformBillPaymentServiceImpl implements PlatformBillPaymentService {

    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentBillPaymentDao gbDepartmentBillPaymentDao;
    @Autowired
    private GbBillPaymentRecalcService gbBillPaymentRecalcService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformBillPaymentIntentResponse payFirst(PlatformBillPayFirstRequest request) {
        validatePayRequest(request == null ? null : request.getBillId(), request == null ? null : request.getGbDepartmentId());
        GbDepartmentBillEntity bill = loadPlatformCashBill(request.getBillId(), request.getGbDepartmentId());

        if (!GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY.equals(bill.getGbDbPayStatus())) {
            throw new IllegalArgumentException("当前采购单不可首付，payStatus=" + bill.getGbDbPayStatus());
        }

        BigDecimal payAmount = PlatformBillPaySummaryHelper.resolvePayAmount(bill);
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("首付金额必须大于 0");
        }

        GbDepartmentBillPaymentEntity payment = createPendingPayment(
                bill.getGbDepartmentBillId(),
                GbBillPlatformConstants.PAY_PHASE_FIRST,
                payAmount,
                buildOutTradeNo("PCF", bill.getGbDepartmentBillId()));

        return buildIntentResponse(bill.getGbDepartmentBillId(), payment, GbBillPlatformConstants.PAY_PHASE_FIRST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformBillPaymentIntentResponse paySupplement(PlatformBillPaySupplementRequest request) {
        validatePayRequest(request == null ? null : request.getBillId(), request == null ? null : request.getGbDepartmentId());
        GbDepartmentBillEntity bill = loadPlatformCashBill(request.getBillId(), request.getGbDepartmentId());

        if (!GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT.equals(bill.getGbDbPayStatus())) {
            throw new IllegalArgumentException("当前采购单不可补款，payStatus=" + bill.getGbDbPayStatus());
        }
        if (bill.getGbDbPendingItemCount() != null && bill.getGbDbPendingItemCount() > 0) {
            throw new IllegalArgumentException("仍有待确认商品，暂不可补款");
        }

        BigDecimal payAmount = PlatformBillPaySummaryHelper.scale(bill.getGbDbSupplementDue());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("补款金额必须大于 0");
        }

        GbDepartmentBillPaymentEntity payment = createPendingPayment(
                bill.getGbDepartmentBillId(),
                GbBillPlatformConstants.PAY_PHASE_SUPPLEMENT,
                payAmount,
                buildOutTradeNo("PCS", bill.getGbDepartmentBillId()));

        return buildIntentResponse(bill.getGbDepartmentBillId(), payment, GbBillPlatformConstants.PAY_PHASE_SUPPLEMENT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPaymentSuccessLocal(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId 不能为空");
        }
        GbDepartmentBillPaymentEntity payment = gbDepartmentBillPaymentDao.queryObject(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("payment 不存在: " + paymentId);
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS.equals(payment.getGbBpStatus())) {
            return;
        }

        payment.setGbBpStatus(GbBillPlatformConstants.PAYMENT_STATUS_SUCCESS);
        payment.setGbBpPaidAt(formatWhatYearDayTime(0));
        gbDepartmentBillPaymentDao.update(payment);

        GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(payment.getGbBpBillId());
        if (bill == null) {
            throw new IllegalArgumentException("bill 不存在: " + payment.getGbBpBillId());
        }
        BigDecimal paidTotal = PlatformBillPaySummaryHelper.scale(bill.getGbDbPaidTotal())
                .add(PlatformBillPaySummaryHelper.scale(payment.getGbBpAmount()))
                .setScale(2, RoundingMode.HALF_UP);
        bill.setGbDbPaidTotal(paidTotal);
        gbDepartmentBillService.update(bill);
        gbBillPaymentRecalcService.recalcBillPaymentState(bill.getGbDepartmentBillId());
    }

    private GbDepartmentBillEntity loadPlatformCashBill(Integer billId, Integer gbDepartmentId) {
        GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(billId);
        if (bill == null) {
            throw new IllegalArgumentException("采购单不存在");
        }
        if (!GbBillPlatformConstants.BILL_SOURCE_PLATFORM_CASH.equals(bill.getGbDbBillSource())) {
            throw new IllegalArgumentException("非平台现金采购单");
        }
        if (!PlatformBillPaySummaryHelper.billBelongsToDepartment(bill, gbDepartmentId)) {
            throw new IllegalArgumentException("采购单不属于当前饭店");
        }
        return bill;
    }

    private static void validatePayRequest(Integer billId, Integer gbDepartmentId) {
        if (billId == null) {
            throw new IllegalArgumentException("billId 不能为空");
        }
        if (gbDepartmentId == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
    }

    private GbDepartmentBillPaymentEntity createPendingPayment(
            Integer billId, String phase, BigDecimal amount, String outTradeNo) {
        GbDepartmentBillPaymentEntity payment = new GbDepartmentBillPaymentEntity();
        payment.setGbBpBillId(billId);
        payment.setGbBpPayPhase(phase);
        payment.setGbBpAmount(amount.setScale(2, RoundingMode.HALF_UP));
        payment.setGbBpOutTradeNo(outTradeNo);
        payment.setGbBpStatus(GbBillPlatformConstants.PAYMENT_STATUS_PENDING);
        gbDepartmentBillPaymentDao.save(payment);
        return payment;
    }

    private static String buildOutTradeNo(String prefix, Integer billId) {
        return prefix + billId + StringUtils.replace(formatWhatYearDayTime(0), " ", "")
                + myRandom();
    }

    private static PlatformBillPaymentIntentResponse buildIntentResponse(
            Integer billId, GbDepartmentBillPaymentEntity payment, String payPhase) {
        PlatformBillPaymentIntentResponse response = new PlatformBillPaymentIntentResponse();
        response.setBillId(billId);
        response.setPaymentId(payment.getGbBpId());
        response.setPayPhase(payPhase);
        response.setPayAmount(PlatformBillPaySummaryHelper.formatMoney(payment.getGbBpAmount()));
        response.setOutTradeNo(payment.getGbBpOutTradeNo());
        response.setMockPay(true);
        response.setMessage("支付意图已创建，等待接入微信支付");
        return response;
    }
}
