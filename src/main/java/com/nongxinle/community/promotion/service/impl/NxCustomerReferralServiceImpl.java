package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerPromotionCodeAttemptDao;
import com.nongxinle.dao.NxCustomerPromotionCodeDao;
import com.nongxinle.dao.NxCustomerReferralReadStateDao;
import com.nongxinle.dao.NxCustomerReferralRewardDao;
import com.nongxinle.dao.NxCustomerUserReferralDao;
import com.nongxinle.dto.PromotionResolveResult;
import com.nongxinle.entity.NxCustomerPromotionCodeAttemptEntity;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerReferralReadStateEntity;
import com.nongxinle.entity.NxCustomerReferralRewardEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardService;
import com.nongxinle.community.promotion.service.NxCustomerReferralService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerReferralService")
public class NxCustomerReferralServiceImpl implements NxCustomerReferralService {

    @Autowired
    private NxCustomerUserReferralDao nxCustomerUserReferralDao;
    @Autowired
    private NxCustomerReferralRewardDao nxCustomerReferralRewardDao;
    @Autowired
    private NxCustomerReferralReadStateDao nxCustomerReferralReadStateDao;
    @Autowired
    private NxCustomerPromotionCodeService nxCustomerPromotionCodeService;
    @Autowired
    private NxCustomerPromotionCodeDao nxCustomerPromotionCodeDao;
    @Autowired
    private NxCustomerPromotionCodeAttemptDao nxCustomerPromotionCodeAttemptDao;
    @Autowired
    private NxCustomerReferralRewardService nxCustomerReferralRewardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPromotionAfterRegister(NxCustomerUserEntity newUser, String promotionCode, String shareEntry) {
        if (newUser == null || newUser.getNxCuUserId() == null) {
            return;
        }
        if (promotionCode == null || promotionCode.trim().isEmpty()) {
            return;
        }

        NxCustomerUserReferralEntity existingQualified = nxCustomerUserReferralDao.queryByInviteeUserId(newUser.getNxCuUserId());
        if (existingQualified != null) {
            return;
        }

        String codeInput = promotionCode.trim();
        PromotionResolveResult resolved = nxCustomerPromotionCodeService.resolvePromotionCode(codeInput, newUser);
        if (!resolved.isRecordReferral()) {
            return;
        }

        NxCustomerPromotionCodeEntity codeEntity = resolved.getPromotionCode();
        if (codeEntity != null && codeEntity.getNxCustomerPromotionCodeId() != null) {
            nxCustomerPromotionCodeDao.incrementUseCount(codeEntity.getNxCustomerPromotionCodeId());
        }

        if (!resolved.isQualified()) {
            saveAttempt(newUser, codeInput, shareEntry, resolved);
            if (codeEntity != null && codeEntity.getNxCustomerPromotionCodeId() != null) {
                nxCustomerPromotionCodeDao.incrementInvalidRegisterCount(codeEntity.getNxCustomerPromotionCodeId());
            }
            return;
        }

        NxCustomerUserReferralEntity referral = buildQualifiedReferral(newUser, codeInput, shareEntry, resolved);
        try {
            nxCustomerUserReferralDao.save(referral);
        } catch (DuplicateKeyException e) {
            return;
        }

        if (referral.getPromotionCodeId() != null) {
            nxCustomerPromotionCodeDao.incrementValidRegisterCount(referral.getPromotionCodeId());
        }

        if (resolved.isRewardQualified()
                && CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER.equals(referral.getSourceOwnerType())) {
            nxCustomerReferralRewardService.createRewardsForNewReferral(
                    newUser, referral.getNxCustomerUserReferralId(),
                    referral.getSourceOwnerId(), resolved.getMatchedRule());
        } else if (resolved.isRewardRuleConflict()
                && CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER.equals(referral.getSourceOwnerType())) {
            nxCustomerReferralRewardService.recordRewardRuleConflict(
                    newUser, referral.getNxCustomerUserReferralId(),
                    referral.getSourceOwnerId(), resolved.getRewardConflictReason());
        }
    }

    private void saveAttempt(NxCustomerUserEntity newUser, String codeInput, String shareEntry,
                             PromotionResolveResult resolved) {
        NxCustomerPromotionCodeAttemptEntity attempt = new NxCustomerPromotionCodeAttemptEntity();
        attempt.setInviteeUserId(newUser.getNxCuUserId());
        attempt.setPromotionCodeSnapshot(codeInput);
        attempt.setCommerceId(newUser.getNxCuCommerceId());
        attempt.setCommunityId(newUser.getNxCuCommunityId());
        attempt.setShareEntry(shareEntry);
        attempt.setInvalidReason(resolved.getInvalidReason());
        attempt.setAttemptedAt(new Date());

        NxCustomerPromotionCodeEntity codeEntity = resolved.getPromotionCode();
        if (codeEntity != null) {
            attempt.setPromotionCodeId(codeEntity.getNxCustomerPromotionCodeId());
            attempt.setSourceOwnerType(codeEntity.getOwnerType());
            attempt.setSourceOwnerId(codeEntity.getOwnerId());
        } else {
            attempt.setSourceOwnerType(resolved.getOwnerType());
            attempt.setSourceOwnerId(resolved.getOwnerId());
        }
        nxCustomerPromotionCodeAttemptDao.save(attempt);
    }

