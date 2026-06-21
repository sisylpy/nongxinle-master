package com.nongxinle.service.platform.admin.impl;

import com.nongxinle.dao.PlatformCouponTemplateDao;
import com.nongxinle.dao.PlatformCouponUsageLogDao;
import com.nongxinle.dao.PlatformStoreCouponDao;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateListRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateSaveRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateUpdateRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponIssueRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponListRequest;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.PlatformCouponTemplateEntity;
import com.nongxinle.entity.PlatformCouponUsageLogEntity;
import com.nongxinle.entity.PlatformStoreCouponEntity;
import com.nongxinle.platform.admin.PlatformMarketAdminContext;
import com.nongxinle.service.platform.PlatformStoreDepartmentResolver;
import com.nongxinle.service.platform.admin.PlatformCouponAdminService;
import com.nongxinle.utils.PlatformCouponConstants;
import com.nongxinle.utils.PlatformCouponRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("platformCouponAdminService")
public class PlatformCouponAdminServiceImpl implements PlatformCouponAdminService {

    private static final int DEFAULT_LIST_LIMIT = 50;
    private static final int MAX_ISSUE_COUNT = 500;

    @Autowired
    private PlatformCouponTemplateDao platformCouponTemplateDao;
    @Autowired
    private PlatformStoreCouponDao platformStoreCouponDao;
    @Autowired
    private PlatformCouponUsageLogDao platformCouponUsageLogDao;
    @Autowired
    private PlatformStoreDepartmentResolver platformStoreDepartmentResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCouponTemplateEntity saveTemplate(PlatformCouponTemplateSaveRequest request) {
        AdminScope scope = requireAdminScope();
        PlatformCouponTemplateEntity template = fromSaveRequest(request);
        template.setMarketId(scope.marketId);
        template.setStatus(PlatformCouponConstants.TEMPLATE_STATUS_ACTIVE);
        template.setIssueCount(0);
        template.setUseCount(0);
        template.setCreatedByMarketUserId(scope.marketUserId);
        template.setUpdatedByMarketUserId(scope.marketUserId);
        PlatformCouponRuleValidator.normalizeDefaults(template);
        PlatformCouponRuleValidator.validateTemplate(template);
        platformCouponTemplateDao.save(template);
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCouponTemplateEntity updateTemplate(PlatformCouponTemplateUpdateRequest request) {
        AdminScope scope = requireAdminScope();
        if (request == null || request.getPctId() == null) {
            throw new IllegalArgumentException("pctId 不能为空");
        }
        PlatformCouponTemplateEntity existing = requireTemplate(scope.marketId, request.getPctId());
        PlatformCouponTemplateEntity patch = fromSaveRequest(request);
        patch.setPctId(existing.getPctId());
        patch.setMarketId(scope.marketId);
        patch.setUpdatedByMarketUserId(scope.marketUserId);
        PlatformCouponRuleValidator.normalizeDefaults(patch);
        PlatformCouponRuleValidator.validateTemplate(patch);
        platformCouponTemplateDao.update(patch);
        return requireTemplate(scope.marketId, request.getPctId());
    }

    @Override
    public List<PlatformCouponTemplateEntity> listTemplates(PlatformCouponTemplateListRequest request) {
        AdminScope scope = requireAdminScope();
        Map<String, Object> map = new HashMap<>();
        map.put("marketId", scope.marketId);
        if (request != null) {
            map.put("status", trimToNull(request.getStatus()));
            map.put("templateName", trimToNull(request.getTemplateName()));
            map.put("offset", request.getOffset() == null ? 0 : request.getOffset());
            map.put("limit", request.getLimit() == null ? DEFAULT_LIST_LIMIT : request.getLimit());
        } else {
            map.put("offset", 0);
            map.put("limit", DEFAULT_LIST_LIMIT);
        }
        return platformCouponTemplateDao.queryList(map);
    }

    @Override
    public PlatformCouponTemplateEntity templateDetail(Integer pctId) {
        AdminScope scope = requireAdminScope();
        return requireTemplate(scope.marketId, pctId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCouponTemplateEntity disableTemplate(Integer pctId) {
        AdminScope scope = requireAdminScope();
        PlatformCouponTemplateEntity existing = requireTemplate(scope.marketId, pctId);
        if (PlatformCouponConstants.TEMPLATE_STATUS_DISABLED.equals(existing.getStatus())) {
            return existing;
        }
        PlatformCouponTemplateEntity patch = new PlatformCouponTemplateEntity();
        patch.setPctId(pctId);
        patch.setMarketId(scope.marketId);
        patch.setStatus(PlatformCouponConstants.TEMPLATE_STATUS_DISABLED);
        patch.setUpdatedByMarketUserId(scope.marketUserId);
        platformCouponTemplateDao.update(patch);
        return requireTemplate(scope.marketId, pctId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformStoreCouponEntity> issueStoreCoupons(PlatformStoreCouponIssueRequest request) {
        AdminScope scope = requireAdminScope();
        if (request == null || request.getTemplateId() == null) {
            throw new IllegalArgumentException("templateId 不能为空");
        }
        int count = request.getCount() == null ? 1 : request.getCount();
        if (count <= 0) {
            throw new IllegalArgumentException("count 必须大于 0");
        }
        if (count > MAX_ISSUE_COUNT) {
            throw new IllegalArgumentException("单次发券数量不能超过 " + MAX_ISSUE_COUNT);
        }
        PlatformCouponTemplateEntity template = requireTemplate(scope.marketId, request.getTemplateId());
        if (!PlatformCouponConstants.TEMPLATE_STATUS_ACTIVE.equals(template.getStatus())) {
            throw new IllegalStateException("券模板已停用，无法发券");
        }
        Integer inputDeptId = resolveInputDepartmentId(request);
        GbDepartmentEntity store = platformStoreDepartmentResolver
                .resolveStoreDepartmentForMarket(scope.marketId, inputDeptId);

        List<PlatformStoreCouponEntity> issued = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            PlatformStoreCouponEntity instance = buildStoreCouponInstance(template, store, scope.marketUserId);
            platformStoreCouponDao.save(instance);
            writeUsageLog(instance, template, PlatformCouponConstants.VERIFY_ISSUE,
                    null, PlatformCouponConstants.STORE_STATUS_AVAILABLE, scope.marketUserId);
            issued.add(instance);
        }
        Map<String, Object> inc = new HashMap<>();
        inc.put("pctId", template.getPctId());
        inc.put("marketId", scope.marketId);
        inc.put("delta", count);
        platformCouponTemplateDao.incrementIssueCount(inc);
        return issued;
    }

    @Override
    public List<PlatformStoreCouponEntity> listStoreCoupons(PlatformStoreCouponListRequest request) {
        AdminScope scope = requireAdminScope();
        Map<String, Object> map = new HashMap<>();
        map.put("marketId", scope.marketId);
        if (request != null) {
            Integer storeId = resolveListStoreId(scope.marketId, request);
            if (storeId != null) {
                map.put("storeGbDepartmentId", storeId);
            }
            map.put("status", trimToNull(request.getStatus()));
            map.put("templateId", request.getTemplateId());
            map.put("offset", request.getOffset() == null ? 0 : request.getOffset());
            map.put("limit", request.getLimit() == null ? DEFAULT_LIST_LIMIT : request.getLimit());
        } else {
            map.put("offset", 0);
            map.put("limit", DEFAULT_LIST_LIMIT);
        }
        return platformStoreCouponDao.queryList(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformStoreCouponEntity voidStoreCoupon(Integer pscId) {
        AdminScope scope = requireAdminScope();
        if (pscId == null) {
            throw new IllegalArgumentException("pscId 不能为空");
        }
        PlatformStoreCouponEntity coupon = requireStoreCoupon(scope.marketId, pscId);
        String beforeStatus = coupon.getStatus();
        if (PlatformCouponConstants.STORE_STATUS_VOID.equals(beforeStatus)) {
            return coupon;
        }
        if (!PlatformCouponConstants.STORE_STATUS_AVAILABLE.equals(beforeStatus)
                && !PlatformCouponConstants.STORE_STATUS_EXPIRED.equals(beforeStatus)) {
            throw new IllegalStateException("当前状态不可作废: " + beforeStatus);
        }
        PlatformStoreCouponEntity patch = new PlatformStoreCouponEntity();
        patch.setPscId(pscId);
        patch.setMarketId(scope.marketId);
        patch.setStatus(PlatformCouponConstants.STORE_STATUS_VOID);
        platformStoreCouponDao.update(patch);
        coupon.setStatus(PlatformCouponConstants.STORE_STATUS_VOID);
        PlatformCouponTemplateEntity template = requireTemplate(scope.marketId, coupon.getTemplateId());
        writeUsageLog(coupon, template, PlatformCouponConstants.VERIFY_VOID,
                beforeStatus, PlatformCouponConstants.STORE_STATUS_VOID, scope.marketUserId);
        return coupon;
    }

    private PlatformStoreCouponEntity buildStoreCouponInstance(PlatformCouponTemplateEntity template,
                                                               GbDepartmentEntity store,
                                                               Integer marketUserId) {
        PlatformStoreCouponEntity instance = new PlatformStoreCouponEntity();
        instance.setTemplateId(template.getPctId());
        instance.setMarketId(template.getMarketId());
        instance.setStoreGbDepartmentId(store.getGbDepartmentId());
        instance.setStatus(PlatformCouponConstants.STORE_STATUS_AVAILABLE);
        instance.setSourceType(PlatformCouponConstants.SOURCE_MANUAL);
        instance.setIssuedByMarketUserId(marketUserId);
        applyInstanceValidity(instance, template);
        return instance;
    }

    private void applyInstanceValidity(PlatformStoreCouponEntity instance,
                                       PlatformCouponTemplateEntity template) {
        if (PlatformCouponConstants.VALIDITY_FIXED_DATE.equals(template.getValidityType())) {
            instance.setStartDate(template.getStartDate());
            instance.setStopDate(template.getStopDate());
            return;
        }
        LocalDate start = LocalDate.now();
        LocalDate stop = start.plusDays(template.getValidityDays());
        instance.setStartDate(start.toString());
        instance.setStopDate(stop.toString());
    }

    private void writeUsageLog(PlatformStoreCouponEntity coupon,
                               PlatformCouponTemplateEntity template,
                               String verifyType,
                               String beforeStatus,
                               String afterStatus,
                               Integer marketUserId) {
        PlatformCouponUsageLogEntity log = new PlatformCouponUsageLogEntity();
        log.setStoreCouponId(coupon.getPscId());
        log.setTemplateId(template.getPctId());
        log.setMarketId(coupon.getMarketId());
        log.setStoreGbDepartmentId(coupon.getStoreGbDepartmentId());
        log.setVerifyType(verifyType);
        log.setDiscountAmount(template.getDiscountAmount());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperatorType(PlatformCouponConstants.OPERATOR_MARKET_USER);
        log.setOperatorId(marketUserId);
        platformCouponUsageLogDao.save(log);
    }

    private PlatformCouponTemplateEntity requireTemplate(Integer marketId, Integer pctId) {
        if (pctId == null) {
            throw new IllegalArgumentException("pctId 不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pctId", pctId);
        map.put("marketId", marketId);
        PlatformCouponTemplateEntity template = platformCouponTemplateDao.queryByMarketAndId(map);
        if (template == null) {
            throw new IllegalArgumentException("券模板不存在或不属于当前市场");
        }
        return template;
    }

    private PlatformStoreCouponEntity requireStoreCoupon(Integer marketId, Integer pscId) {
        Map<String, Object> map = new HashMap<>();
        map.put("pscId", pscId);
        map.put("marketId", marketId);
        PlatformStoreCouponEntity coupon = platformStoreCouponDao.queryByMarketAndId(map);
        if (coupon == null) {
            throw new IllegalArgumentException("门店券不存在或不属于当前市场");
        }
        return coupon;
    }

    private Integer resolveInputDepartmentId(PlatformStoreCouponIssueRequest request) {
        if (request.getStoreGbDepartmentId() != null) {
            return request.getStoreGbDepartmentId();
        }
        if (request.getGbDepartmentId() != null) {
            return request.getGbDepartmentId();
        }
        throw new IllegalArgumentException("gbDepartmentId 或 storeGbDepartmentId 不能为空");
    }

    private Integer resolveListStoreId(Integer marketId, PlatformStoreCouponListRequest request) {
        if (request.getStoreGbDepartmentId() != null) {
            return platformStoreDepartmentResolver
                    .resolveStoreDepartmentForMarket(marketId, request.getStoreGbDepartmentId())
                    .getGbDepartmentId();
        }
        if (request.getGbDepartmentId() != null) {
            return platformStoreDepartmentResolver
                    .resolveStoreDepartmentForMarket(marketId, request.getGbDepartmentId())
                    .getGbDepartmentId();
        }
        return null;
    }

    private PlatformCouponTemplateEntity fromSaveRequest(PlatformCouponTemplateSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        PlatformCouponTemplateEntity template = new PlatformCouponTemplateEntity();
        template.setTemplateName(trimToNull(request.getTemplateName()));
        template.setCouponType(trimToNull(request.getCouponType()));
        template.setDiscountAmount(request.getDiscountAmount());
        template.setThresholdAmount(request.getThresholdAmount());
        template.setScopeType(trimToNull(request.getScopeType()));
        template.setScopeRefIds(trimToNull(request.getScopeRefIds()));
        template.setUseChannel(trimToNull(request.getUseChannel()));
        template.setBizPurpose(trimToNull(request.getBizPurpose()));
        template.setClaimStrategy(trimToNull(request.getClaimStrategy()));
        template.setValidityType(trimToNull(request.getValidityType()));
        template.setValidityDays(request.getValidityDays());
        template.setStartDate(trimToNull(request.getStartDate()));
        template.setStopDate(trimToNull(request.getStopDate()));
        return template;
    }

    private AdminScope requireAdminScope() {
        Integer marketId = PlatformMarketAdminContext.getMarketId();
        Integer marketUserId = PlatformMarketAdminContext.getCurrentMarketUserId();
        if (marketId == null || marketUserId == null) {
            throw new IllegalStateException("未登录市场后台");
        }
        return new AdminScope(marketId, marketUserId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class AdminScope {
        private final Integer marketId;
        private final Integer marketUserId;

        private AdminScope(Integer marketId, Integer marketUserId) {
            this.marketId = marketId;
            this.marketUserId = marketUserId;
        }
    }
}
