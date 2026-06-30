-- =============================================================================
-- 开发环境：将市场后台测试账号改为 123 / 123
-- 密码 SHA-256 hex（与 PlatformMarketAdminAuthServiceImpl 一致）
-- =============================================================================

UPDATE platform_market_user
SET login_account    = '123',
    password_hash    = 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
    real_name        = '测试管理员',
    updated_at     = NOW()
WHERE market_id = 1
  AND pmu_id = 1;

-- 若 pmu_id 不确定，可改用：
-- UPDATE platform_market_user SET ... WHERE market_id = 1 AND login_account = 'platform_coupon_phase1a';

SELECT pmu_id, market_id, login_account, real_name, status
FROM platform_market_user
WHERE market_id = 1 AND login_account = '123';
