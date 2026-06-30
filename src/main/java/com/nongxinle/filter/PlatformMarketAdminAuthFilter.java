package com.nongxinle.filter;

import com.nongxinle.entity.PlatformMarketUserEntity;
import com.nongxinle.entity.PlatformMarketUserSessionEntity;
import com.nongxinle.platform.admin.PlatformMarketAdminContext;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import com.nongxinle.utils.PlatformMarketUserConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 市场后台 API 鉴权 Filter（与 {@link com.nongxinle.platform.admin.PlatformMarketAdminAuthInterceptor} 互补）。
 * Servlet url-pattern 无法覆盖多级路径，故在 Filter 内按 URI 判断。
 */
@Component("platformMarketAdminAuthFilter")
public class PlatformMarketAdminAuthFilter extends OncePerRequestFilter {

    @Autowired
    private PlatformMarketAdminAuthService platformMarketAdminAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!requiresMarketAdminAuth(request)) {
            chain.doFilter(request, response);
            return;
        }
        String token = resolveToken(request);
        if (StringUtils.isBlank(token)) {
            writeUnauthorized(response, "缺少登录凭证");
            return;
        }
        PlatformMarketUserSessionEntity session =
                platformMarketAdminAuthService.resolveValidSession(token.trim());
        if (session == null) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return;
        }
        PlatformMarketUserEntity user = platformMarketAdminAuthService.loadActiveUser(session.getPmuId());
        if (user == null) {
            writeUnauthorized(response, "账号不可用");
            return;
        }
        try {
            PlatformMarketAdminContext.set(user.getPmuId(), user.getMarketId(), user.getRoleType());
            chain.doFilter(request, response);
        } finally {
            PlatformMarketAdminContext.clear();
        }
    }

    private boolean requiresMarketAdminAuth(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.contains("/api/platform/admin/")) {
            return false;
        }
        if (uri.contains("/api/platform/admin/auth/login")
                || uri.contains("/api/platform/admin/auth/bootstrap")) {
            return false;
        }
        return true;
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

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\"}");
    }
}
