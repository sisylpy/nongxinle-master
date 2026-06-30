# NxCustomerUser 推广注册奖励接口说明（三类推广主体版）

> **SQL**：全新环境执行 `docs/sql/nx_customer_promotion_init.sql`；已执行旧版见 `nx_customer_promotion_migrate_from_v1v2.sql`

## 一、推广活动 vs 奖励规则（职责分离）

| 层 | 表 | 职责 |
|---|---|---|
| **推广活动** | `nx_customer_promotion_campaign` | 决定 COMMUNITY_USER / EXTERNAL_PROMOTER 注册是否计入推广业绩 |
| **奖励规则** | `nx_customer_referral_reward_rule` | 决定有效推广后是否发券/积分/佣金等奖励资产 |
| **推广关系** | `nx_customer_user_referral` | 仅保存 **QUALIFIED** 的有效推广来源事实 |
| **无效尝试** | `nx_customer_promotion_code_attempt` | 保存无效码审计，**不占用** referral 的 invitee 唯一键 |

### 三类主体资格与奖励

| 主体 | 形成 QUALIFIED 推广 | 无奖励配置时 |
|---|---|---|
| `CUSTOMER_USER` | 码+主体有效即可 | 仍可 QUALIFIED；不发券；可产生首页推荐成功动态 |
| `COMMUNITY_USER` | 码+主体+资格+**推广活动有效** | 活动有效即可计业绩；无奖励规则不发券 |
| `EXTERNAL_PROMOTER` | 码+主体+合作期+**推广活动有效** | 活动有效即可计业绩；无佣金规则不结算 |

`nx_customer_referral_reward_rule` **不再**决定员工/地推的推广业绩资格。

活动暂停、终止或人员离职后：**历史 referral、业绩、已发奖励不回写失效**。

## 二、推广码与推广活动命中关系（正式主权）

**采用方案 B：推广码长期属于主体，注册时动态匹配活动。**

- `nx_customer_promotion_code` **不**绑定 `campaign_id`
- 注册解析时，对 `COMMUNITY_USER` / `EXTERNAL_PROMOTER` 按以下维度匹配唯一当前活动：
  - `communityId`（新用户注册社区）
  - `commerceId`（由社区解析或码上快照）
  - `ownerType`（与 `owner_scope` 匹配：`owner_scope = ownerType` 或 `owner_scope = ALL`）
  - `campaign_scene = REGISTER_ACQUISITION`（**服务端常量**，非请求参数、非推广码字段）
  - `campaign_status = ACTIVE`
  - `effective_start_at <= now <= effective_end_at`（空边界视为无限制）
- `campaign_code` 为活动实例唯一编号，仅用于后台识别与报表，**不参与**注册动态匹配
- 活动命中 0 条 → `CAMPAIGN_NOT_ACTIVE`，写 attempt，注册仍成功
- 活动命中 1 条 → QUALIFIED，`referral.campaign_id` 快照该活动 id
- 活动命中 >1 条 → `CAMPAIGN_AMBIGUOUS`，写 attempt，**禁止**随意取第一条

### 奖励规则命中（CUSTOMER_USER 发券）

- 维度：`community + trigger_type(REGISTER) + rule_code + reward_target + beneficiaryType + 时间`
- 0 条：无发券资格，推广仍 QUALIFIED
- 1 条：正常生成 PENDING 奖励
- >1 条：`RULE_AMBIGUOUS`，推广仍 QUALIFIED，写入 `status=FAILED` 奖励记录（`coupon_words_snapshot` 存原因）

同一 `community + trigger_type + rule_code + reward_target + beneficiary_type` 活动时间窗不得重叠（闭区间）。

### 活动时间重叠防护

同一 `community_id + commerce_id + campaign_scene` 下，若时间窗重叠则视为冲突：

- 相同 `owner_scope` 不可重叠
- `ALL` 与任意具体 `owner_scope`（`COMMUNITY_USER` / `EXTERNAL_PROMOTER` 等）不可重叠（避免注册时双命中）
- `TERMINATED` 活动不参与重叠校验

创建、修改时间、重新 `activate` 时由 `countOverlappingCampaign` + Service 校验；数据库层依赖业务校验（时间重叠无简单唯一索引）。

实现类：`NxCustomerPromotionCampaignServiceImpl.assertNoTimeOverlap` / `resolveActiveCampaign`。

## 三、活动修改限制

| 字段 | 无历史 referral | 已有历史 referral |
|---|---|---|
| `community_id` / `commerce_id` / `owner_scope` / `campaign_scene` / `campaign_code` | 创建时必填，创建后**不可改** | **不可改**；需新建活动或升版本 |
| `campaign_name` | 可改 | 可改 |
| `effective_start_at` | 可改（需过重叠校验） | **不可改** |
| `effective_end_at` | 可改（需过重叠校验） | 仅可**延后**，不可提前 |
| `campaign_status` | `activate` / `suspend` / `terminate` | 同上；`terminate` 写 `terminated_at` |
| `campaign_version` | 系统维护 | 范围变更应新建活动并递增版本 |

已有推广记录的活动的业绩统计、referral 明细以 `referral.campaign_id` 快照为准，不受后续活动状态变更影响。

## 四、无效码与 invitee 唯一键