    private NxCustomerUserReferralEntity buildQualifiedReferral(NxCustomerUserEntity newUser, String codeInput,
                                                                 String shareEntry, PromotionResolveResult resolved) {
        NxCustomerUserReferralEntity referral = new NxCustomerUserReferralEntity();
        referral.setInviteeUserId(newUser.getNxCuUserId());
        referral.setPromotionCodeSnapshot(codeInput);
        referral.setCommerceId(newUser.getNxCuCommerceId());
        referral.setCommunityId(newUser.getNxCuCommunityId());
        referral.setShareEntry(shareEntry);
        referral.setBindTime(new Date());
        referral.setQualificationStatus(CustomerReferralConstants.QUALIFICATION_QUALIFIED);
        referral.setRewardQualified(resolved.isRewardQualified() ? 1 : 0);

        NxCustomerPromotionCodeEntity codeEntity = resolved.getPromotionCode();
        if (codeEntity != null) {
            referral.setPromotionCodeId(codeEntity.getNxCustomerPromotionCodeId());
            referral.setSourceOwnerType(codeEntity.getOwnerType());
            referral.setSourceOwnerId(codeEntity.getOwnerId());
            if (CustomerReferralConstants.OWNER_TYPE_EXTERNAL_PROMOTER.equals(codeEntity.getOwnerType())) {
                referral.setPromoterId(codeEntity.getOwnerId());
            }
        }
        if (resolved.getMatchedRule() != null) {
            referral.setRewardRuleId(resolved.getMatchedRule().getNxCustomerReferralRewardRuleId());
        }
        if (resolved.getMatchedCampaign() != null) {
            referral.setCampaignId(resolved.getMatchedCampaign().getNxCustomerPromotionCampaignId());
        }
        return referral;
    }

    @Override
    public Map<String, Object> queryHomeNoticeSummary(Integer userId) {
        int lastReadId = getLastReadReferralId(userId);
        Date displaySince = displaySinceDate();

        Map<String, Object> q = sourceOwnerMap(userId);
        q.put("lastReadReferralId", lastReadId);
        q.put("displaySince", displaySince);

        int newCount = nxCustomerUserReferralDao.countUnreadDirectInDisplayWindow(q);
        NxCustomerUserReferralEntity latest = null;
        if (newCount > 0) {
            latest = nxCustomerUserReferralDao.queryLatestUnreadDirectInWindow(q);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("hasUnreadReferralUpdate", newCount > 0);
        summary.put("newRegisteredCount", newCount);
        if (latest != null && latest.getInviteeUser() != null) {
            summary.put("latestRegisteredUserName", latest.getInviteeUser().getNxCuWxNickName());
            summary.put("latestRegisteredAt", latest.getBindTime());
        } else {
            summary.put("latestRegisteredUserName", null);
            summary.put("latestRegisteredAt", null);
        }
        summary.put("displayUntil", calcDisplayUntil(latest != null ? latest.getBindTime() : null));
        summary.put("readWatermarkReferralId", nxCustomerUserReferralDao.queryMaxDirectReferralIdBySourceOwner(sourceOwnerMap(userId)));
        return summary;
    }

    @Override
    public Map<String, Object> queryMyReferralOverview(Integer userId, Integer directOffset, Integer directLimit) {
        Map<String, Object> overview = new HashMap<>();
        overview.put("stats", queryMyReferralStats(userId));
        overview.put("homeNotice", queryHomeNoticeSummary(userId));
        overview.put("directList", queryDirectReferralList(userId, directOffset, directLimit));
        overview.put("pendingRewards", nxCustomerReferralRewardService.queryRewardList(
                userId, CustomerReferralConstants.REWARD_STATUS_PENDING, 0, 10));
        overview.put("readWatermarkReferralId",
                nxCustomerUserReferralDao.queryMaxDirectReferralIdBySourceOwner(sourceOwnerMap(userId)));
        return overview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markDynamicsRead(Integer userId, Integer upToReferralId) {
        if (userId == null || upToReferralId == null || upToReferralId <= 0) {
            return false;
        }
        Integer maxId = nxCustomerUserReferralDao.queryMaxDirectReferralIdBySourceOwner(sourceOwnerMap(userId));
        if (maxId == null || upToReferralId > maxId) {
            upToReferralId = maxId;
        }
        if (upToReferralId == null || upToReferralId <= 0) {
            return false;
        }

        int lastRead = getLastReadReferralId(userId);
        if (upToReferralId <= lastRead) {
            return false;
        }

        NxCustomerReferralReadStateEntity state = new NxCustomerReferralReadStateEntity();
        state.setUserId(userId);
        state.setLastReadReferralId(upToReferralId);
        state.setLastReadAt(new Date());

        NxCustomerReferralReadStateEntity existing = nxCustomerReferralReadStateDao.queryByUserId(userId);
        if (existing == null) {
            nxCustomerReferralReadStateDao.save(state);
            return true;
        }
        return nxCustomerReferralReadStateDao.update(state) > 0;
    }

    @Override
    public Map<String, Object> queryMyReferralStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> sourceMap = sourceOwnerMap(userId);
        stats.put("directCount", nxCustomerUserReferralDao.countDirectBySourceOwner(sourceMap));
        stats.put("level2Count", nxCustomerUserReferralDao.countLevel2BySourceOwner(sourceMap));

        Map<String, Object> pendingMap = new HashMap<>();
        pendingMap.put("beneficiaryUserId", userId);
        pendingMap.put("status", CustomerReferralConstants.REWARD_STATUS_PENDING);
        stats.put("pendingRewardCount", nxCustomerReferralRewardDao.countByBeneficiaryAndStatus(pendingMap));

        Map<String, Object> claimedMap = new HashMap<>();
        claimedMap.put("beneficiaryUserId", userId);
        claimedMap.put("status", CustomerReferralConstants.REWARD_STATUS_CLAIMED);
        stats.put("claimedRewardCount", nxCustomerReferralRewardDao.countByBeneficiaryAndStatus(claimedMap));
        return stats;
    }

    @Override
    public List<Map<String, Object>> queryDirectReferralList(Integer userId, Integer offset, Integer limit) {
        Map<String, Object> map = sourceOwnerMap(userId);
        map.put("offset", offset);
        map.put("limit", limit);
        List<NxCustomerUserReferralEntity> list = nxCustomerUserReferralDao.queryDirectList(map);
        return toReferralItemList(list, false, userId);
    }

    @Override
    public List<Map<String, Object>> queryLevel2ReferralList(Integer userId, Integer offset, Integer limit) {
        Map<String, Object> map = sourceOwnerMap(userId);
        map.put("offset", offset);
        map.put("limit", limit);
        List<NxCustomerUserReferralEntity> list = nxCustomerUserReferralDao.queryLevel2List(map);
        return toReferralItemList(list, true, userId);
    }

    private Map<String, Object> sourceOwnerMap(Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER);
        map.put("sourceOwnerId", userId);
        return map;
    }

