package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCommunityUserPromotionEligibleEntity;

import java.util.Date;

public interface NxCommunityUserPromotionEligibleService {

    NxCommunityUserPromotionEligibleEntity queryObject(Integer communityUserId);

    NxCommunityUserPromotionEligibleEntity enable(Integer communityUserId, Date validStartAt, Date validEndAt);

    boolean disable(Integer communityUserId);

    void deactivatePromotionAssets(Integer communityUserId, String reason);
}
