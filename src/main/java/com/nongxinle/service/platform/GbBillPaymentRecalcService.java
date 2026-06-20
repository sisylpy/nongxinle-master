package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.GbBillPaymentRecalcResult;

public interface GbBillPaymentRecalcService {

    GbBillPaymentRecalcResult recalcBillPaymentState(Integer billId);
}
