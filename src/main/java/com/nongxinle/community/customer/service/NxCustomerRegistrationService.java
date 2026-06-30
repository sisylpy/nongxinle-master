package com.nongxinle.community.customer.service;

import java.util.Map;

public interface NxCustomerRegistrationService {

    Map<String, Object> registerNewCustomerMix(String phoneCode, String openId, Integer commerceId,
                                               Integer commId, String promotionCode);
}
