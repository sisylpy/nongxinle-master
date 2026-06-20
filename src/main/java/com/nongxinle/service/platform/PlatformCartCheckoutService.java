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
}
