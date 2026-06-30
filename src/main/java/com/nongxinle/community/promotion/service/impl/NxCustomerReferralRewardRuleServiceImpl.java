package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerReferralRewardRuleDao;
import com.nongxinle.dto.RewardRuleResolveResult;
import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardRuleService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerReferralRewardRuleService")
public class NxCustomerReferralRewardRuleServiceImpl implements NxCustomerReferralRewardRuleService {

    @Autowired
    private NxCustomerReferralRewardRuleDao nxCustomerReferralRewardRuleDao;

    @Override
    public RewardRuleResolveResult resolveRegisterRule(Integer communityId, String beneficiaryType, Date now) {
        if (communityId == null) {
            return RewardRuleResolveResult.none();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("communityId", communityId);
        map.put("ruleCode", CustomerReferralConstants.RULE_DIRECT_REGISTER);
        map.put("rewardTarget", CustomerReferralConstants.TARGET_DIRECT_INVITER);
        map.put("triggerType", CustomerReferralConstants.TRIGGER_REGISTER);
        map.put("beneficiaryType", beneficiaryType);
        map.put("now", now != null ? now : new Date());
        List<NxCustomerReferralRewardRuleEntity> matches = nxCustomerReferralRewardRuleDao.queryActiveRuleMatches(map);
        if (matches == null || matches.isEmpty()) {
            return RewardRuleResolveResult.none();
        }
        if (matches.size() > 1) {
            return RewardRuleResolveResult.conflict(matches.size());
        }
        NxCustomerReferralRewardRuleEntity rule = matches.get(0);
        if (rule.getEnabled() == null || rule.getEnabled() != 1) {
            return RewardRuleResolveResult.none();
        }
        return RewardRuleResolveResult.single(rule);
    }
}
