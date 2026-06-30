-- 平台购物车 checkout 微信支付意图（bill 创建前）
-- 执行前：SHOW TABLES LIKE 'platform_checkout_payment';

CREATE TABLE IF NOT EXISTS platform_checkout_payment (
    pcp_id                    INT AUTO_INCREMENT PRIMARY KEY,
    pcp_checkout_token        VARCHAR(64)  NOT NULL COMMENT 'checkout 批次幂等 token',
    pcp_market_id             INT          NOT NULL,
    pcp_gb_department_id      INT          NOT NULL,
    pcp_gb_department_father_id INT        NULL,
    pcp_gb_distributer_id     INT          NULL,
    pcp_gb_order_user_id      INT          NULL,
    pcp_delivery_date         VARCHAR(32)  NULL,
    pcp_remark                VARCHAR(512) NULL,
    pcp_order_ids_json        TEXT         NOT NULL COMMENT 'JSON 数组 nxOrderId 快照',
    pcp_known_total           DECIMAL(12,2) NOT NULL,
    pcp_pending_price_item_count INT       NOT NULL DEFAULT 0 COMMENT 'checkout 快照：价格待确认行数',
    pcp_out_trade_no          VARCHAR(64)  NOT NULL,
    pcp_wx_prepay_id          VARCHAR(128) NULL,
    pcp_open_id               VARCHAR(128) NOT NULL,
    pcp_status                VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    pcp_bill_id               INT          NULL,
    pcp_transaction_id        VARCHAR(64)  NULL,
    pcp_notify_raw            TEXT         NULL,
    pcp_paid_at               VARCHAR(32)  NULL,
    pcp_expire_at             VARCHAR(32)  NULL COMMENT 'PENDING 过期时间',
    pcp_closed_at             VARCHAR(32)  NULL COMMENT '取消/超时关闭时间',
    pcp_created_at            VARCHAR(32)  NULL,
    pcp_updated_at            VARCHAR(32)  NULL,
    UNIQUE KEY uk_pcp_checkout_token (pcp_checkout_token),
    UNIQUE KEY uk_pcp_out_trade_no (pcp_out_trade_no),
    KEY idx_pcp_dep_status (pcp_gb_department_id, pcp_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台购物车 checkout 微信支付（bill 创建前）';
