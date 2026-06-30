package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.community.core.service.NxCommunityService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionScopeService {

    @Autowired
    private NxCommunityService nxCommunityService;

    /**
     * 正式员工的 commerce 边界唯一来源：所属社区 nx_community.nx_community_commerce_id
     */
    public Integer resolveCommerceIdByCommunityId(Integer communityId) {
        if (communityId == null) {
            return null;
        }
        NxCommunityEntity community = nxCommunityService.queryObject(communityId);
        return community != null ? community.getNxCommunityCommerceId() : null;
    }

    public boolean isCodeCommerceConsistentWithCommunity(NxCustomerPromotionCodeEntity code, Integer communityId) {
        Integer commerceId = resolveCommerceIdByCommunityId(communityId);
        if (commerceId == null || code.getCommerceId() == null) {
            return true;
        }
        return commerceId.equals(code.getCommerceId());
    }

    public String validateCodeCommerceForCommunity(NxCustomerPromotionCodeEntity code, Integer communityId) {
        if (!isCodeCommerceConsistentWithCommunity(code, communityId)) {
            return CustomerReferralConstants.INVALID_CROSS_COMMERCE;
        }
        return null;
    }
}
