-- platform_checkout_payment 补齐 pendingPriceItemCount 快照列
-- 若表尚未创建，直接执行 upgrade_platform_checkout_payment.sql（已含该列）即可。
-- 若已执行旧版建表，再执行本文件。

ALTER TABLE platform_checkout_payment
    ADD COLUMN pcp_pending_price_item_count INT NOT NULL DEFAULT 0
        COMMENT 'checkout 快照：价格待确认行数' AFTER pcp_known_total;