- `uk_referral_invitee` 仅约束 **QUALIFIED** 的 referral 记录（一名新用户最多一条有效推广来源）
- 无效码尝试写入 `nx_customer_promotion_code_attempt`，**不占用** referral 主权
- 用户已成功注册后不可再次绑定其他推广人（注册与推广在同一事务；无补绑接口）

### code_attempt 审计字段与写入链路

| 字段 | 说明 |
|---|---|
| `promotion_code_snapshot` | 用户输入的原始推广码字符串 |
| `promotion_code_id` | 码表 id；`CODE_NOT_FOUND` 时为 **NULL** |
| `invitee_user_id` | 新注册用户 id |
| `community_id` / `commerce_id` | 注册时用户归属 |
| `invalid_reason` | 如 `CODE_NOT_FOUND`、`CAMPAIGN_NOT_ACTIVE`、`CAMPAIGN_AMBIGUOUS` 等 |
| `attempted_at` | 尝试时间 |
| `source_owner_type` / `source_owner_id` | 码可解析时从码表快照；否则从 resolve 结果 |
| `share_entry` | 分享入口（可选） |

写入链路：`NxCustomerController.saveNewCustomerMix` → `NxCustomerRegistrationServiceImpl` → `NxCustomerReferralServiceImpl.processPromotionAfterRegister` → `resolvePromotionCode` → 若 `!qualified` 则 `saveAttempt`。

## 五、一人一活动码并发控制

**目标**：三类主体同一时刻最多一个 `ACTIVE` 码；并发 create / regenerate / activate 不产生双 ACTIVE。

| 层级 | 机制 | 锁定对象 | 事务边界 |
|---|---|---|---|
| 应用行锁 | `nx_customer_promotion_code_owner_lock` + `SELECT ... FOR UPDATE` | `(owner_type, owner_id)` | `@Transactional` 包裹 `createCode` / `regenerateCode` / `updateCodeStatus(ACTIVE)` / `getOrCreate*` / `deactivateAllActiveCodesByOwner` |
| 数据库唯一约束 | 生成列 `active_owner_slot = CONCAT(owner_type, ':', owner_id)` 当 `code_status='ACTIVE'` | 每个 ACTIVE 码一行 | `uk_code_active_owner_slot`；`DuplicateKeyException` 时回查并返回已有 ACTIVE 或提示重试 |

流程：先 `ensureLockRow` → `lockOwnerForUpdate` → 批量 DISABLED 同主体旧 ACTIVE → 插入/更新新 ACTIVE。

## 六、人员停用与码停用联动

| 事件 | 联动行为 |
|---|---|
| `nxCouWorkingStatus` 从在职变为非在职 | 自动 `deactivatePromotionAssets`：停用 eligible + 全部 ACTIVE 码 |
| `nxcommunityuserpromotion/disable` | 同上 |
| `nxcustomerpromoter/suspend` 或 `terminate` | 同步 `deactivateAllActiveCodesByOwner`（内含主体锁） |
| 历史 referral / 业绩 | **不回写**失效 |

## 七、COMMUNITY_USER 的 commerce 来源

正式员工 **无** `commerceId` 字段。统一通过：

```text
nx_community_user.nx_COU_community_id
  → nx_community.nx_community_commerce_id
```

由 `PromotionScopeService.resolveCommerceIdByCommunityId()` 解析；Validator、建码与活动命中均使用此路径。

## 八、三类 ownerType

| ownerType | 主体表 | 校验器 |
|---|---|---|
| `CUSTOMER_USER` | `nx_customer_user` | `CustomerUserPromotionOwnerValidator` |
| `COMMUNITY_USER` | `nx_community_user` + `nx_community_user_promotion_eligible` | `CommunityUserPromotionOwnerValidator` |
| `EXTERNAL_PROMOTER` | `nx_customer_promoter` | `ExternalPromoterPromotionOwnerValidator` |

## 九、后台接口

### 推广活动 `api/nxcustomerpromotioncampaign`

| 接口 | 说明 |
|---|---|
| `POST /save` | 创建活动（body: communityId, commerceId, ownerScope, campaignName, effectiveStartAt, effectiveEndAt 等） |
| `POST /info?campaignId=` | 活动详情（含 qualifiedReferralCount） |
| `POST /list` | 列表（可按 communityId, commerceId, ownerScope, campaignStatus 筛选） |
| `POST /update` | 修改可编辑字段（名称、时间；受历史 referral 限制） |
| `POST /activate?campaignId=` | 启用（ACTIVE） |
| `POST /suspend?campaignId=&reason=` | 暂停 |
| `POST /terminate?campaignId=&reason=` | 提前终止 |
| `POST /stats?campaignId=` | 有效推广人数汇总 |
| `POST /statsByOwner?campaignId=` | 按推广人员（source_owner）统计 |
| `POST /referralDetails` | 推广明细（可选 sourceOwnerType, sourceOwnerId 筛选） |

### 其他

- 正式员工资格：`api/nxcommunityuserpromotion`（`enable/disable/info/stats/referralDetails`）
- 临时地推：`api/nxcustomerpromoter`
- 推广码（三类统一）：`api/nxcustomerpromotioncode`

`resolveScope` 已移除（commerce 解析保留在 Service 内部）。
