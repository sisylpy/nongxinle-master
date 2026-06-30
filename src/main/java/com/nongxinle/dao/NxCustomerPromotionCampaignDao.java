package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerPromotionCampaignDao {

    NxCustomerPromotionCampaignEntity queryObject(Integer id);

    NxCustomerPromotionCampaignEntity queryObjectForUpdate(Integer id);

    List<NxCustomerPromotionCampaignEntity> queryList(Map<String, Object> map);

    List<NxCustomerPromotionCampaignEntity> queryActiveCampaignMatches(Map<String, Object> map);

    int countOverlappingCampaign(Map<String, Object> map);

    int countReferralsByCampaign(Integer campaignId);

    int save(NxCustomerPromotionCampaignEntity entity);

    int update(NxCustomerPromotionCampaignEntity entity);

    int countQualifiedReferralsByCampaign(Integer campaignId);

    List<Map<String, Object>> statsByOwnerForCampaign(Integer campaignId);

    List<NxCustomerUserReferralEntity> queryReferralsByCampaign(Map<String, Object> map);
}
