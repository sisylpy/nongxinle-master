package com.nongxinle.controller;

import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateIdRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateListRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateSaveRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformCouponTemplateUpdateRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponIssueRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponListRequest;
import com.nongxinle.dto.platform.admin.coupon.PlatformStoreCouponVoidRequest;
import com.nongxinle.service.platform.admin.PlatformCouponAdminService;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 京采市场后台 · 优惠券 Phase 1a（不接 checkout / 支付 / 购物车试算）。
 */
@RestController
@RequestMapping("api/platform/admin/coupon")
public class PlatformCouponAdminController {

    private static final Logger log = LoggerFactory.getLogger(PlatformCouponAdminController.class);

    @Autowired
    private PlatformCouponAdminService platformCouponAdminService;

    @RequestMapping(value = "/template/save", method = RequestMethod.POST)
    @ResponseBody
    public R saveTemplate(@RequestBody PlatformCouponTemplateSaveRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.saveTemplate(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/template/save] failed", e);
            return R.error("保存券模板失败");
        }
    }

    @RequestMapping(value = "/template/update", method = RequestMethod.POST)
    @ResponseBody
    public R updateTemplate(@RequestBody PlatformCouponTemplateUpdateRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.updateTemplate(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/template/update] failed", e);
            return R.error("更新券模板失败");
        }
    }

    @RequestMapping(value = "/template/list", method = RequestMethod.POST)
    @ResponseBody
    public R listTemplates(@RequestBody(required = false) PlatformCouponTemplateListRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.listTemplates(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/template/list] failed", e);
            return R.error("查询券模板失败");
        }
    }

    @RequestMapping(value = "/template/detail", method = RequestMethod.POST)
    @ResponseBody
    public R templateDetail(@RequestBody PlatformCouponTemplateIdRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.templateDetail(request.getPctId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/template/detail] failed", e);
            return R.error("查询券模板详情失败");
        }
    }

    @RequestMapping(value = "/template/disable", method = RequestMethod.POST)
    @ResponseBody
    public R disableTemplate(@RequestBody PlatformCouponTemplateIdRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.disableTemplate(request.getPctId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/template/disable] failed", e);
            return R.error("停用券模板失败");
        }
    }

    @RequestMapping(value = "/store/issue", method = RequestMethod.POST)
    @ResponseBody
    public R issueStoreCoupons(@RequestBody PlatformStoreCouponIssueRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.issueStoreCoupons(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/store/issue] failed", e);
            return R.error("门店发券失败");
        }
    }

    @RequestMapping(value = "/store/list", method = RequestMethod.POST)
    @ResponseBody
    public R listStoreCoupons(@RequestBody(required = false) PlatformStoreCouponListRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.listStoreCoupons(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/store/list] failed", e);
            return R.error("查询门店券失败");
        }
    }

    @RequestMapping(value = "/store/void", method = RequestMethod.POST)
    @ResponseBody
    public R voidStoreCoupon(@RequestBody PlatformStoreCouponVoidRequest request) {
        try {
            return R.ok("success").put("data", platformCouponAdminService.voidStoreCoupon(request.getPscId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/coupon/store/void] failed", e);
            return R.error("作废门店券失败");
        }
    }
}
