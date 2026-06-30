package com.nongxinle.dto;

import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardRuleResolveResult {

    private NxCustomerReferralRewardRuleEntity matchedRule;
    private boolean ambiguous;
    private int matchCount;

    public static RewardRuleResolveResult none() {
        RewardRuleResolveResult r = new RewardRuleResolveResult();
        r.matchCount = 0;
        return r;
    }

    public static RewardRuleResolveResult single(NxCustomerReferralRewardRuleEntity rule) {
        RewardRuleResolveResult r = new RewardRuleResolveResult();
        r.matchedRule = rule;
        r.matchCount = 1;
        return r;
    }

    public static RewardRuleResolveResult conflict(int matchCount) {
        RewardRuleResolveResult r = new RewardRuleResolveResult();
        r.ambiguous = true;
        r.matchCount = matchCount;
        return r;
    }
}
