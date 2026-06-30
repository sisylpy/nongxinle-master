-- 桌台绑定当前订单：订单驱动桌台状态
ALTER TABLE nx_community_desk
    ADD COLUMN nx_cd_current_order_id INT NULL COMMENT '当前绑定待支付POS订单ID' AFTER nx_cd_status;

CREATE INDEX idx_cd_current_order_id ON nx_community_desk (nx_cd_current_order_id);

-- 回填：将今日待支付 POS 订单绑定到对应桌台（每桌取最新一单）
UPDATE nx_community_desk d
INNER JOIN (
    SELECT o.nx_co_desk_id AS desk_id, MAX(o.nx_community_orders_id) AS order_id
    FROM nx_community_orders o
    WHERE o.nx_co_order_channel = 'POS'
      AND o.nx_co_status = 0
      AND o.nx_co_payment_status = 0
      AND o.nx_co_desk_id IS NOT NULL
      AND o.nx_co_desk_id NOT IN (-1, 99)
    GROUP BY o.nx_co_desk_id
) latest ON latest.desk_id = d.nx_community_desk_id
SET d.nx_cd_current_order_id = latest.order_id,
    d.nx_cd_status = 1
WHERE d.nx_cd_current_order_id IS NULL;
