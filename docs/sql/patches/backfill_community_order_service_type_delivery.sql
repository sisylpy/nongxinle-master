-- 外送订单 nx_CO_service_type 漏写为 NULL/0 时，补成 1 以便进入社区派单 eligible 池。
-- 执行后刷新分派 sandbox 即可看到客户。

UPDATE nx_community_orders
SET nx_CO_service_type = 1
WHERE nx_CO_delivery_address_id IS NOT NULL
  AND (nx_CO_service_type IS NULL OR nx_CO_service_type <> 1)
  AND nx_CO_status >= 2
  AND nx_CO_status NOT IN (5, 99);
