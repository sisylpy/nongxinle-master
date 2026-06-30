package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerReferralRewardRuleDao {

    NxCustomerReferralRewardRuleEntity queryObject(Integer id);

    List<NxCustomerReferralRewardRuleEntity> queryList(Map<String, Object> map);

    List<NxCustomerReferralRewardRuleEntity> queryActiveRuleMatches(Map<String, Object> map);

    int countOverlappingRule(Map<String, Object> map);
}
