package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.GbBillPaymentRecalcResult;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GbBillPaymentRecalcServiceImpl implements GbBillPaymentRecalcService {

    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;

    @Override
    public GbBillPaymentRecalcResult recalcBillPaymentState(Integer billId) {
        GbBillPaymentRecalcResult result = new GbBillPaymentRecalcResult();
        result.setBillId(billId);
        if (billId == null) {
            result.setNoOp(true);
            return result;
        }

        GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(billId);
        if (bill == null) {
            result.setNoOp(true);
            return result;
        }

        result.setBillSource(bill.getGbDbBillSource());
        if (!GbBillPlatformConstants.BILL_SOURCE_PLATFORM_CASH.equals(bill.getGbDbBillSource())) {
            result.setNoOp(true);
            return result;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<GbDepartmentOrdersEntity> lines = gbDepartmentOrdersService.queryDisOrdersByParams(map);

        BigDecimal finalTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int pendingCount = 0;
        for (GbDepartmentOrdersEntity line : lines) {
            if (isCancelledLine(line)) {
                continue;
            }
            if (isPendingLine(line)) {
                pendingCount++;
                continue;
            }
            finalTotal = finalTotal.add(resolveLineSubtotal(line));
        }

        BigDecimal knownTotal = scaleMoney(bill.getGbDbKnownTotal());
        BigDecimal paidTotal = scaleMoney(bill.getGbDbPaidTotal());
        BigDecimal supplementDue = finalTotal.subtract(paidTotal).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        String payStatus = resolvePayStatus(bill, knownTotal, paidTotal, finalTotal, pendingCount, supplementDue);

        bill.setGbDbTotal(finalTotal.toPlainString());
        bill.setGbDbPendingItemCount(pendingCount);
        bill.setGbDbSupplementDue(supplementDue);
        bill.setGbDbPayStatus(payStatus);
        gbDepartmentBillService.update(bill);

        result.setFinalTotal(finalTotal);
        result.setKnownTotal(knownTotal);
        result.setPaidTotal(paidTotal);
        result.setSupplementDue(supplementDue);
        result.setPendingItemCount(pendingCount);
        result.setPayStatus(payStatus);
        result.setBlocksNewOrder(GbBillPlatformConstants.blocksNewOrder(payStatus)
                && hasOutstandingDebt(payStatus, knownTotal, paidTotal, supplementDue));
        result.setNoOp(false);
        return result;
    }

    public static String resolvePayStatus(GbDepartmentBillEntity bill, BigDecimal knownTotal, BigDecimal paidTotal,
                                   BigDecimal finalTotal, int pendingCount, BigDecimal supplementDue) {
        if (GbBillPlatformConstants.PAY_STATUS_CANCELLED.equals(bill.getGbDbPayStatus())) {
            return GbBillPlatformConstants.PAY_STATUS_CANCELLED;
        }
        if (pendingCount > 0 && paidTotal.compareTo(BigDecimal.ZERO) == 0 && knownTotal.compareTo(BigDecimal.ZERO) == 0) {
            return GbBillPlatformConstants.PAY_STATUS_NONE;
        }
        if (pendingCount > 0 && paidTotal.compareTo(BigDecimal.ZERO) == 0 && knownTotal.compareTo(BigDecimal.ZERO) > 0) {
            return GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY;
        }
        if (pendingCount > 0 && paidTotal.compareTo(BigDecimal.ZERO) > 0) {
            return GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID;
        }
        if (pendingCount == 0 && paidTotal.compareTo(finalTotal) >= 0) {
            return GbBillPlatformConstants.PAY_STATUS_PAID;
        }
        if (pendingCount == 0 && paidTotal.compareTo(BigDecimal.ZERO) == 0
                && knownTotal.compareTo(BigDecimal.ZERO) > 0) {
            return GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY;
        }
        if (pendingCount == 0 && supplementDue.compareTo(BigDecimal.ZERO) > 0) {
            return GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT;
        }
        if (pendingCount == 0 && knownTotal.compareTo(BigDecimal.ZERO) == 0
                && paidTotal.compareTo(BigDecimal.ZERO) == 0) {
            return GbBillPlatformConstants.PAY_STATUS_NONE;
        }
        return GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY;
    }

    public static boolean hasOutstandingDebt(String payStatus, BigDecimal knownTotal, BigDecimal paidTotal,
                                      BigDecimal supplementDue) {
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY.equals(payStatus)) {
            return knownTotal.compareTo(paidTotal) > 0;
        }
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT.equals(payStatus)) {
            return supplementDue.compareTo(BigDecimal.ZERO) > 0;
        }
        return false;
    }

    private static boolean isCancelledLine(GbDepartmentOrdersEntity line) {
        return line.getGbDoStatus() != null && line.getGbDoStatus() < 0;
    }

    private static boolean isPendingLine(GbDepartmentOrdersEntity line) {
        if (GbBillPlatformConstants.PRICE_CONFIRM_PENDING.equals(line.getGbDoPriceConfirmStatus())) {
            return true;
        }
        if (StringUtils.isBlank(line.getGbDoPriceConfirmStatus())) {
            return true;
        }
        return !GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(line.getGbDoPriceConfirmStatus());
    }

    private static BigDecimal resolveLineSubtotal(GbDepartmentOrdersEntity line) {
        if (StringUtils.isNotBlank(line.getGbDoSubtotal())) {
            try {
                return new BigDecimal(line.getGbDoSubtotal().trim()).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        BigDecimal price = parseMoney(line.getGbDoPrice());
        BigDecimal qty = parseMoney(line.getGbDoQuantity());
        if (price == null || qty == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal parseMoney(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
