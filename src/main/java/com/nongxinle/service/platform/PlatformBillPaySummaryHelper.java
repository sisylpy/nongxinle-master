package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformOutstandingBillInfo;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class PlatformBillPaySummaryHelper {

    private PlatformBillPaySummaryHelper() {
    }

    static PlatformOutstandingBillInfo toOutstandingInfo(GbDepartmentBillEntity bill, NxDistributerEntity supplier) {
        if (bill == null || !GbBillPlatformConstants.blocksNewOrder(bill.getGbDbPayStatus())) {
            return null;
        }
        PlatformOutstandingBillInfo info = new PlatformOutstandingBillInfo();
        info.setBillId(bill.getGbDepartmentBillId());
        info.setBillPayState(bill.getGbDbPayStatus());
        info.setPayPhase(resolvePayPhase(bill.getGbDbPayStatus()));
        info.setPayAmount(formatMoney(resolvePayAmount(bill)));
        if (supplier != null) {
            info.setSupplierName(supplier.getNxDistributerShowName() != null
                    ? supplier.getNxDistributerShowName()
                    : supplier.getNxDistributerName());
        }
        info.setMessage(resolveMessage(bill.getGbDbPayStatus()));
        return info;
    }

    static PlatformOutstandingBillInfo toBlockedInfo(GbDepartmentBillEntity bill, NxDistributerEntity supplier) {
        PlatformOutstandingBillInfo info = toOutstandingInfo(bill, supplier);
        if (info != null) {
            info.setBlocked(true);
        }
        return info;
    }

    static BigDecimal resolvePayAmount(GbDepartmentBillEntity bill) {
        BigDecimal knownTotal = scale(bill.getGbDbKnownTotal());
        BigDecimal paidTotal = scale(bill.getGbDbPaidTotal());
        BigDecimal supplementDue = scale(bill.getGbDbSupplementDue());
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY.equals(bill.getGbDbPayStatus())) {
            return knownTotal.subtract(paidTotal).max(BigDecimal.ZERO);
        }
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT.equals(bill.getGbDbPayStatus())) {
            return supplementDue.max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    static String resolvePayPhase(String payStatus) {
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT.equals(payStatus)) {
            return GbBillPlatformConstants.PAY_PHASE_SUPPLEMENT;
        }
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY.equals(payStatus)) {
            return GbBillPlatformConstants.PAY_PHASE_FIRST;
        }
        return null;
    }

    static String resolveMessage(String payStatus) {
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT.equals(payStatus)) {
            return "您有待补款采购单，请先补款后再下单";
        }
        if (GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY.equals(payStatus)) {
            return "您有待支付采购单，请先完成支付";
        }
        return null;
    }

    static boolean billBelongsToDepartment(GbDepartmentBillEntity bill, Integer gbDepartmentId) {
        if (bill == null || gbDepartmentId == null) {
            return false;
        }
        if (gbDepartmentId.equals(bill.getGbDbDepId())) {
            return true;
        }
        return bill.getGbDbDepFatherId() != null && gbDepartmentId.equals(bill.getGbDbDepFatherId());
    }

    static String formatMoney(BigDecimal value) {
        return scale(value).toPlainString();
    }

    static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal scale(String value) {
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
