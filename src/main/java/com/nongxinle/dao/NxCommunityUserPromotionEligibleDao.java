package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityUserPromotionEligibleEntity;

public interface NxCommunityUserPromotionEligibleDao {

    NxCommunityUserPromotionEligibleEntity queryObject(Integer communityUserId);

    int save(NxCommunityUserPromotionEligibleEntity entity);

    int update(NxCommunityUserPromotionEligibleEntity entity);
}
