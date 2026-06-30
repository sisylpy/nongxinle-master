-- POS 第一阶段：支付流水、核销流水、订单渠道字段
-- 执行前：SHOW COLUMNS FROM nx_community_orders;

ALTER TABLE nx_community_orders
    ADD COLUMN nx_co_order_channel varchar(16) DEFAULT 'miniapp' COMMENT '订单来源：miniapp/pos' AFTER nx_co_desk_id;

ALTER TABLE nx_community_orders
    ADD COLUMN nx_co_pos_operator_id int DEFAULT NULL COMMENT 'POS操作员工ID' AFTER nx_co_order_channel;

CREATE TABLE IF NOT EXISTS nx_community_pos_payment (
    nx_pos_payment_id int NOT NULL AUTO_INCREMENT,
    nx_pp_order_id int NOT NULL COMMENT '关联 nx_community_orders',
    nx_pp_community_id int NOT NULL,
    nx_pp_pay_channel varchar(16) NOT NULL COMMENT 'WECHAT/ALIPAY',
    nx_pp_out_trade_no varchar(64) NOT NULL,
    nx_pp_transaction_id varchar(64) DEFAULT NULL,
    nx_pp_amount decimal(10,2) NOT NULL,
    nx_pp_status varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED/CLOSED',
    nx_pp_qr_code_url varchar(512) DEFAULT NULL,
    nx_pp_operator_id int DEFAULT NULL,
    nx_pp_paid_at datetime DEFAULT NULL,
    nx_pp_notify_raw text,
    nx_pp_create_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nx_pp_expire_at datetime DEFAULT NULL,
    PRIMARY KEY (nx_pos_payment_id),
    UNIQUE KEY uk_pp_out_trade_no (nx_pp_out_trade_no),
    KEY idx_pp_order_id (nx_pp_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POS支付流水';

CREATE TABLE IF NOT EXISTS nx_community_coupon_verify_log (
    nx_coupon_verify_log_id int NOT NULL AUTO_INCREMENT,
    nx_cvl_user_coupon_id int NOT NULL,
    nx_cvl_coupon_id int NOT NULL,
    nx_cvl_community_id int NOT NULL,
    nx_cvl_verify_type varchar(32) NOT NULL COMMENT 'standalone/order_lock/order_paid_verify/unlock',
    nx_cvl_order_id int DEFAULT NULL,
    nx_cvl_order_sub_id int DEFAULT NULL,
    nx_cvl_desk_id int DEFAULT NULL,
    nx_cvl_operator_id int DEFAULT NULL,
    nx_cvl_before_status int DEFAULT NULL,
    nx_cvl_after_status int DEFAULT NULL,
    nx_cvl_remark varchar(255) DEFAULT NULL,
    nx_cvl_create_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nx_coupon_verify_log_id),
    KEY idx_cvl_user_coupon (nx_cvl_user_coupon_id),
    KEY idx_cvl_order (nx_cvl_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券核销流水';
