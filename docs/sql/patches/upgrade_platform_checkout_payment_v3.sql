-- platform_checkout_payment：PENDING 超时 / 取消时间戳
-- 若已执行 upgrade_platform_checkout_payment.sql，再执行本文件。

ALTER TABLE platform_checkout_payment
    ADD COLUMN pcp_expire_at VARCHAR(32) NULL COMMENT 'PENDING 过期时间' AFTER pcp_paid_at;

ALTER TABLE platform_checkout_payment
    ADD COLUMN pcp_closed_at VARCHAR(32) NULL COMMENT '取消/超时关闭时间' AFTER pcp_expire_at;
