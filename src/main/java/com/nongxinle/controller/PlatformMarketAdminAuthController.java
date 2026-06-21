package com.nongxinle.controller;

import com.nongxinle.dto.platform.admin.PlatformMarketAdminBootstrapRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminLoginRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminSessionDto;
import com.nongxinle.platform.admin.PlatformMarketAdminContext;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import com.nongxinle.utils.PlatformMarketUserConstants;
import com.nongxinle.utils.R;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 京采市场 Electron 后台登录（独立于饭店端 / 配送商 / Community）。
 */
@RestController
@RequestMapping("api/platform/admin/auth")
public class PlatformMarketAdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(PlatformMarketAdminAuthController.class);

    @Autowired
    private PlatformMarketAdminAuthService platformMarketAdminAuthService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public R login(@RequestBody PlatformMarketAdminLoginRequest request) {
        try {
            PlatformMarketAdminSessionDto session = platformMarketAdminAuthService.login(request);
            log.info("[platform/admin/auth/login] pmuId={} marketId={}",
                    session.getCurrentMarketUserId(), session.getMarketId());
            return R.ok("success").put("data", session);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/auth/login] failed request={}", request, e);
            return R.error("登录失败");
        }
    }

    /**
     * 市场首个后台账号初始化（仅当该 market 尚无用户时可用）。
     */
    @RequestMapping(value = "/bootstrap", method = RequestMethod.POST)
    @ResponseBody
    public R bootstrap(@RequestBody PlatformMarketAdminBootstrapRequest request) {
        try {
            PlatformMarketAdminSessionDto session = platformMarketAdminAuthService.bootstrapFirstAdmin(request);
            log.info("[platform/admin/auth/bootstrap] pmuId={} marketId={}",
                    session.getCurrentMarketUserId(), session.getMarketId());
            return R.ok("success").put("data", session);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/auth/bootstrap] failed request={}", request, e);
            return R.error("初始化失败");
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public R logout(HttpServletRequest request) {
        try {
            platformMarketAdminAuthService.logout(resolveToken(request));
            return R.ok();
        } catch (Exception e) {
            log.error("[platform/admin/auth/logout] failed", e);
            return R.error("退出失败");
        }
    }

    @RequestMapping(value = "/me", method = RequestMethod.POST)
    @ResponseBody
    public R me() {
        try {
            Integer pmuId = PlatformMarketAdminContext.getCurrentMarketUserId();
            PlatformMarketAdminSessionDto session = platformMarketAdminAuthService.currentSession(pmuId);
            return R.ok("success").put("data", session);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/admin/auth/me] failed", e);
            return R.error("获取会话失败");
        }
    }

    @RequestMapping(value = "/ping", method = RequestMethod.POST)
    @ResponseBody
    public R ping() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("currentMarketUserId", PlatformMarketAdminContext.getCurrentMarketUserId());
        data.put("marketId", PlatformMarketAdminContext.getMarketId());
        data.put("roleType", PlatformMarketAdminContext.getRoleType());
        return R.ok("success").put("data", data);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerToken = request.getHeader(PlatformMarketUserConstants.HEADER_MARKET_TOKEN);
        if (StringUtils.isNotBlank(headerToken)) {
            return headerToken.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        String accessToken = request.getHeader("Access-Token");
        return StringUtils.isBlank(accessToken) ? null : accessToken.trim();
    }
}
