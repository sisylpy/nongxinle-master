package com.nongxinle.service.platform.admin.impl;

import com.nongxinle.dao.PlatformMarketUserDao;
import com.nongxinle.dao.PlatformMarketUserSessionDao;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminBootstrapRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminLoginRequest;
import com.nongxinle.dto.platform.admin.PlatformMarketAdminSessionDto;
import com.nongxinle.entity.PlatformMarketUserEntity;
import com.nongxinle.entity.PlatformMarketUserSessionEntity;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.service.platform.admin.PlatformMarketAdminAuthService;
import com.nongxinle.utils.PlatformMarketUserConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("platformMarketAdminAuthService")
public class PlatformMarketAdminAuthServiceImpl implements PlatformMarketAdminAuthService {

    @Autowired
    private PlatformMarketUserDao platformMarketUserDao;
    @Autowired
    private PlatformMarketUserSessionDao platformMarketUserSessionDao;
    @Autowired
    private SysCityMarketService sysCityMarketService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformMarketAdminSessionDto login(PlatformMarketAdminLoginRequest request) {
        validateLoginRequest(request);
        SysCityMarketEntity market = requireMarket(request.getMarketId());
        PlatformMarketUserEntity user = loadUserForLogin(request.getMarketId(), request.getLoginAccount().trim());
        String passwordHash = hashPassword(request.getPassword());
        if (!passwordHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码不正确");
        }
        if (!PlatformMarketUserConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new IllegalStateException("账号已停用");
        }
        String token = createSession(user.getPmuId());
        touchLastLogin(user.getPmuId());
        return toSessionDto(user, market, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformMarketAdminSessionDto bootstrapFirstAdmin(PlatformMarketAdminBootstrapRequest request) {
        validateBootstrapRequest(request);
        SysCityMarketEntity market = requireMarket(request.getMarketId());
        int existing = platformMarketUserDao.countByMarketId(request.getMarketId());
        if (existing > 0) {
            throw new IllegalStateException("该市场已有后台用户，请使用登录接口");
        }
        PlatformMarketUserEntity user = new PlatformMarketUserEntity();
        user.setMarketId(request.getMarketId());
        user.setLoginAccount(request.getLoginAccount().trim());
        user.setPhone(trimToNull(request.getPhone()));
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setRoleType(resolveRoleType(request.getRoleType(), PlatformMarketUserConstants.ROLE_ADMIN));
        user.setStatus(PlatformMarketUserConstants.STATUS_ACTIVE);
        platformMarketUserDao.save(user);
        String token = createSession(user.getPmuId());
        return toSessionDto(user, market, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        platformMarketUserSessionDao.deleteByToken(token.trim());
    }

    @Override
    public PlatformMarketAdminSessionDto currentSession(Integer pmuId) {
        if (pmuId == null) {
            throw new IllegalArgumentException("pmuId 不能为空");
        }
        PlatformMarketUserEntity user = loadActiveUser(pmuId);
        if (user == null) {
            throw new IllegalStateException("账号不可用");
        }
        SysCityMarketEntity market = requireMarket(user.getMarketId());
        return toSessionDto(user, market, null);
    }

    @Override
    public PlatformMarketUserSessionEntity resolveValidSession(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PlatformMarketUserSessionEntity session = platformMarketUserSessionDao.queryByToken(token);
        if (session == null || session.getExpireAt() == null) {
            return null;
        }
        if (session.getExpireAt().before(new Date())) {
            platformMarketUserSessionDao.deleteByToken(token);
            return null;
        }
        return session;
    }

    @Override
    public PlatformMarketUserEntity loadActiveUser(Integer pmuId) {
        if (pmuId == null) {
            return null;
        }
        PlatformMarketUserEntity user = platformMarketUserDao.queryObject(pmuId);
        if (user == null || !PlatformMarketUserConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            return null;
        }
        return user;
    }

    private PlatformMarketUserEntity loadUserForLogin(Integer marketId, String loginAccount) {
        Map<String, Object> map = new HashMap<>();
        map.put("marketId", marketId);
        map.put("loginAccount", loginAccount);
        PlatformMarketUserEntity user = platformMarketUserDao.queryByMarketAndLoginAccount(map);
        if (user == null) {
            throw new IllegalArgumentException("账号或密码不正确");
        }
        return user;
    }

    private SysCityMarketEntity requireMarket(Integer marketId) {
        if (marketId == null || marketId <= 0) {
            throw new IllegalArgumentException("marketId 无效");
        }
        SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
        if (market == null) {
            throw new IllegalArgumentException("市场不存在: marketId=" + marketId);
        }
        return market;
    }

    private String createSession(Integer pmuId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        PlatformMarketUserSessionEntity session = new PlatformMarketUserSessionEntity();
        session.setPmuId(pmuId);
        session.setToken(token);
        session.setExpireAt(calcExpireAt());
        platformMarketUserSessionDao.save(session);
        return token;
    }

    private Date calcExpireAt() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, PlatformMarketUserConstants.SESSION_TTL_DAYS);
        return cal.getTime();
    }

    private void touchLastLogin(Integer pmuId) {
        PlatformMarketUserEntity patch = new PlatformMarketUserEntity();
        patch.setPmuId(pmuId);
        patch.setLastLoginAt(new Date());
        platformMarketUserDao.update(patch);
    }

    private PlatformMarketAdminSessionDto toSessionDto(PlatformMarketUserEntity user,
                                                       SysCityMarketEntity market,
                                                       String token) {
        PlatformMarketAdminSessionDto dto = new PlatformMarketAdminSessionDto();
        dto.setToken(token);
        dto.setCurrentMarketUserId(user.getPmuId());
        dto.setMarketId(user.getMarketId());
        dto.setLoginAccount(user.getLoginAccount());
        dto.setRealName(user.getRealName());
        dto.setRoleType(user.getRoleType());
        dto.setStatus(user.getStatus());
        if (market != null) {
            dto.setMarketName(market.getSysCmMarketName());
        }
        return dto;
    }

    private String hashPassword(String rawPassword) {
        return new Sha256Hash(rawPassword).toHex();
    }

    private String resolveRoleType(String roleType, String defaultRole) {
        if (StringUtils.isBlank(roleType)) {
            return defaultRole;
        }
        return roleType.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateLoginRequest(PlatformMarketAdminLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getMarketId() == null || request.getMarketId() <= 0) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (StringUtils.isBlank(request.getLoginAccount())) {
            throw new IllegalArgumentException("loginAccount 不能为空");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("password 不能为空");
        }
    }

    private void validateBootstrapRequest(PlatformMarketAdminBootstrapRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getMarketId() == null || request.getMarketId() <= 0) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (StringUtils.isBlank(request.getLoginAccount())) {
            throw new IllegalArgumentException("loginAccount 不能为空");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("password 不能为空");
        }
    }
}
