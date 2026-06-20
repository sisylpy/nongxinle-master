package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformBillPayFirstRequest;
import com.nongxinle.dto.platform.customer.PlatformBillPaySupplementRequest;
import com.nongxinle.dto.platform.customer.PlatformBillPaymentIntentResponse;

public interface PlatformBillPaymentService {

    PlatformBillPaymentIntentResponse payFirst(PlatformBillPayFirstRequest request);

    PlatformBillPaymentIntentResponse paySupplement(PlatformBillPaySupplementRequest request);

    /**
     * 本地验收辅助：模拟支付成功，非正式微信回调。
     */
    void markPaymentSuccessLocal(Integer paymentId);
}
