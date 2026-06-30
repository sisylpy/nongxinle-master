package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerPromotionCodeDao;
import com.nongxinle.dao.NxCustomerPromotionCodeOwnerLockDao;
import com.nongxinle.dto.PromotionOwnerValidateResult;
import com.nongxinle.dto.PromotionOwnerValidationContext;
import com.nongxinle.dto.PromotionResolveResult;
import com.nongxinle.dto.RewardRuleResolveResult;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.customer.service.NxCommunityUserService;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCampaignService;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardRuleService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.community.promotion.service.PromotionOwnerValidatorRegistry;
import com.nongxinle.community.promotion.service.PromotionScopeService;
import com.nongxinle.utils.CustomerReferralConstants;
import com.nongxinle.utils.PromotionCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerPromotionCodeService")
public class NxCustomerPromotionCodeServiceImpl implements NxCustomerPromotionCodeService {

    @Autowired
    private NxCustomerPromotionCodeDao nxCustomerPromotionCodeDao;
    @Autowired
    private NxCustomerPromotionCodeOwnerLockDao nxCustomerPromotionCodeOwnerLockDao;
    @Autowired
    private NxCustomerPromotionCampaignService nxCustomerPromotionCampaignService;
    @Autowired
    private NxCustomerReferralRewardRuleService nxCustomerReferralRewardRuleService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCommunityUserService nxCommunityUserService;
    @Autowired
    private PromotionScopeService promotionScopeService;
    @Autowired
    private PromotionCodeGenerator promotionCodeGenerator;
    @Autowired
    private PromotionOwnerValidatorRegistry promotionOwnerValidatorRegistry;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCodeEntity getOrCreateForCustomerUser(NxCustomerUserEntity user) {
        if (user == null || user.getNxCuUserId() == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String ownerType = CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER;
        Integer ownerId = user.getNxCuUserId();
        NxCustomerPromotionCodeEntity existing = queryActiveByOwner(ownerType, ownerId);
        if (existing != null) {
            return existing;
        }
        return getOrCreateWithLock(ownerType, ownerId, () -> createCodeUnlocked(
                ownerType, ownerId, user.getNxCuCommerceId(), user.getNxCuCommunityId(), null, null, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCodeEntity getOrCreateForCommunityUser(Integer communityUserId) {
        if (communityUserId == null) {
            throw new IllegalArgumentException("communityUserId不能为空");
        }
        NxCommunityUserEntity staff = nxCommunityUserService.queryObject(communityUserId);
        if (staff == null) {
            throw new IllegalArgumentException("社区工作人员不存在");
        }
        String ownerType = CustomerReferralConstants.OWNER_TYPE_COMMUNITY_USER;
        NxCustomerPromotionCodeEntity existing = queryActiveByOwner(ownerType, communityUserId);
        if (existing != null) {
            return existing;
        }
        Integer commerceId = promotionScopeService.resolveCommerceIdByCommunityId(staff.getNxCouCommunityId());
        return getOrCreateWithLock(ownerType, communityUserId, () -> createCodeUnlocked(
                ownerType, communityUserId, commerceId, staff.getNxCouCommunityId(), null, null, null));
    }

    @Override
    public PromotionResolveResult resolvePromotionCode(String promotionCode, NxCustomerUserEntity newUser) {
        if (promotionCode == null || promotionCode.trim().isEmpty()) {
            return PromotionResolveResult.noCode();
        }
        if (newUser == null || newUser.getNxCuUserId() == null) {
            return PromotionResolveResult.noCode();
        }

        String codeStr = promotionCode.trim();
        NxCustomerPromotionCodeEntity code = nxCustomerPromotionCodeDao.queryByCode(codeStr);
        Date now = new Date();

        if (code == null) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CODE_NOT_FOUND, null);
        }

        PromotionResolveResult codeStatusResult = validateCodeStatus(code, now);
        if (codeStatusResult != null) {
            return codeStatusResult;
        }

        PromotionOwnerValidationContext ownerContext = new PromotionOwnerValidationContext();
        ownerContext.setPromotionCode(code);
        ownerContext.setNewUser(newUser);
        ownerContext.setNow(now);

        PromotionOwnerValidateResult ownerResult =
                promotionOwnerValidatorRegistry.validate(code.getOwnerType(), ownerContext);
        if (!ownerResult.isValid()) {
            return PromotionResolveResult.attempt(ownerResult.getInvalidReason(), code);
        }

        return buildQualifiedResult(code, ownerResult, newUser.getNxCuCommunityId());
    }

    private PromotionResolveResult validateCodeStatus(NxCustomerPromotionCodeEntity code, Date now) {
        if (CustomerReferralConstants.CODE_STATUS_DISABLED.equals(code.getCodeStatus())) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CODE_DISABLED, code);
        }
        if (CustomerReferralConstants.CODE_STATUS_SUSPENDED.equals(code.getCodeStatus())) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CODE_SUSPENDED, code);
        }
        if (code.getValidStartAt() != null && now.before(code.getValidStartAt())) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CODE_NOT_STARTED, code);
        }
        if (code.getValidEndAt() != null && now.after(code.getValidEndAt())) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CODE_EXPIRED, code);
        }
        return null;
    }

    private PromotionResolveResult buildQualifiedResult(NxCustomerPromotionCodeEntity code,
                                                         PromotionOwnerValidateResult ownerResult,
                                                         Integer communityId) {
        String ownerType = code.getOwnerType();
        Integer ownerId = code.getOwnerId();
        Integer externalPromoterId = ownerResult.getExternalPromoterId();

        if (CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER.equals(ownerType)) {
            RewardRuleResolveResult ruleResult = resolveActiveRewardRule(communityId,
                    CustomerReferralConstants.BENEFICIARY_TYPE_CUSTOMER_USER);
            if (ruleResult.isAmbiguous()) {
                PromotionResolveResult r = PromotionResolveResult.success(code, ownerType, ownerId, externalPromoterId,
                        false, null, null);
                r.setRewardRuleConflict(true);
                r.setRewardConflictReason(CustomerReferralConstants.INVALID_RULE_AMBIGUOUS);
                return r;
            }
            boolean rewardQualified = ruleResult.getMatchedRule() != null;
            return PromotionResolveResult.success(code, ownerType, ownerId, externalPromoterId,
                    rewardQualified, null, ruleResult.getMatchedRule());
        }

        Integer commerceId = code.getCommerceId();
        if (commerceId == null && communityId != null) {
            commerceId = promotionScopeService.resolveCommerceIdByCommunityId(communityId);
        }
        try {
            NxCustomerPromotionCampaignEntity campaign = nxCustomerPromotionCampaignService.resolveActiveCampaign(
                    communityId, commerceId, ownerType, new Date());
            if (campaign == null) {
                return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CAMPAIGN_NOT_ACTIVE, code);
            }
            return PromotionResolveResult.success(code, ownerType, ownerId, externalPromoterId,
                    false, campaign, null);
        } catch (IllegalStateException e) {
            return PromotionResolveResult.attempt(CustomerReferralConstants.INVALID_CAMPAIGN_AMBIGUOUS, code);
        }
    }

    private RewardRuleResolveResult resolveActiveRewardRule(Integer communityId, String beneficiaryType) {
        return nxCustomerReferralRewardRuleService.resolveRegisterRule(communityId, beneficiaryType, new Date());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCodeEntity createCode(String ownerType, Integer ownerId, Integer commerceId,
                                                     Integer communityId, Date validStartAt, Date validEndAt,
                                                     Integer rewardRuleId) {
        return getOrCreateWithLock(ownerType, ownerId, () -> createCodeUnlocked(
                ownerType, ownerId, commerceId, communityId, validStartAt, validEndAt, rewardRuleId));
    }

    private NxCustomerPromotionCodeEntity createCodeUnlocked(String ownerType, Integer ownerId, Integer commerceId,
                                                              Integer communityId, Date validStartAt, Date validEndAt,
                                                              Integer rewardRuleId) {
        deactivateAllActiveCodesByOwnerUnlocked(ownerType, ownerId, "创建新码前停用旧ACTIVE码");
        for (int attempt = 0; attempt < 5; attempt++) {
            NxCustomerPromotionCodeEntity entity = new NxCustomerPromotionCodeEntity();
            entity.setPromotionCode(promotionCodeGenerator.generateUniqueCode());
            entity.setOwnerType(ownerType);
            entity.setOwnerId(ownerId);
            entity.setCommerceId(commerceId);
            entity.setCommunityId(communityId);
            entity.setCodeStatus(CustomerReferralConstants.CODE_STATUS_ACTIVE);
            entity.setValidStartAt(validStartAt);
            entity.setValidEndAt(validEndAt);
            entity.setRewardRuleId(rewardRuleId);
            entity.setUseCount(0);
            entity.setValidRegisterCount(0);
            entity.setInvalidRegisterCount(0);
            try {
                nxCustomerPromotionCodeDao.save(entity);
                assertSingleActiveCode(ownerType, ownerId);
                return entity;
            } catch (DuplicateKeyException e) {
                NxCustomerPromotionCodeEntity dup = nxCustomerPromotionCodeDao.queryByCode(entity.getPromotionCode());
                if (dup != null) {
                    continue;
                }
                NxCustomerPromotionCodeEntity active = queryActiveByOwner(ownerType, ownerId);
                if (active != null) {
                    return active;
                }
                throw new IllegalStateException("推广码并发冲突，请重试");
            }
        }
        throw new IllegalStateException("推广码创建失败，请重试");
    }

    private NxCustomerPromotionCodeEntity getOrCreateWithLock(String ownerType, Integer ownerId,
                                                               java.util.function.Supplier<NxCustomerPromotionCodeEntity> createAction) {
        lockOwner(ownerType, ownerId);
        NxCustomerPromotionCodeEntity existing = queryActiveByOwner(ownerType, ownerId);
        if (existing != null) {
            return existing;
        }
        return createAction.get();
    }

    private void lockOwner(String ownerType, Integer ownerId) {
        nxCustomerPromotionCodeOwnerLockDao.ensureLockRow(ownerType, ownerId);
        nxCustomerPromotionCodeOwnerLockDao.lockOwnerForUpdate(ownerType, ownerId);
    }

    private void assertSingleActiveCode(String ownerType, Integer ownerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerType", ownerType);
        map.put("ownerId", ownerId);
        int activeCount = nxCustomerPromotionCodeDao.countActiveByOwner(map);
        if (activeCount > 1) {
            throw new IllegalStateException("同一主体存在多个ACTIVE推广码，请检查数据");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxCustomerPromotionCodeEntity regenerateCode(Integer promotionCodeId, String disabledReason) {
        NxCustomerPromotionCodeEntity old = nxCustomerPromotionCodeDao.queryObject(promotionCodeId);
        if (old == null) {
            throw new IllegalArgumentException("推广码不存在");
        }
        return getOrCreateWithLock(old.getOwnerType(), old.getOwnerId(), () -> {
            NxCustomerPromotionCodeEntity disable = new NxCustomerPromotionCodeEntity();
            disable.setNxCustomerPromotionCodeId(promotionCodeId);
            disable.setCodeStatus(CustomerReferralConstants.CODE_STATUS_DISABLED);
            disable.setDisabledAt(new Date());
            disable.setDisabledReason(disabledReason != null ? disabledReason : "重新生成");
            nxCustomerPromotionCodeDao.update(disable);
            return createCodeUnlocked(old.getOwnerType(), old.getOwnerId(), old.getCommerceId(), old.getCommunityId(),
                    old.getValidStartAt(), old.getValidEndAt(), old.getRewardRuleId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateAllActiveCodesByOwner(String ownerType, Integer ownerId, String reason) {
        lockOwner(ownerType, ownerId);
        deactivateAllActiveCodesByOwnerUnlocked(ownerType, ownerId, reason);
    }

    private void deactivateAllActiveCodesByOwnerUnlocked(String ownerType, Integer ownerId, String reason) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerType", ownerType);
        map.put("ownerId", ownerId);
        map.put("disabledAt", new Date());
        map.put("disabledReason", reason != null ? reason : "批量停用");
        nxCustomerPromotionCodeDao.disableAllActiveByOwner(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCodeStatus(Integer promotionCodeId, String codeStatus, String disabledReason) {
        NxCustomerPromotionCodeEntity current = nxCustomerPromotionCodeDao.queryObject(promotionCodeId);
        if (current == null) {
            return false;
        }
        if (CustomerReferralConstants.CODE_STATUS_ACTIVE.equals(codeStatus)) {
            lockOwner(current.getOwnerType(), current.getOwnerId());
            deactivateAllActiveCodesByOwnerUnlocked(current.getOwnerType(), current.getOwnerId(),
                    "启用新码前停用其他ACTIVE码");
        }
        NxCustomerPromotionCodeEntity update = new NxCustomerPromotionCodeEntity();
        update.setNxCustomerPromotionCodeId(promotionCodeId);
        update.setCodeStatus(codeStatus);
        if (CustomerReferralConstants.CODE_STATUS_DISABLED.equals(codeStatus)) {
            update.setDisabledAt(new Date());
            update.setDisabledReason(disabledReason);
        }
        boolean ok = nxCustomerPromotionCodeDao.update(update) > 0;
        if (ok && CustomerReferralConstants.CODE_STATUS_ACTIVE.equals(codeStatus)) {
            assertSingleActiveCode(current.getOwnerType(), current.getOwnerId());
        }
        return ok;
    }

    @Override
    public boolean updateCodeValidity(Integer promotionCodeId, Date validStartAt, Date validEndAt) {
        NxCustomerPromotionCodeEntity update = new NxCustomerPromotionCodeEntity();
        update.setNxCustomerPromotionCodeId(promotionCodeId);
        update.setValidStartAt(validStartAt);
        update.setValidEndAt(validEndAt);
        return nxCustomerPromotionCodeDao.update(update) > 0;
    }

    @Override
    public NxCustomerPromotionCodeEntity queryObject(Integer id) {
        return nxCustomerPromotionCodeDao.queryObject(id);
    }

    @Override
    public NxCustomerPromotionCodeEntity queryActiveByOwner(String ownerType, Integer ownerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerType", ownerType);
        map.put("ownerId", ownerId);
        NxCustomerPromotionCodeEntity code = nxCustomerPromotionCodeDao.queryActiveByOwner(map);
        if (code != null) {
            assertSingleActiveCode(ownerType, ownerId);
        }
        return code;
    }

    @Override
    public NxCustomerPromotionCodeEntity queryLatestByOwner(String ownerType, Integer ownerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerType", ownerType);
        map.put("ownerId", ownerId);
        return nxCustomerPromotionCodeDao.queryLatestByOwner(map);
    }

    @Override
    public List<NxCustomerPromotionCodeEntity> queryList(Map<String, Object> map) {
        return nxCustomerPromotionCodeDao.queryList(map);
    }
}
