package com.nongxinle.community.promotion.service;

import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCodeAttemptService {

    List<Map<String, Object>> queryMyAttemptList(Integer userId, Integer communityId, Integer offset, Integer limit);

    int queryMyAttemptTotal(Integer userId, Integer communityId);
}
