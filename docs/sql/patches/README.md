-- =============================================================================
-- 补丁索引：部分执行 init 后的结构补齐（按需执行子文件）
--
-- 现象：CREATE TABLE IF NOT EXISTS 不会升级已存在旧表，Java 查询报 Unknown column。
--
-- | 报错列 / 表                         | 执行文件 |
-- |-------------------------------------|----------|
-- | nx_customer_promotion_campaign.*    | upgrade_nx_customer_promotion_campaign.sql |
-- | nx_customer_user_referral.campaign_id / inviter_user_id 等 | upgrade_nx_customer_user_referral.sql |
-- | nx_customer_user_coupon.nx_cuc_start_date 等 | upgrade_nx_customer_user_coupon.sql |
-- | nx_customer_referral_reward_rule.rule_status 等 | upgrade_nx_customer_referral_reward_rule.sql |
-- | nx_community_pos_payment / coupon_verify_log / 订单渠道字段 | upgrade_nx_community_pos.sql |
-- | nx_community_coupon.nx_cp_validity_type 等元数据列         | upgrade_nx_community_coupon_metadata.sql（本地）/ upgrade_nx_community_coupon_metadata_server.sql（服务器整段执行） |
-- | nx_community_coupon.coupon_type / discount_amount 等规则列 | upgrade_coupon_rule_v1.sql |
-- | 批发市场平台化 Phase 2a（4 表 + 订单分配）                  | upgrade_nx_platform_phase2a.sql |
-- | Phase 2b 平台订单履约（出库完成→READY_FOR_PICKUP）        | upgrade_nx_platform_phase2b_fulfillment.sql |
-- | 京采 P0：assign 表 GB 来源字段（批发商店铺 ASSIGNED）     | upgrade_nx_platform_assign_gb_source.sql |
-- | Phase 2a 阶段 0：nx_DO_distributer_id 是否 NULL            | check_nx_department_orders_distributer_id.sql |
-- | Phase 2a Round 1 测试种子                                   | seed_nx_platform_phase2a_test.sql |
-- | `platform_checkout_payment`（checkout 微信支付意图，bill 创建前） | upgrade_platform_checkout_payment.sql → v2 → v3 |
-- | **服务器一次性全量升级（推荐）**                              | upgrade_platform_server_full.sql |
-- | **清空平台联调数据（购物车/checkout/bill/assign/订单行）**    | cleanup_platform_transactional_data.sql |
-- | 京采市场后台用户 Phase 1a-0（platform_market_user）           | upgrade_platform_market_user_v1.sql |
-- | 京采市场后台开发测试账号 123/123（仅本地）                    | upgrade_platform_market_user_dev_login_123.sql |
-- | 京采平台优惠券 Phase 1a（模板 + 门店券）                      | upgrade_platform_coupon_phase1a.sql |
| 配送商多司机路线派单 Phase 1（4 表）                           | upgrade_nx_dis_route_dispatch_phase1.sql |
| Phase 1 P0：站点订单快照 + routeDate/dispatchDate              | upgrade_nx_dis_route_stop_order_snapshot.sql |
| Phase 1.5a：shipment_task + plan 三阶段状态 + stop.task_id     | upgrade_nx_dis_route_dispatch_phase1_5a.sql |
| 司机每日上岗状态（simulate 仅 ON_DUTY）                        | upgrade_nx_dis_driver_duty.sql |
| Phase 2a：固定顺序配送时间窗排程结果字段                        | upgrade_nx_dis_route_schedule_phase2.sql |
| Phase 2b-1：配送批次 + 可执行性状态                             | upgrade_nx_dis_route_dispatch_phase2b1.sql |
| Phase 2b-5：客户调度参数 + 当日送达窗口快照 / override          | upgrade_nx_dis_route_dispatch_phase2b5.sql |
| **一键执行 Phase 2a + 2b-1 patch（本地）**                    | `scripts/apply-route-dispatch-phase2-patches.sh` |
--
-- =============================================================================
-- Phase 2a Round 1 执行顺序（submitLine 验收，必须严格按序）
-- =============================================================================
--
--   1. check_nx_department_orders_distributer_id.sql   ← 阶段 0，确认 nx_DO_distributer_id 是否 NULL
--   2. upgrade_nx_platform_phase2a.sql                 ← 建 4 张平台表（含 nx_platform_order_assign）
--   3. seed_nx_platform_phase2a_test.sql               ← 改真实 marketId/departmentId 后执行
--
-- ⚠️  先跑完 1→2→3，再启动含 Phase 2a Java 代码的应用。
--     新代码在部分旧配送商查询中 NOT EXISTS nx_platform_order_assign；
--     若表未建，旧配送商接口会直接报错（表不存在）。
--
-- 详细验收步骤见：docs/nxPlatform/Phase2a-Round1-验收指南.md
--
-- Phase 2b 履约表执行顺序：
--   1. upgrade_nx_platform_phase2a.sql（若未执行）
--   2. upgrade_nx_platform_phase2b_fulfillment.sql
--   3. 可选 backfill（同文件 §3）
--   4. 再部署 Phase 2b Java
-- 详见：docs/nxPlatform/Phase2b-SQL-Patch-Draft.md
--
-- 执行前建议：
--   SHOW COLUMNS FROM <表名>;
-- 某条 ALTER 报 Duplicate column name 时跳过该句即可。
-- =============================================================================
