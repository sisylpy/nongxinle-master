package com.nongxinle.community.coupon.controller;

import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.community.coupon.service.NxCommunityCouponRuleSaveService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务间规则券保存入口：复用 {@link NxCommunityCouponRuleSaveService}，不另写落库逻辑。
 * 鉴权由配置项 nxcommunity.internal.api-token 控制（请求头名可配置）。
 */
@RestController
@RequestMapping("api/internal/nxcommunitycoupon")
public class NxCommunityCouponInternalController {

    @Autowired
    private NxCommunityCouponRuleSaveService nxCommunityCouponRuleSaveService;

    @Value("${nxcommunity.internal.api-token:}")
    private String internalApiToken;

    @Value("${nxcommunity.internal.auth-header-name:X-NxCommunity-Internal-Token}")
    private String authHeaderName;

    @RequestMapping(value = "/saveRuleCoupon", method = RequestMethod.POST)
    @ResponseBody
    public R saveRuleCoupon(@RequestBody NxCommunityCouponEntity coupon, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return R.error(403, "internal_api_unauthorized");
        }
        try {
            NxCommunityCouponEntity saved = nxCommunityCouponRuleSaveService.saveRuleCoupon(coupon);
            return R.ok().put("data", saved);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        if (!StringUtils.hasText(internalApiToken) || request == null) {
            return false;
        }
        String header = StringUtils.hasText(authHeaderName) ? authHeaderName.trim() : "X-NxCommunity-Internal-Token";
        String token = request.getHeader(header);
        return StringUtils.hasText(token) && internalApiToken.trim().equals(token.trim());
    }
}
