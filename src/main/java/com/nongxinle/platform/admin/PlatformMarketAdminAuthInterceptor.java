package com.nongxinle.platform.admin;

import com.nongxinle.entity.PlatformMarketUserEntity;
import com.nongxinle.entity.PlatformMarketUserSessionEntity;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import com.nongxinle.utils.PlatformMarketUserConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PlatformMarketAdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private PlatformMarketAdminAuthService platformMarketAdminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = resolveToken(request);
        if (StringUtils.isBlank(token)) {
            writeUnauthorized(response, "缺少登录凭证");
            return false;
        }
        PlatformMarketUserSessionEntity session = platformMarketAdminAuthService.resolveValidSession(token.trim());
        if (session == null) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }
        PlatformMarketUserEntity user = platformMarketAdminAuthService.loadActiveUser(session.getPmuId());
        if (user == null) {
            writeUnauthorized(response, "账号不可用");
            return false;
        }
        PlatformMarketAdminContext.set(user.getPmuId(), user.getMarketId(), user.getRoleType());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        PlatformMarketAdminContext.clear();
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
        return request.getHeader("Access-Token");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\"}");
    }
}
