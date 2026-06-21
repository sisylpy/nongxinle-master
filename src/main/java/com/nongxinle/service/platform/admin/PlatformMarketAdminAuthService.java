package com.nongxinle.service.platform.admin;

import com.nongxinle.dto.platform.admin.PlatformMarketAdminBootstrapRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminLoginRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminSessionDto;
import com.nongxinle.entity.PlatformMarketUserEntity;
import com.nongxinle.entity.PlatformMarketUserSessionEntity;

public interface PlatformMarketAdminAuthService {

    PlatformMarketAdminSessionDto login(PlatformMarketAdminLoginRequest request);

    PlatformMarketAdminSessionDto bootstrapFirstAdmin(PlatformMarketAdminBootstrapRequest request);

    void logout(String token);

    PlatformMarketAdminSessionDto currentSession(Integer pmuId);

    PlatformMarketUserSessionEntity resolveValidSession(String token);

    PlatformMarketUserEntity loadActiveUser(Integer pmuId);
}
