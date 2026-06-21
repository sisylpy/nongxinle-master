package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformCheckoutPayRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPayResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentCancelRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPaymentStatusResponse;

public interface PlatformCheckoutPaymentService {

    PlatformCheckoutPayResponse checkoutPay(PlatformCheckoutPayRequest request);

    PlatformCheckoutPaymentStatusResponse queryPaymentStatus(PlatformCheckoutPaymentStatusRequest request);

    PlatformCheckoutPaymentStatusResponse cancelPayment(PlatformCheckoutPaymentCancelRequest request);

    String handleWechatNotify(String notifyXml);

    /**
     * Runner / 本地验收：跳过微信验签，执行与回调相同的 finalize 闭环。
     */
    PlatformCheckoutPaymentStatusResponse completePaymentSuccessForRunner(Integer paymentId, String transactionId);

    /**
     * Runner：仅创建 PENDING payment 快照（不调微信 unifiedOrder）。
     */
    Integer createPaymentIntentSnapshot(PlatformCheckoutPayRequest request);
}
