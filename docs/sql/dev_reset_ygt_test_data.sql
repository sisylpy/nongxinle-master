-- ============================================================
-- 优果团 Phase 1B 本地测试数据重置 SQL (方案 A - 备选)
-- 用途：完全清除 ygt 相关测试数据，重新拉取后可验证
--       OPEN 团期下新 candidate 是否自动关联 campaignId。
-- 注意：按外键依赖顺序 DELETE，先删子表再删主表。
-- ============================================================

-- 1) group_order_item -> group_order, campaign_goods
DELETE FROM ygt_group_order_item;

-- 2) group_order -> order_candidate, group_buy_campaign
DELETE FROM ygt_group_order;

-- 3) order_candidate_item -> order_candidate
DELETE FROM ygt_order_candidate_item;

-- 4) order_candidate -> chat_message, group_buy_campaign
DELETE FROM ygt_order_candidate;

-- 5) campaign_goods -> group_buy_campaign
DELETE FROM ygt_campaign_goods;

-- 6) chat_message -> wecom_group
DELETE FROM ygt_chat_message;

-- 7) group_buy_campaign -> wecom_group
DELETE FROM ygt_group_buy_campaign;

-- 8) archive_cursor (无 FK)
UPDATE ygt_archive_cursor SET ygt_ac_last_seq = 0, ygt_ac_last_pull_status = 'INIT' WHERE 1=1;

-- 9) wecom_group（可选: 仅清理 source=MOCK 的模拟群）
-- DELETE FROM ygt_wecom_group WHERE ygt_wg_source = 'MOCK';

-- ============================================================
-- 重置后验证步骤：
-- 1. POST /api/ygt/admin/campaign/create  (创建团期)
-- 2. POST /api/ygt/admin/campaign/{id}/goods/add  (添加商品)
-- 3. POST /api/ygt/admin/campaign/{id}/open  (开启团期)
-- 4. POST /api/ygt/admin/wecom/archive/pull  (重新拉取)
-- 5. GET  /api/ygt/admin/candidate/list  (查候选单 campaignId 是否为 OPEN 团期ID)
-- 6. GET  /api/ygt/admin/campaign/{id}/summary  (campaignGoods 应不为空)
-- ============================================================
