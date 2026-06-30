package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerPromoterDao;
import com.nongxinle.dao.NxCustomerPromotionCodeAttemptDao;
import com.nongxinle.dao.NxCustomerPromotionCodeDao;
import com.nongxinle.dao.NxCustomerPromotionCodeOwnerLockDao;
import com.nongxinle.dao.NxCustomerReferralRewardDao;
import com.nongxinle.dao.NxCustomerUserReferralDao;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerPromoterEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromoterService;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.community.promotion.service.PromotionScopeService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerPromoterService")
public class NxCustomerPromoterServiceImpl implements NxCustomerPromoterService {

    @Autowired
    private NxCustomerPromoterDao nxCustomerPromoterDao;
    @Autowired
    private NxCustomerUserReferralDao nxCustomerUserReferralDao;
    @Autowired
    private NxCustomerReferralRewardDao nxCustomerReferralRewardDao;
    @Autowired
    private NxCustomerPromotionCodeDao nxCustomerPromotionCodeDao;
    @Autowired
    private NxCustomerPromotionCodeAttemptDao nxCustomerPromotionCodeAttemptDao;
    @Autowired
    private NxCustomerPromotionCodeOwnerLockDao nxCustomerPromotionCodeOwnerLockDao;
    @Autowired
    private NxCustomerPromotionCodeService nxCustomerPromotionCodeService;
    @Autowired
    private PromotionScopeService promotionScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromoterEntity savePromoter(NxCustomerPromoterEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (entity.getPromoterName() == null || entity.getPromoterName().trim().isEmpty()) {
            throw new IllegalArgumentException("promoterName不能为空");
        }
        if (entity.getPromoterType() == null || entity.getPromoterType().trim().isEmpty()) {
            throw new IllegalArgumentException("promoterType不能为空");
        }
        if (entity.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId不能为空");
        }
        if (entity.getCommerceId() == null) {
            Integer commerceId = promotionScopeService.resolveCommerceIdByCommunityId(entity.getCommunityId());
            if (commerceId == null) {
                throw new IllegalArgumentException("无法根据 communityId 解析 commerceId");
            }
            entity.setCommerceId(commerceId);
        }
        entity.setPromoterName(entity.getPromoterName().trim());
        if (entity.getPromoterStatus() == null) {
            entity.setPromoterStatus(CustomerReferralConstants.PROMOTER_STATUS_ACTIVE);
        }
        if (entity.getNxCustomerPromoterId() == null) {
            nxCustomerPromoterDao.save(entity);
            nxCustomerPromotionCodeService.createCode(
                    CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER,
                    entity.getNxCustomerPromoterId(),
                    entity.getCommerceId(),
                    entity.getCommunityId(),
                    entity.getCooperationStartAt(),
                    entity.getCooperationEndAt(),
                    null);
        } else {
            nxCustomerPromoterDao.update(entity);
        }
        fillPromotionCode(entity);
        return entity;
    }

    @Override
    public NxCustomerPromoterEntity queryObject(Integer id) {
        NxCustomerPromoterEntity promoter = nxCustomerPromoterDao.queryObject(id);
        if (promoter != null) {
            promoter.setQualifiedReferralCount(nxCustomerPromoterDao.countQualifiedReferrals(id));
            fillPromotionCode(promoter);
        }
        return promoter;
    }

    @Override
    public List<NxCustomerPromoterEntity> queryList(Map<String, Object> map) {
        List<NxCustomerPromoterEntity> list = nxCustomerPromoterDao.queryList(map);
        for (NxCustomerPromoterEntity item : list) {
            item.setQualifiedReferralCount(
                    nxCustomerPromoterDao.countQualifiedReferrals(item.getNxCustomerPromoterId()));
            fillPromotionCode(item);
        }
        return list;
    }

