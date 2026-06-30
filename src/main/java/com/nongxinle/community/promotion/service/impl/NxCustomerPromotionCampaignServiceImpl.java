package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerPromotionCampaignDao;
import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCampaignService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerPromotionCampaignService")
public class NxCustomerPromotionCampaignServiceImpl implements NxCustomerPromotionCampaignService {

    @Autowired
    private NxCustomerPromotionCampaignDao nxCustomerPromotionCampaignDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCampaignEntity createCampaign(NxCustomerPromotionCampaignEntity entity) {
        validateRequired(entity);
        if (entity.getCampaignScene() == null || entity.getCampaignScene().trim().isEmpty()) {
            entity.setCampaignScene(CustomerReferralConstants.CAMPAIGN_SCENE_REGISTER_ACQUISITION);
        }
        if (entity.getCampaignCode() == null || entity.getCampaignCode().trim().isEmpty()) {
            entity.setCampaignCode("CPN-" + entity.getCommunityId() + "-" + System.currentTimeMillis());
        }
        if (entity.getCampaignStatus() == null) {
            entity.setCampaignStatus(CustomerReferralConstants.CAMPAIGN_STATUS_ACTIVE);
        }
        entity.setCampaignVersion(1);
        assertNoTimeOverlap(entity, null);
        nxCustomerPromotionCampaignDao.save(entity);
        return entity;
    }

    @Override
    public NxCustomerPromotionCampaignEntity queryObject(Integer campaignId) {
        if (campaignId == null) {
            return null;
        }
        NxCustomerPromotionCampaignEntity campaign = nxCustomerPromotionCampaignDao.queryObject(campaignId);
        if (campaign != null) {
            campaign.setQualifiedReferralCount(nxCustomerPromotionCampaignDao.countQualifiedReferralsByCampaign(campaignId));
        }
        return campaign;
    }