    private List<Map<String, Object>> toReferralItemList(List<NxCustomerUserReferralEntity> list, boolean level2,
                                                           Integer inviterUserId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (NxCustomerUserReferralEntity item : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("referralId", item.getNxCustomerUserReferralId());
            row.put("bindTime", item.getBindTime());
            row.put("communityId", item.getCommunityId());
            row.put("rewardQualified", item.getRewardQualified());
            if (item.getInviteeUser() != null) {
                Map<String, Object> invitee = new HashMap<>();
                invitee.put("userId", item.getInviteeUser().getNxCuUserId());
                invitee.put("nickName", item.getInviteeUser().getNxCuWxNickName());
                invitee.put("avatarUrl", item.getInviteeUser().getNxCuWxAvatarUrl());
                invitee.put("phone", maskPhone(item.getInviteeUser().getNxCuWxPhoneNumber()));
                invitee.put("joinDate", item.getInviteeUser().getNxCuJoinDate());
                row.put("invitee", invitee);
            }
            if (!level2) {
                row.put("subReferralCount", item.getSubReferralCount() != null ? item.getSubReferralCount() : 0);
                row.put("rewardStatus", queryDirectReferralRewardStatus(inviterUserId, item.getInviteeUserId()));
            }
            if (level2 && item.getViaDirectFriend() != null) {
                Map<String, Object> via = new HashMap<>();
                via.put("userId", item.getViaDirectFriend().getNxCuUserId());
                via.put("nickName", item.getViaDirectFriend().getNxCuWxNickName());
                via.put("avatarUrl", item.getViaDirectFriend().getNxCuWxAvatarUrl());
                row.put("viaDirectFriend", via);
            }
            result.add(row);
        }
        return result;
    }

    private Integer queryDirectReferralRewardStatus(Integer inviterUserId, Integer triggerUserId) {
        if (inviterUserId == null || triggerUserId == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("beneficiaryUserId", inviterUserId);
        map.put("triggerUserId", triggerUserId);
        List<NxCustomerReferralRewardEntity> rewards = nxCustomerReferralRewardDao.queryList(map);
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }
        return rewards.get(0).getStatus();
    }

    private int getLastReadReferralId(Integer userId) {
        NxCustomerReferralReadStateEntity state = nxCustomerReferralReadStateDao.queryByUserId(userId);
        if (state == null || state.getLastReadReferralId() == null) {
            return 0;
        }
        return state.getLastReadReferralId();
    }

    private Date displaySinceDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -CustomerReferralConstants.HOME_NOTICE_DISPLAY_DAYS);
        return cal.getTime();
    }

    private Date calcDisplayUntil(Date latestBindTime) {
        if (latestBindTime == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(latestBindTime);
        cal.add(Calendar.DAY_OF_YEAR, CustomerReferralConstants.HOME_NOTICE_DISPLAY_DAYS);
        return cal.getTime();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
