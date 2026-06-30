package com.nongxinle.service.platform.admin;

import com.nongxinle.dto.platform.admin.coupon.PlatformCouponNxGoodsCategoryOptionDto;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponStoreOptionDto;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateListRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateSaveRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateUpdateRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponIssueRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponListRequest;
import com.nongxinle.entity.PlatformCouponTemplateEntity;
import com.nongxinle.entity.PlatformStoreCouponEntity;

import java.util.List;

public interface PlatformCouponAdminService {

    PlatformCouponTemplateEntity saveTemplate(PlatformCouponTemplateSaveRequest request);

    PlatformCouponTemplateEntity updateTemplate(PlatformCouponTemplateUpdateRequest request);

    List<PlatformCouponTemplateEntity> listTemplates(PlatformCouponTemplateListRequest request);

    PlatformCouponTemplateEntity templateDetail(Integer pctId);

    PlatformCouponTemplateEntity disableTemplate(Integer pctId);

    List<PlatformStoreCouponEntity> issueStoreCoupons(PlatformStoreCouponIssueRequest request);

    List<PlatformStoreCouponEntity> listStoreCoupons(PlatformStoreCouponListRequest request);

    PlatformStoreCouponEntity voidStoreCoupon(Integer pscId);

    List<PlatformCouponStoreOptionDto> listStoreOptions();

    List<PlatformCouponNxGoodsCategoryOptionDto> listNxGoodsCategoryOptions();
}
