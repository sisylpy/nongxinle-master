package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class LineAmountConfirmResult {

    private boolean confirmable;
    private String priceConfirmStatus;
    private BigDecimal resolvedUnitPrice;
    private String resolvedPriceStandard;
    private BigDecimal lineSubtotal;
    private String pendingReason;

    public static LineAmountConfirmResult confirmed(BigDecimal unitPrice, String priceStandard, BigDecimal subtotal) {
        LineAmountConfirmResult result = new LineAmountConfirmResult();
        result.confirmable = true;
        result.priceConfirmStatus = com.nongxinle.utils.GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED;
        result.resolvedUnitPrice = unitPrice;
        result.resolvedPriceStandard = priceStandard;
        result.lineSubtotal = subtotal;
        return result;
    }

    public static LineAmountConfirmResult pending(String reason) {
        LineAmountConfirmResult result = new LineAmountConfirmResult();
        result.confirmable = false;
        result.priceConfirmStatus = com.nongxinle.utils.GbBillPlatformConstants.PRICE_CONFIRM_PENDING;
        result.pendingReason = reason;
        return result;
    }
}