    private void fillPromotionCode(NxCustomerPromoterEntity promoter) {
        NxCustomerPromotionCodeEntity code = nxCustomerPromotionCodeService.queryActiveByOwner(
                CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER,
                promoter.getNxCustomerPromoterId());
        if (code == null) {
            code = nxCustomerPromotionCodeService.queryLatestByOwner(
                    CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER,
                    promoter.getNxCustomerPromoterId());
        }
        if (code != null) {
            promoter.setPromotionCode(code.getPromotionCode());
            promoter.setPromotionCodeId(code.getNxCustomerPromotionCodeId());
            promoter.setPromotionCodeStatus(code.getCodeStatus());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activate(Integer promoterId) {
        return updateStatus(promoterId, CustomerReferralConstants.PROMOTER_STATUS_ACTIVE, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean suspend(Integer promoterId, String reason) {
        return updateStatus(promoterId, CustomerReferralConstants.PROMOTER_STATUS_SUSPENDED, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean terminate(Integer promoterId, String reason) {
        return updateStatus(promoterId, CustomerReferralConstants.PROMOTER_STATUS_TERMINATED, reason);
    }

    private boolean updateStatus(Integer promoterId, String status, String reason) {
        NxCustomerPromoterEntity update = new NxCustomerPromoterEntity();
        update.setNxCustomerPromoterId(promoterId);
        update.setPromoterStatus(status);
        if (CustomerReferralConstants.PROMOTER_STATUS_TERMINATED.equals(status)
                || CustomerReferralConstants.PROMOTER_STATUS_SUSPENDED.equals(status)) {
            update.setDisabledAt(new Date());
            update.setDisabledReason(reason);
            nxCustomerPromotionCodeService.deactivateAllActiveCodesByOwner(
                    CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER, promoterId,
                    CustomerReferralConstants.PROMOTER_STATUS_TERMINATED.equals(status) ? "推广员终止合作" : "推广员暂停");
        }
        return nxCustomerPromoterDao.update(update) > 0;
    }

    @Override
    public boolean updateCooperationPeriod(Integer promoterId, Date startAt, Date endAt) {
        NxCustomerPromoterEntity update = new NxCustomerPromoterEntity();
        update.setNxCustomerPromoterId(promoterId);
        update.setCooperationStartAt(startAt);
        update.setCooperationEndAt(endAt);
        return nxCustomerPromoterDao.update(update) > 0;
    }

    @Override
    public Map<String, Object> queryPromoterStats(Integer promoterId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("qualifiedReferralCount", nxCustomerPromoterDao.countQualifiedReferrals(promoterId));
        Map<String, Object> q = new HashMap<>();
        q.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER);
        q.put("sourceOwnerId", promoterId);
        stats.put("totalReferralCount", nxCustomerUserReferralDao.countBySourceOwner(q));
        return stats;
    }

    @Override
    public List<NxCustomerUserReferralEntity> queryReferralDetails(Integer promoterId, Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER);
        map.put("sourceOwnerId", promoterId);
        map.put("offset", offset);
        map.put("limit", limit);
        return nxCustomerUserReferralDao.queryBySourceOwner(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deletePromoter(Integer promoterId) {
        if (promoterId == null) {
            throw new IllegalArgumentException("promoterId不能为空");
        }
        NxCustomerPromoterEntity promoter = nxCustomerPromoterDao.queryObject(promoterId);
        if (promoter == null) {
            throw new IllegalArgumentException("推广人员不存在");
        }

        String ownerType = CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER;
        Map<String, Object> ownerMap = new HashMap<>();
        ownerMap.put("ownerType", ownerType);
        ownerMap.put("ownerId", promoterId);
        ownerMap.put("sourceOwnerType", ownerType);
        ownerMap.put("sourceOwnerId", promoterId);

        int deletedRewards = nxCustomerReferralRewardDao.deleteByPromoter(ownerMap);
        int deletedReferrals = nxCustomerUserReferralDao.deleteByPromoter(ownerMap);
        int deletedAttempts = nxCustomerPromotionCodeAttemptDao.deleteBySourceOwner(ownerMap);
        int deletedCodes = nxCustomerPromotionCodeDao.deleteByOwner(ownerMap);
        nxCustomerPromotionCodeOwnerLockDao.deleteByOwner(ownerType, promoterId);
        int deletedPromoter = nxCustomerPromoterDao.delete(promoterId);
        if (deletedPromoter <= 0) {
            throw new IllegalStateException("删除推广人员失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("promoterId", promoterId);
        result.put("deletedRewardCount", deletedRewards);
        result.put("deletedReferralCount", deletedReferrals);
        result.put("deletedAttemptCount", deletedAttempts);
        result.put("deletedPromotionCodeCount", deletedCodes);
        return result;
    }
}
