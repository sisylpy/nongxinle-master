package com.nongxinle.community.promotion.service;

import com.nongxinle.dto.RewardRuleResolveResult;

import java.util.Date;

public interface NxCustomerReferralRewardRuleService {

    /**
     * 注册拉新场景解析奖励规则：0 条无规则，1 条命中，多条为配置冲突。
     */
    RewardRuleResolveResult resolveRegisterRule(Integer communityId, String beneficiaryType, Date now);
}
