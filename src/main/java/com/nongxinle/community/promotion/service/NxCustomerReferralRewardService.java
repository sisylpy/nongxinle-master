package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCustomerReferralRewardEntity;
import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerReferralRewardService {

    void createRewardsForNewReferral(NxCustomerUserEntity newUser, Integer referralId,
                                     Integer beneficiaryUserId, NxCustomerReferralRewardRuleEntity matchedRule);

    void recordRewardRuleConflict(NxCustomerUserEntity newUser, Integer referralId,
                                  Integer beneficiaryUserId, String conflictReason);

    List<NxCustomerReferralRewardEntity> queryRewardList(Integer userId, Integer status, Integer offset, Integer limit);

    NxCustomerUserCouponEntity claimReward(Integer userId, Integer rewardId);

    Map<String, Object> claimRewardsBatch(Integer userId, String rewardIds, Boolean claimAll);

    void attachLoginExtras(Map<String, Object> loginData, Integer userId, Integer commId);

    List<Map<String, Object>> queryReferralCouponGroups(Integer userId);
}
