package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerReferralService {

    void processPromotionAfterRegister(NxCustomerUserEntity newUser, String promotionCode, String shareEntry);

    Map<String, Object> queryHomeNoticeSummary(Integer userId);

    Map<String, Object> queryMyReferralOverview(Integer userId, Integer directOffset, Integer directLimit);

    boolean markDynamicsRead(Integer userId, Integer upToReferralId);

    Map<String, Object> queryMyReferralStats(Integer userId);

    List<Map<String, Object>> queryDirectReferralList(Integer userId, Integer offset, Integer limit);

    List<Map<String, Object>> queryLevel2ReferralList(Integer userId, Integer offset, Integer limit);
}
