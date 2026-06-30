package com.nongxinle.platform;

import com.nongxinle.dto.platform.admin.PlatformMarketAdminBootstrapRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminLoginRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminSessionDto;
import com.nongxinle.entity.PlatformMarketUserSessionEntity;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Phase 1a-0：市场后台用户 bootstrap + login 最小验收。
 *
 * <pre>
 * mvn -q test-compile exec:java \
 *   -Dexec.mainClass=com.nongxinle.platform.PlatformMarketAdminAuthRunner
 * </pre>
 */
public class PlatformMarketAdminAuthRunner {

    private static final int MARKET_ID = 1;
    private static final String LOGIN_ACCOUNT = "123";
    private static final String PASSWORD = "123";

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-gb-p11-test.xml");
        try {
            PlatformMarketAdminAuthService authService = ctx.getBean(PlatformMarketAdminAuthService.class);

            PlatformMarketAdminBootstrapRequest bootstrap = new PlatformMarketAdminBootstrapRequest();
            bootstrap.setMarketId(MARKET_ID);
            bootstrap.setLoginAccount(LOGIN_ACCOUNT);
            bootstrap.setPassword(PASSWORD);
            bootstrap.setRealName("平台管理员");
            bootstrap.setRoleType("ADMIN");

            PlatformMarketAdminSessionDto session;
            try {
                session = authService.bootstrapFirstAdmin(bootstrap);
                System.out.println("bootstrap ok pmuId=" + session.getCurrentMarketUserId()
                        + " marketId=" + session.getMarketId());
            } catch (IllegalStateException exists) {
                System.out.println("bootstrap skipped: " + exists.getMessage());
                PlatformMarketAdminLoginRequest login = new PlatformMarketAdminLoginRequest();
                login.setMarketId(MARKET_ID);
                login.setLoginAccount(LOGIN_ACCOUNT);
                login.setPassword(PASSWORD);
                session = authService.login(login);
                System.out.println("login ok pmuId=" + session.getCurrentMarketUserId()
                        + " marketId=" + session.getMarketId());
            }

            if (session.getToken() == null) {
                throw new IllegalStateException("token 为空");
            }
            if (session.getMarketId() == null || session.getMarketId() != MARKET_ID) {
                throw new IllegalStateException("marketId 不正确: " + session.getMarketId());
            }

            PlatformMarketUserSessionEntity resolved = authService.resolveValidSession(session.getToken());
            if (resolved == null) {
                throw new IllegalStateException("resolveValidSession 失败");
            }
            System.out.println("session resolve ok pmuId=" + resolved.getPmuId());

            PlatformMarketAdminSessionDto me = authService.currentSession(session.getCurrentMarketUserId());
            System.out.println("me ok marketName=" + me.getMarketName() + " roleType=" + me.getRoleType());
            System.out.println("Phase 1a-0 market admin auth smoke PASSED");
        } finally {
            ctx.close();
        }
    }
}