    @Override
    public List<NxCustomerPromotionCampaignEntity> queryList(Map<String, Object> map) {
        List<NxCustomerPromotionCampaignEntity> list = nxCustomerPromotionCampaignDao.queryList(map);
        for (NxCustomerPromotionCampaignEntity item : list) {
            item.setQualifiedReferralCount(
                    nxCustomerPromotionCampaignDao.countQualifiedReferralsByCampaign(item.getNxCustomerPromotionCampaignId()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCampaignEntity updateCampaignEditableFields(Integer campaignId, String campaignName,
                                                                           Date effectiveStartAt, Date effectiveEndAt) {
        NxCustomerPromotionCampaignEntity existing = nxCustomerPromotionCampaignDao.queryObjectForUpdate(campaignId);
        if (existing == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        int referralCount = nxCustomerPromotionCampaignDao.countReferralsByCampaign(campaignId);
        boolean hasReferrals = referralCount > 0;

        NxCustomerPromotionCampaignEntity update = new NxCustomerPromotionCampaignEntity();
        update.setNxCustomerPromotionCampaignId(campaignId);
        if (campaignName != null) {
            update.setCampaignName(campaignName);
        }
        if (effectiveStartAt != null) {
            if (hasReferrals && existing.getEffectiveStartAt() != null
                    && !effectiveStartAt.equals(existing.getEffectiveStartAt())) {
                throw new IllegalStateException("活动已产生推广记录，不可修改开始时间");
            }
            update.setEffectiveStartAt(effectiveStartAt);
        }
        if (effectiveEndAt != null) {
            if (hasReferrals && existing.getEffectiveEndAt() != null
                    && effectiveEndAt.before(existing.getEffectiveEndAt())) {
                throw new IllegalStateException("活动已产生推广记录，结束时间只能延后不能提前");
            }
            update.setEffectiveEndAt(effectiveEndAt);
        }
        NxCustomerPromotionCampaignEntity probe = copyForOverlapCheck(existing, update);
        assertNoTimeOverlap(probe, campaignId);
        nxCustomerPromotionCampaignDao.update(update);
        return queryObject(campaignId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activate(Integer campaignId) {
        return updateStatus(campaignId, CustomerReferralConstants.CAMPAIGN_STATUS_ACTIVE, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean suspend(Integer campaignId, String reason) {
        return updateStatus(campaignId, CustomerReferralConstants.CAMPAIGN_STATUS_SUSPENDED, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean terminate(Integer campaignId, String reason) {
        NxCustomerPromotionCampaignEntity update = new NxCustomerPromotionCampaignEntity();
        update.setNxCustomerPromotionCampaignId(campaignId);
        update.setCampaignStatus(CustomerReferralConstants.CAMPAIGN_STATUS_TERMINATED);
        update.setTerminatedAt(new Date());
        update.setStatusReason(reason);
        return nxCustomerPromotionCampaignDao.update(update) > 0;
    }

    private boolean updateStatus(Integer campaignId, String status, String reason) {
        NxCustomerPromotionCampaignEntity existing = nxCustomerPromotionCampaignDao.queryObject(campaignId);
        if (existing == null) {
            return false;
        }
        if (CustomerReferralConstants.CAMPAIGN_STATUS_TERMINATED.equals(existing.getCampaignStatus())) {
            throw new IllegalStateException("活动已终止，不可变更状态");
        }
        NxCustomerPromotionCampaignEntity update = new NxCustomerPromotionCampaignEntity();
        update.setNxCustomerPromotionCampaignId(campaignId);
        update.setCampaignStatus(status);
        update.setStatusReason(reason);
        if (CustomerReferralConstants.CAMPAIGN_STATUS_ACTIVE.equals(status)) {
            assertNoTimeOverlap(existing, campaignId);
        }
        return nxCustomerPromotionCampaignDao.update(update) > 0;
    }

    @Override
    public Map<String, Object> queryCampaignStats(Integer campaignId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("qualifiedReferralCount", nxCustomerPromotionCampaignDao.countQualifiedReferralsByCampaign(campaignId));
        stats.put("totalReferralCount", nxCustomerPromotionCampaignDao.countReferralsByCampaign(campaignId));
        return stats;
    }

    @Override
    public List<Map<String, Object>> statsByOwner(Integer campaignId) {
        return nxCustomerPromotionCampaignDao.statsByOwnerForCampaign(campaignId);
    }

    @Override
    public List<NxCustomerUserReferralEntity> queryReferralDetails(Integer campaignId, String sourceOwnerType,
                                                                   Integer sourceOwnerId, Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("campaignId", campaignId);
        map.put("sourceOwnerType", sourceOwnerType);
        map.put("sourceOwnerId", sourceOwnerId);
        map.put("offset", offset);
        map.put("limit", limit);
        return nxCustomerPromotionCampaignDao.queryReferralsByCampaign(map);
    }

    @Override
    public NxCustomerPromotionCampaignEntity resolveActiveCampaign(Integer communityId, Integer commerceId,
                                                                    String ownerScope, Date now) {
        if (communityId == null || commerceId == null || ownerScope == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("communityId", communityId);
        map.put("commerceId", commerceId);
        map.put("campaignScene", CustomerReferralConstants.CAMPAIGN_SCENE_REGISTER_ACQUISITION);
        map.put("ownerScope", ownerScope);
        map.put("now", now != null ? now : new Date());
        List<NxCustomerPromotionCampaignEntity> matches = nxCustomerPromotionCampaignDao.queryActiveCampaignMatches(map);
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("推广活动命中歧义，同一时刻存在多个有效活动");
        }
        return matches.get(0);
    }

    private void validateRequired(NxCustomerPromotionCampaignEntity entity) {
        if (entity.getCommunityId() == null || entity.getCommerceId() == null || entity.getOwnerScope() == null) {
            throw new IllegalArgumentException("communityId、commerceId、ownerScope不能为空");
        }
    }

    private void assertNoTimeOverlap(NxCustomerPromotionCampaignEntity entity, Integer excludeCampaignId) {
        Map<String, Object> map = new HashMap<>();
        map.put("communityId", entity.getCommunityId());
        map.put("commerceId", entity.getCommerceId());
        map.put("campaignScene", entity.getCampaignScene() != null
                ? entity.getCampaignScene() : CustomerReferralConstants.CAMPAIGN_SCENE_REGISTER_ACQUISITION);
        map.put("ownerScope", entity.getOwnerScope());
        map.put("effectiveStartAt", entity.getEffectiveStartAt());
        map.put("effectiveEndAt", entity.getEffectiveEndAt());
        map.put("excludeCampaignId", excludeCampaignId);
        int overlap = nxCustomerPromotionCampaignDao.countOverlappingCampaign(map);
        if (overlap > 0) {
            throw new IllegalStateException("同一 community/commerce/ownerScope 下活动时间不可重叠");
        }
    }

    private NxCustomerPromotionCampaignEntity copyForOverlapCheck(NxCustomerPromotionCampaignEntity existing,
                                                                   NxCustomerPromotionCampaignEntity patch) {
        NxCustomerPromotionCampaignEntity probe = new NxCustomerPromotionCampaignEntity();
        probe.setCommunityId(existing.getCommunityId());
        probe.setCommerceId(existing.getCommerceId());
        probe.setCampaignScene(existing.getCampaignScene());
        probe.setOwnerScope(existing.getOwnerScope());
        probe.setEffectiveStartAt(patch.getEffectiveStartAt() != null
                ? patch.getEffectiveStartAt() : existing.getEffectiveStartAt());
        probe.setEffectiveEndAt(patch.getEffectiveEndAt() != null
                ? patch.getEffectiveEndAt() : existing.getEffectiveEndAt());
        return probe;
    }
}
