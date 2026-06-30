package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCampaignService {

    NxCustomerPromotionCampaignEntity createCampaign(NxCustomerPromotionCampaignEntity entity);

    NxCustomerPromotionCampaignEntity queryObject(Integer campaignId);

    List<NxCustomerPromotionCampaignEntity> queryList(Map<String, Object> map);

    NxCustomerPromotionCampaignEntity updateCampaignEditableFields(Integer campaignId, String campaignName,
                                                                    Date effectiveStartAt, Date effectiveEndAt);

    boolean activate(Integer campaignId);

    boolean suspend(Integer campaignId, String reason);

    boolean terminate(Integer campaignId, String reason);

    Map<String, Object> queryCampaignStats(Integer campaignId);

    List<Map<String, Object>> statsByOwner(Integer campaignId);

    List<NxCustomerUserReferralEntity> queryReferralDetails(Integer campaignId, String sourceOwnerType,
                                                             Integer sourceOwnerId, Integer offset, Integer limit);

    /**
     * 注册时按 community/commerce/ownerType/时间 唯一命中当前活动（推广码不绑定 campaign）。
     */
    NxCustomerPromotionCampaignEntity resolveActiveCampaign(Integer communityId, Integer commerceId,
                                                             String ownerScope, Date now);
}
