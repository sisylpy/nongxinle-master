package com.nongxinle.community.promotion.service;

import com.nongxinle.dto.PromotionResolveResult;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCodeService {

    NxCustomerPromotionCodeEntity getOrCreateForCustomerUser(NxCustomerUserEntity user);

    NxCustomerPromotionCodeEntity getOrCreateForCommunityUser(Integer communityUserId);

    PromotionResolveResult resolvePromotionCode(String promotionCode, NxCustomerUserEntity newUser);

    NxCustomerPromotionCodeEntity createCode(String ownerType, Integer ownerId, Integer commerceId,
                                              Integer communityId, Date validStartAt, Date validEndAt,
                                              Integer rewardRuleId);

    NxCustomerPromotionCodeEntity regenerateCode(Integer promotionCodeId, String disabledReason);

    boolean updateCodeStatus(Integer promotionCodeId, String codeStatus, String disabledReason);

    boolean updateCodeValidity(Integer promotionCodeId, Date validStartAt, Date validEndAt);

    void deactivateAllActiveCodesByOwner(String ownerType, Integer ownerId, String reason);

    NxCustomerPromotionCodeEntity queryObject(Integer id);

    NxCustomerPromotionCodeEntity queryActiveByOwner(String ownerType, Integer ownerId);

    NxCustomerPromotionCodeEntity queryLatestByOwner(String ownerType, Integer ownerId);

    List<NxCustomerPromotionCodeEntity> queryList(Map<String, Object> map);
}
