# 历史推广 SQL 归档说明（不可执行）

本目录仅保留说明，**不包含可执行 SQL**。

## 为何移除 `nx_customer_referral.sql` / `nx_customer_referral_v2.sql`

上述文件会创建与当前 Java 代码**不兼容**的旧结构，例如：

- `nx_customer_user_referral.inviter_user_id`（当前已改为 `source_owner_type` / `source_owner_id`）
- 用户表 `nx_cu_referral_code` 作为推广码主权（当前已改为 `nx_customer_promotion_code`）
- 无推广活动、无效尝试、主体锁、生成列等最终版结构

误执行将导致表结构与 `com.nongxinle.service.impl.NxCustomerPromotionCodeServiceImpl` 等类不一致。

## 正式 SQL 入口（仅此两个）

| 场景 | 文件 |
|---|---|
| 全新环境，从未执行推广 SQL | `docs/sql/nx_customer_promotion_init.sql` |
| 已执行 v1/v2 旧结构 | `docs/sql/nx_customer_promotion_migrate_from_v1v2.sql` |

## 历史版本参考

若需查阅旧版 DDL 原文，请从 Git 历史恢复：

```bash
git show HEAD~N:docs/sql/nx_customer_referral.sql
git show HEAD~N:docs/sql/nx_customer_referral_v2.sql
```

（`N` 为删除前的提交）
