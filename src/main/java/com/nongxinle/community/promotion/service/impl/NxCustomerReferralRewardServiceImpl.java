package com.nongxinle.community.promotion.service.impl;

import com.alibaba.fastjson.JSON;
import com.nongxinle.dao.NxCustomerReferralRewardDao;
import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.entity.NxCustomerReferralRewardEntity;
import com.nongxinle.entity.NxCustomerReferralRewardRuleEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardService;
import com.nongxinle.community.promotion.service.NxCustomerReferralService;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerReferralRewardService")
public class NxCustomerReferralRewardServiceImpl implements NxCustomerReferralRewardService {

    @Autowired
    private NxCustomerReferralRewardDao nxCustomerReferralRewardDao;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    @Lazy
    private NxCustomerReferralService nxCustomerReferralService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRewardsForNewReferral(NxCustomerUserEntity newUser, Integer referralId,
                                            Integer beneficiaryUserId,
                                            NxCustomerReferralRewardRuleEntity matchedRule) {
        if (newUser == null || referralId == null || beneficiaryUserId == null || matchedRule == null) {
            return;
        }
        Integer communityId = newUser.getNxCuCommunityId();
        if (communityId == null) {
            return;
        }
        createRewardFromRule(matchedRule, communityId, beneficiaryUserId, newUser.getNxCuUserId(), referralId,
                CustomerReferralConstants.REWARD_LEVEL_DIRECT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRewardRuleConflict(NxCustomerUserEntity newUser, Integer referralId,
                                         Integer beneficiaryUserId, String conflictReason) {
        if (newUser == null || referralId == null || beneficiaryUserId == null) {
            return;
        }
        NxCustomerReferralRewardEntity reward = new NxCustomerReferralRewardEntity();
        reward.setBeneficiaryType(CustomerReferralConstants.BENEFICIARY_TYPE_CUSTOMER_USER);
        reward.setBeneficiaryUserId(beneficiaryUserId);
        reward.setTriggerUserId(newUser.getNxCuUserId());
        reward.setReferralId(referralId);
        reward.setCommunityId(newUser.getNxCuCommunityId());
        reward.setRewardLevel(CustomerReferralConstants.REWARD_LEVEL_DIRECT);
        reward.setRewardKind(CustomerReferralConstants.REWARD_KIND_COUPON);
        reward.setStatus(CustomerReferralConstants.REWARD_STATUS_FAILED);
        reward.setCouponWordsSnapshot(conflictReason != null
                ? conflictReason : CustomerReferralConstants.INVALID_RULE_AMBIGUOUS);
        nxCustomerReferralRewardDao.save(reward);
    }

    private void createRewardFromRule(NxCustomerReferralRewardRuleEntity rule, Integer communityId,
                                      Integer beneficiaryUserId, Integer triggerUserId, Integer referralId,
                                      int rewardLevel) {
        if (rule == null || rule.getEnabled() == null || rule.getEnabled() != 1) {
            return;
        }

        NxCustomerReferralRewardEntity reward = new NxCustomerReferralRewardEntity();
        reward.setBeneficiaryType(CustomerReferralConstants.BENEFICIARY_TYPE_CUSTOMER_USER);
        reward.setBeneficiaryUserId(beneficiaryUserId);
        reward.setTriggerUserId(triggerUserId);
        reward.setReferralId(referralId);
        reward.setCommunityId(communityId);
        reward.setRewardLevel(rewardLevel);
        reward.setRuleId(rule.getNxCustomerReferralRewardRuleId());
        reward.setRewardKind(rule.getRewardKind());
        reward.setStatus(CustomerReferralConstants.REWARD_STATUS_PENDING);

        if (CustomerReferralConstants.REWARD_KIND_COUPON.equals(rule.getRewardKind())) {
            if (rule.getCouponTemplateId() == null) {
                return;
            }
            NxCommunityCouponEntity template = nxCommunityCouponService.queryObject(rule.getCouponTemplateId());
            if (template == null || template.getNxCpStatus() == null || template.getNxCpStatus() != 0) {
                return;
            }
            if (template.getNxCpCommunityId() != null && !template.getNxCpCommunityId().equals(communityId)) {
                return;
            }
            reward.setCouponTemplateId(rule.getCouponTemplateId());
            fillCouponSnapshot(reward, template);
        } else if (CustomerReferralConstants.REWARD_KIND_POINTS.equals(rule.getRewardKind())) {
            reward.setPointsAmount(rule.getPointsAmount());
            saveRewardIdempotent(reward);
            return;
        } else {
            return;
        }

        saveRewardIdempotent(reward);
    }

    private void fillCouponSnapshot(NxCustomerReferralRewardEntity reward, NxCommunityCouponEntity template) {
        reward.setCouponNameSnapshot(template.getNxCommunityCouponName());
        reward.setCouponPriceSnapshot(template.getDiscountAmount() != null
                ? template.getDiscountAmount().toPlainString() : "0");
        reward.setCouponOriginalPriceSnapshot(null);
        reward.setCouponWordsSnapshot(template.getNxCpWords());
        reward.setCouponStartDateSnapshot(template.getNxCpStartDate());
        reward.setCouponStopDateSnapshot(template.getNxCpStopDate());
        String validityType = template.getNxCpValidityType();
        reward.setCouponValidityTypeSnapshot(validityType != null
                ? validityType : CustomerReferralConstants.VALIDITY_FIXED_DATE);
        reward.setCouponValidityDaysSnapshot(template.getNxCpValidityDays());
    }

    private void saveRewardIdempotent(NxCustomerReferralRewardEntity reward) {
        try {
            nxCustomerReferralRewardDao.save(reward);
        } catch (DuplicateKeyException e) {
            Map<String, Object> key = new HashMap<>();
            key.put("beneficiaryUserId", reward.getBeneficiaryUserId());
            key.put("triggerUserId", reward.getTriggerUserId());
            key.put("ruleId", reward.getRuleId());
            NxCustomerReferralRewardEntity existing = nxCustomerReferralRewardDao.queryByUniqueKey(key);
            if (existing == null) {
                throw e;
            }
        }
    }

    @Override
    public List<NxCustomerReferralRewardEntity> queryRewardList(Integer userId, Integer status,
                                                                 Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("beneficiaryUserId", userId);
        map.put("status", status);
        map.put("offset", offset);
        map.put("limit", limit);
        return nxCustomerReferralRewardDao.queryList(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerUserCouponEntity claimReward(Integer userId, Integer rewardId) {
        return doClaimReward(userId, rewardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimRewardsBatch(Integer userId, String rewardIds, Boolean claimAll) {
        List<Integer> ids = new ArrayList<>();
        if (Boolean.TRUE.equals(claimAll)) {
            ids = nxCustomerReferralRewardDao.queryPendingRewardIds(userId);
        } else if (rewardIds != null && !rewardIds.trim().isEmpty()) {
            for (String part : rewardIds.split(",")) {
                if (!part.trim().isEmpty()) {
                    ids.add(Integer.parseInt(part.trim()));
                }
            }
        }

        int requestedCount = ids.size();
        int claimedCount = 0;
        int alreadyClaimedCount = 0;
        int failedCount = 0;
        List<Integer> claimedUserCouponIds = new ArrayList<>();
        List<Map<String, Object>> failures = new ArrayList<>();

        for (Integer rewardId : ids) {
            NxCustomerReferralRewardEntity existing = nxCustomerReferralRewardDao.queryObject(rewardId);
            if (existing == null) {
                failedCount++;
                continue;
            }
            if (!existing.getBeneficiaryUserId().equals(userId)) {
                failedCount++;
                continue;
            }
            if (existing.getStatus() == CustomerReferralConstants.REWARD_STATUS_CLAIMED) {
                alreadyClaimedCount++;
                continue;
            }
            try {
                NxCustomerUserCouponEntity coupon = doClaimReward(userId, rewardId);
                if (coupon != null && coupon.getNxCustomerUserCouponId() != null) {
                    claimedUserCouponIds.add(coupon.getNxCustomerUserCouponId());
                }
                claimedCount++;
            } catch (IllegalStateException | IllegalArgumentException e) {
                failedCount++;
                Map<String, Object> fail = new HashMap<>();
                fail.put("rewardId", rewardId);
                fail.put("reason", e.getMessage());
                failures.add(fail);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("requestedCount", requestedCount);
        result.put("claimedCount", claimedCount);
        result.put("alreadyClaimedCount", alreadyClaimedCount);
        result.put("failedCount", failedCount);
        result.put("claimedUserCouponIds", claimedUserCouponIds);
        result.put("failures", failures);
        return result;
    }

    private NxCustomerUserCouponEntity doClaimReward(Integer userId, Integer rewardId) {
        NxCustomerReferralRewardEntity reward = nxCustomerReferralRewardDao.queryObjectForUpdate(rewardId);
        if (reward == null) {
            throw new IllegalArgumentException("奖励不存在");
        }
        if (!reward.getBeneficiaryUserId().equals(userId)) {
            throw new IllegalArgumentException("无权领取该奖励");
        }

        if (reward.getStatus() == CustomerReferralConstants.REWARD_STATUS_CLAIMED) {
            if (reward.getUserCouponId() != null) {
                return nxCustomerUserCouponService.queryUserCouponDetail(reward.getUserCouponId());
            }
            throw new IllegalStateException("奖励已领取");
        }
        if (reward.getStatus() != CustomerReferralConstants.REWARD_STATUS_PENDING) {
            throw new IllegalStateException("奖励不可领取");
        }

        if (!CustomerReferralConstants.REWARD_KIND_COUPON.equals(reward.getRewardKind())) {
            throw new IllegalStateException("当前奖励类型暂不支持领取");
        }

        if (isSnapshotExpired(reward)) {
            markRewardExpired(reward);
            throw new IllegalStateException("奖励已过期");
        }

        NxCommunityCouponEntity template = nxCommunityCouponService.queryObject(reward.getCouponTemplateId());
        if (template == null) {
            markRewardFailed(reward);
            throw new IllegalStateException("优惠券模板不存在");
        }
        if (template.getNxCpStatus() == null || template.getNxCpStatus() != 0) {
            markRewardFailed(reward);
            throw new IllegalStateException("优惠券模板已失效");
        }

        NxCustomerUserCouponEntity userCoupon = new NxCustomerUserCouponEntity();
        userCoupon.setNxCucCouponId(template.getNxCommunityCouponId());
        userCoupon.setNxCucCustomerUserId(userId);
        userCoupon.setNxCucCommunityId(reward.getCommunityId() != null ? reward.getCommunityId() : template.getNxCpCommunityId());
        userCoupon.setNxCucStatus(0);
        userCoupon.setNxCucSourceType(CustomerReferralConstants.SOURCE_REFERRAL_REWARD);
        userCoupon.setNxCucSourceBizId(rewardId);
        applyCouponValidityFromRewardSnapshot(userCoupon, reward);

        try {
            nxCustomerUserCouponService.save(userCoupon);
        } catch (DuplicateKeyException e) {
            Map<String, Object> findMap = new HashMap<>();
            findMap.put("userId", userId);
            findMap.put("sourceType", CustomerReferralConstants.SOURCE_REFERRAL_REWARD);
            findMap.put("sourceBizId", rewardId);
            List<NxCustomerUserCouponEntity> existing = nxCustomerUserCouponService.queryUserCouponListByParams(findMap);
            if (!existing.isEmpty()) {
                completeRewardClaim(reward, existing.get(0).getNxCustomerUserCouponId());
                return existing.get(0);
            }
            throw e;
        }

        template.setNxCpDownCount(template.getNxCpDownCount() != null ? template.getNxCpDownCount() + 1 : 1);
        nxCommunityCouponService.update(template);

        completeRewardClaim(reward, userCoupon.getNxCustomerUserCouponId());
        return userCoupon;
    }

    private void applyCouponValidityFromRewardSnapshot(NxCustomerUserCouponEntity userCoupon,
                                                        NxCustomerReferralRewardEntity reward) {
        String validityType = reward.getCouponValidityTypeSnapshot();
        if (validityType == null || validityType.trim().isEmpty()) {
            validityType = CustomerReferralConstants.VALIDITY_FIXED_DATE;
        }
        LocalDate claimDate = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (CustomerReferralConstants.VALIDITY_DAYS_AFTER_CLAIM.equals(validityType)) {
            int days = reward.getCouponValidityDaysSnapshot() != null ? reward.getCouponValidityDaysSnapshot() : 0;
            if (days < 0) {
                days = 0;
            }
            LocalDate startDate = claimDate;
            LocalDate stopDate = claimDate.plusDays(days);
            userCoupon.setNxCucStartDate(startDate.format(dateFmt));
            userCoupon.setNxCucStopDate(stopDate.format(dateFmt));
        } else {
            userCoupon.setNxCucStartDate(reward.getCouponStartDateSnapshot());
            userCoupon.setNxCucStopDate(reward.getCouponStopDateSnapshot());
        }
        userCoupon.setNxCucShareTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private boolean isSnapshotExpired(NxCustomerReferralRewardEntity reward) {
        if (CustomerReferralConstants.VALIDITY_DAYS_AFTER_CLAIM.equals(reward.getCouponValidityTypeSnapshot())) {
            return false;
        }
        String stopDate = reward.getCouponStopDateSnapshot();
        if (stopDate == null || stopDate.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDateTime stop = LocalDateTime.parse(stopDate + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return LocalDateTime.now().isAfter(stop);
        } catch (Exception e) {
            return false;
        }
    }

    private void completeRewardClaim(NxCustomerReferralRewardEntity reward, Integer userCouponId) {
        reward.setStatus(CustomerReferralConstants.REWARD_STATUS_CLAIMED);
        reward.setUserCouponId(userCouponId);
        reward.setClaimedAt(new Date());
        nxCustomerReferralRewardDao.update(reward);
    }

    private void markRewardFailed(NxCustomerReferralRewardEntity reward) {
        reward.setStatus(CustomerReferralConstants.REWARD_STATUS_FAILED);
        nxCustomerReferralRewardDao.update(reward);
    }

    private void markRewardExpired(NxCustomerReferralRewardEntity reward) {
        reward.setStatus(CustomerReferralConstants.REWARD_STATUS_EXPIRED);
        nxCustomerReferralRewardDao.update(reward);
    }

    @Override
    public void attachLoginExtras(Map<String, Object> loginData, Integer userId, Integer commId) {
        attachPublicCoupons(loginData, userId, commId);
        loginData.put("referralHomeNotice", nxCustomerReferralService.queryHomeNoticeSummary(userId));
    }

    private void attachPublicCoupons(Map<String, Object> loginData, Integer userId, Integer commId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", 0);
        map.put("publicActiveOnly", true);
        List<NxCommunityCouponEntity> couponList = nxCommunityCouponService.queryCustomerShowCoupon(map);
        List<NxCommunityCouponEntity> downCoupons = new ArrayList<>();
        if (couponList != null) {
            for (NxCommunityCouponEntity communityCouponEntity : couponList) {
                Map<String, Object> mapIF = new HashMap<>();
                mapIF.put("coupId", communityCouponEntity.getNxCommunityCouponId());
                mapIF.put("userId", userId);
                mapIF.put("xiaoyuStatus", 4);
                mapIF.put("fromShareUserId", 0);
                List<NxCustomerUserCouponEntity> claimed = nxCustomerUserCouponService.queryUserCouponListByParams(mapIF);
                if (claimed.isEmpty()) {
                    downCoupons.add(communityCouponEntity);
                }
            }
        }
        if (!downCoupons.isEmpty()) {
            loginData.put("coupon", downCoupons.get(0));
            loginData.put("couponList", JSON.toJSONString(downCoupons));
        } else {
            loginData.put("coupon", null);
            loginData.put("couponList", null);
        }
    }

    @Override
    public List<Map<String, Object>> queryReferralCouponGroups(Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("sourceType", CustomerReferralConstants.SOURCE_REFERRAL_REWARD);
        map.put("xiaoyuStatus", 4);
        List<NxCustomerUserCouponEntity> coupons = nxCustomerUserCouponService.queryUserCouponListByParams(map);
        Map<Integer, Map<String, Object>> grouped = new HashMap<>();
        for (NxCustomerUserCouponEntity item : coupons) {
            Integer templateId = item.getNxCucCouponId();
            Map<String, Object> group = grouped.get(templateId);
            if (group == null) {
                group = new HashMap<>();
                group.put("couponTemplateId", templateId);
                if (item.getNxCommunityCouponEntity() != null) {
                    group.put("couponName", item.getNxCommunityCouponEntity().getNxCommunityCouponName());
                    group.put("price", item.getNxCommunityCouponEntity().getDiscountAmount() != null
                            ? item.getNxCommunityCouponEntity().getDiscountAmount().toPlainString() : "0");
                    group.put("originalPrice", null);
                    group.put("words", item.getNxCommunityCouponEntity().getNxCpWords());
                    group.put("stopDate", item.getNxCommunityCouponEntity().getNxCpStopDate());
                }
                group.put("quantity", 0);
                group.put("userCouponIds", new ArrayList<Integer>());
                grouped.put(templateId, group);
            }
            group.put("quantity", (Integer) group.get("quantity") + 1);
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) group.get("userCouponIds");
            ids.add(item.getNxCustomerUserCouponId());
        }
        return new ArrayList<>(grouped.values());
    }
}
