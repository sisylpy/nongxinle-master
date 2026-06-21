package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformCartLineDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCartLineItem;
import com.nongxinle.dto.platform.customer.PlatformCartLineUpdateRequest;
import com.nongxinle.dto.platform.customer.PlatformCartListRequest;
import com.nongxinle.dto.platform.customer.PlatformCartListResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmResponse;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewResponse;

public interface PlatformCartCheckoutService {

    PlatformCartListResponse listCartLines(PlatformCartListRequest request);

    PlatformCartLineItem updateCartLine(PlatformCartLineUpdateRequest request);

    void deleteCartLine(PlatformCartLineDeleteRequest request);

    PlatformCheckoutPreviewResponse checkoutPreview(PlatformCheckoutPreviewRequest request);

    PlatformCheckoutConfirmResponse checkoutConfirm(PlatformCheckoutConfirmRequest request);

    /**
     * 微信支付成功回调：执行 checkout 正式化（创建 bill、挂行、assign），并落库 bill payment。
     * 本地 mock 请用 {@link #checkoutConfirm}。
     */
    PlatformCheckoutConfirmResponse finalizeCheckoutAfterWechatPayment(PlatformCheckoutConfirmRequest request,
                                                                       String outTradeNo,
                                                                       String transactionId,
                                                                       String notifyRaw);
}
