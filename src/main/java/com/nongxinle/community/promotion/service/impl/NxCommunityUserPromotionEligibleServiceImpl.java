package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCommunityUserPromotionEligibleDao;
import com.nongxinle.entity.NxCommunityUserPromotionEligibleEntity;
import com.nongxinle.community.promotion.service.NxCommunityUserPromotionEligibleService;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("nxCommunityUserPromotionEligibleService")
public class NxCommunityUserPromotionEligibleServiceImpl implements NxCommunityUserPromotionEligibleService {

    @Autowired
    private NxCommunityUserPromotionEligibleDao nxCommunityUserPromotionEligibleDao;
    @Autowired
    private NxCustomerPromotionCodeService nxCustomerPromotionCodeService;

    @Override
    public NxCommunityUserPromotionEligibleEntity queryObject(Integer communityUserId) {
        return nxCommunityUserPromotionEligibleDao.queryObject(communityUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCommunityUserPromotionEligibleEntity enable(Integer communityUserId, Date validStartAt, Date validEndAt) {
        if (communityUserId == null) {
            throw new IllegalArgumentException("communityUserId不能为空");
        }
        NxCommunityUserPromotionEligibleEntity existing = nxCommunityUserPromotionEligibleDao.queryObject(communityUserId);
        if (existing == null) {
            NxCommunityUserPromotionEligibleEntity entity = new NxCommunityUserPromotionEligibleEntity();
            entity.setCommunityUserId(communityUserId);
            entity.setEnabled(1);
            entity.setValidStartAt(validStartAt);
            entity.setValidEndAt(validEndAt);
            nxCommunityUserPromotionEligibleDao.save(entity);
            return entity;
        }
        NxCommunityUserPromotionEligibleEntity update = new NxCommunityUserPromotionEligibleEntity();
        update.setCommunityUserId(communityUserId);
        update.setEnabled(1);
        update.setValidStartAt(validStartAt);
        update.setValidEndAt(validEndAt);
        nxCommunityUserPromotionEligibleDao.update(update);
        return nxCommunityUserPromotionEligibleDao.queryObject(communityUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Integer communityUserId) {
        deactivatePromotionAssets(communityUserId, "推广资格停用");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivatePromotionAssets(Integer communityUserId, String reason) {
        if (communityUserId == null) {
            return;
        }
        NxCommunityUserPromotionEligibleEntity existing = nxCommunityUserPromotionEligibleDao.queryObject(communityUserId);
        if (existing == null) {
            NxCommunityUserPromotionEligibleEntity entity = new NxCommunityUserPromotionEligibleEntity();
            entity.setCommunityUserId(communityUserId);
            entity.setEnabled(0);
            nxCommunityUserPromotionEligibleDao.save(entity);
        } else {
            NxCommunityUserPromotionEligibleEntity update = new NxCommunityUserPromotionEligibleEntity();
            update.setCommunityUserId(communityUserId);
            update.setEnabled(0);
            nxCommunityUserPromotionEligibleDao.update(update);
        }
        nxCustomerPromotionCodeService.deactivateAllActiveCodesByOwner(
                CustomerReferralConstants.OWNER_TYPE_COMMUNITY_USER, communityUserId, reason);
    }
}
