# AI 文本添加 Adsense 尾货广告商品 — 架构方案

> **版本**：v0.2  
> **状态**：架构设计（不含实现代码）  
> **管理端**：`jcyx-admin-mini` — 运营输入文字、预览草稿、确认保存/发布  
> **商城端**：`jcyx-mall-mini` — 首页 banner、商品详情抢购（倒计时/剩余份数）

---

## 1. 目标一句话

让采购/运营用一段自然语言，快速生成**可确认的限时限量尾货广告商品草稿**；确认后进入现有 **NxCommunityGoods + Adsense** 体系；是否对用户可见由 **Adsense 发布开关** 决定；可用于首页展示与优果团企微群传播。

```text
rawText（前端输入法文字，非后端 ASR）
  → AI 语义合同 + 草稿
  → 用户明确确认（保存 / 发布 两种动作）
  → TailDealAdsensePublishService（主权编排）
  → NxCommunityGoods + nx_community_adsense
  → 商城首页 / 详情页 / 群分享字段
```

---

## 2. 主权分层（基于现有代码）

| 层 | 主权实体 / Service | 职责 |
|----|-------------------|------|
| 商品基础资料 | `NxCommunityGoods` + `NxCommunityGoodsService` | 名称、规格、单位、重量、价格、分类、详情 |
| 广告商品能力 | Goods 上 Adsense 字段 + `nx_community_adsense` | 限时窗口、总/剩余份数、抢购价、详情页 Adsense 逻辑 |
| **用户可见性（发布开关）** | **`nx_community_adsense.nx_CA_status`** | **0=显示，1=隐藏**（管理端 `addAdsense`「是否显示」switch） |
| 商品级 Adsense 开关 | `nx_community_goods.nx_cg_is_open_adsense` | 1=该商品处于广告/限量抢购模式 |
| 传播场景 | 优果团企微群（参数 `targetWecomGroupId`） | 返回分享 path/卡片；第一版不自动发群 |
| AI | 新建 `community/marketing/ai/taildeal/*` | **只产草稿，不直写表** |

**不新建**与 Goods/Adsense 平行的 tailDeal 主商品体系。

---

## 3. 现有能力能否承载尾货抢购？

### 3.1 结论：**可以承载主体，需小范围扩展 + 一处首页过滤补强**

现有 Adsense 设计文档定位为「推爆品、限时限量」，与尾货抢购高度重合。  
缺口主要在：**起订量 / 购买倍数 / 每人限购**、**规格重量语义**、**发布开关在首页 API 的严格执行**。

### 3.2 发布开关（必须尊重，AI 不得绕过）

代码事实（`addAdsense/addAdsense.wxml` + `addAdsense.js`）：

| 字段 | 值 | 含义 |
|------|-----|------|
| `NxCommunityAdsenseEntity.nxCaStatus` | **0** | **显示**（switch 打开） |
| | **1** | **隐藏**（switch 关闭） |

现有 `comSaveGoodsAdsense` 创建 banner 时**写死** `nxCaStatus=0`（默认显示）。  
AI 发布 Service **必须按确认动作显式设置**，不能沿用写死逻辑。

**AI 确认语义与开关映射：**

| 用户确认动作 | `nxCgIsOpenAdsense` | `nxCaStatus` | 用户端可见性 |
|--------------|---------------------|--------------|--------------|
| **仅确认保存**（`CONFIRM_SAVE`） | 1（已配置 Adsense 参数） | **1（隐藏）** | 首页/广告栏不可见；可进管理端继续编辑、补图 |
| **确认发布 / 发首页 / 开始抢购**（`CONFIRM_PUBLISH`） | 1 | **0（显示）** | 首页 banner + 详情页 Adsense 逻辑可见 |

> 首页、广告栏、商品详情入口必须继续尊重 `nxCaStatus`。  
> **现状缺口**：`customerIndexData` 调用 `queryAdsenseByParams` 时**未传 `status=0`**，理论上可能展示已隐藏 banner。实施本功能时应**最小补丁**：首页查询增加 `nx_CA_status=0`（独立小改，不混入 AI 包逻辑）。

### 3.3 商品级开关 `nxCgIsOpenAdsense`

- 管理端 `disEditGoodsNx.confirmSaveAdsense`：保存广告前设 `nxCgIsOpenAdsense=1`，再调 `comSaveGoodsAdsense`。
- 商城 `zeroGoodsPage` 等：仅当 `nxCgIsOpenAdsense==1` 才走限量库存校验 `userCheckAdsenseQuantity`。
- AI 确认后两种动作都应设 `nxCgIsOpenAdsense=1`（已开启广告商品能力）；**是否对外展示**只看 `nxCaStatus`。

---

## 4. 可复用字段清单

### 4.1 `NxCommunityGoodsEntity`（已有）

| 尾货语义 | 现有字段 | 说明 |
|----------|----------|------|
| 商品名 | `nxCgGoodsName` | ✅ |
| 规格单位 | `nxCgGoodsStandardname` | 如「箱」 |
| 规格重量 | `nxCgGoodsStandardWeight` | 如「13斤」— 对应「13斤一箱」 |
| 抢购价 | `nxCgGoodsPrice` (+ integer/decimal 服务端拆分) | ✅ |
| 原价/划线价 | `nxCgGoodsHuaxianPrice` | 尾货低于原价 |
| 促销文案 | `nxCgPromotionWords` / `nxCgPromotionPrice` | 可选 |
| 售后/质量说明 | `nxCgGoodsDetail` | 拼接 afterSaleNote、qualityNote |
| 分类 | `nxCgCfgGoodsFatherId` | 创建新商品必填 |
| 商品类型 | `nxCgGoodsType` | 默认 0 → `zeroGoodsPage` |
| 开启广告模式 | `nxCgIsOpenAdsense` | ✅ |
| 广告时段 | `nxCgAdsenseStartTime/StopTime` + `*TimeZone` | 当日 HH:mm → 分钟数 |
| 总份数 | `nxCgAdsenseStockQuantity` | ✅ |
| 剩余份数 | `nxCgAdsenseRestQuantity` | 初始=总份数；支付时 `checkAdsenseGoods` 扣减 |
| 已抢份数 | （无独立字段） | 可算：`stock - rest` |

### 4.2 `NxCommunityAdsenseEntity`（已有）

| 用途 | 字段 |
|------|------|
| 关联商品 | `nxCaCgGoodsId` |
| 首页 banner 图 | `nxCaFilePath`（第一版可用商品顶图或占位，AI 不处理素材） |
| 点击跳转 | `nxCaClickTo`（`comSaveGoodsAdsense` 按 goodsType 生成 path） |
| 社区 | `nxCaCommunityId` |
| 时段 | `nxCaStartTime/StopTime` + Zone |
| **发布开关** | **`nxCaStatus`** |

### 4.3 现有主权 API（PublishService 应封装调用）

| API | 路径 | 用途 |
|-----|------|------|
| 创建商品 | `POST api/nxcommunitygoods/comSaveComGoods` | 新 Goods |
| 更新商品 | `POST api/nxcommunitygoods/updateComGoods` | 改价/详情/重量 |
| 开启广告 | `POST api/nxadsense/comSaveGoodsAdsense` | 写 banner + 更新 Goods Adsense 字段 |
| 更新广告参数 | `POST api/nxcommunitygoods/updateAdsendse` | 改时段/库存 |
| 关闭广告 | `POST api/nxadsense/closeGoodsAdsense` | 非本需求主链 |

管理端参考：`disEditGoodsNx.confirmSaveAdsense()`。

### 4.4 商城端现有行为

| 能力 | 位置 |
|------|------|
| 首页 banner 列表 | `index.js` → `customerIndexData` → `adsenseList` |
| 详情限量加购 | `zeroGoodsPage.js` → `userCheckAdsenseQuantity` + `nxCgAdsenseRestQuantity` |
| Adsense 时段内加购 | `nxCgAdsenseStartTimeZone/StopTimeZone` 与 `getNowMinute()` |

**倒计时**：现有详情页主要依赖时段 + 剩余份数；可在 `zeroGoodsPage` **增量**增加本地倒计时条（用服务端返回的 `endTime`/`nxCgAdsenseStopTime`，每秒刷新），不改订单主链。

---

## 5. 缺失字段与最小扩展方案

| 需求 | 现状 | 最小扩展（推荐） |
|------|------|------------------|
| 起订量 `minOrderQty` | ❌ | `nx_community_goods` 增列 `nx_cg_adsense_min_order_qty INT` |
| 购买倍数 `orderMultiple` | ❌ | 增列 `nx_cg_adsense_order_multiple INT DEFAULT 1` |
| 每人限购 `limitPerCustomer` | ❌ 现有 `userCheckAdsenseQuantity` 非 per-user cap | 增列 `nx_cg_adsense_limit_per_customer INT` + **新增**下单前校验（不改编译 POS/checkout 主流程，只在 Adsense 加购路径加校验） |
| 优果团群关联 | ❌ | **不写入 Goods 主表**；可选 `nx_community_adsense_ext`（`adsense_id`, `wecom_group_id`, `ygt_campaign_id`, `scene=TAIL_DEAL`）或 confirm 响应内存传递，第一版可仅响应 JSON |
| 完整 datetime 结束 | ⚠️ 仅当日 HH:mm | 第一版够用（「11点结束」→ 当天 11:00）；跨日尾货后续可加 `nx_cg_adsense_end_date` |
| 图片/视频 | 老流程 `updateComGoodsWithFile*` | **第一版 AI 不处理**；confirm 后用 `disEditGoodsNx` 补充 |

**不推荐**：新建 `nx_community_tail_deal` 主表接管商品与 Adsense。

---

## 6. AI 轻量 Harness 分层

借鉴 jczb「精彩账本」思想，nongxinle 轻量实现：

```text
┌─────────────────────────────────────────────────────────┐
│ 1. Intake          rawText 清洗、communityId 注入        │
│ 2. SemanticContract LLM 填槽 → TailDealAdsenseSemanticContract │
│ 3. DraftBuilder    Contract → communityGoodsPlan + adsensePlan │
│ 4. GoodsMatchAdapter  queryComGoodsByQuickSearch（确定性）│
│ 5. DeterministicValidator 价格/库存/时间/缺失字段/风险    │
│ 6. DraftRenderer   userFacingSummary + 草稿卡片字段       │
│ 7. ConfirmSafetyGate  白名单动作，拒绝模糊确认            │
│ 8. PublishService  主权保存（唯一写库入口）               │
│ 9. ReplayCases     5 条固定 rawText → 期望 JSON（单测）   │
└─────────────────────────────────────────────────────────┘
```

**不引入**：jczb 803 文件 Graph、semantic contract registry、Tool 编排框架。

### 6.1 模块包路径（建议）

```text
com.nongxinle.community.marketing.ai.taildeal/
  controller/TailDealAdsenseAiController
  service/TailDealAdsenseParseService
  service/TailDealAdsensePublishService      ← 唯一写库编排
  contract/TailDealAdsenseSemanticContract
  contract/TailDealAdsenseSemanticContractParser
  draft/TailDealAdsenseDraftBuilder
  validate/TailDealAdsenseDeterministicValidator
  render/TailDealAdsenseDraftRenderer
  gate/TailDealAdsenseConfirmSafetyGate
  session/TailDealAdsenseDraftSessionStore
  adapter/CommunityGoodsMatchAdapter
  client/CommunityGoodsLlmClient              ← DeepSeek，可参考 OcrController
resources/ai-prompts/taildeal/
  taildeal_adsense_extract.v1.md              ← 全新 Prompt
```

预估 **12～14 个 Java 类 + 1 Prompt + 本架构/契约文档**。

---

## 7. API 设计

基路径（与现有 `api/nxadsense` 一致）：

```text
POST /api/nxadsense/ai/parseTailDealDraft
POST /api/nxadsense/ai/confirmTailDeal
```

### 7.1 parseTailDealDraft

**请求：**

```json
{
  "rawText": "福建黑叶荔枝还有20箱，90元一箱，13斤一箱，2箱起订，每人限购2箱，今天11点结束，发首页，发优果团群里",
  "operatorUserId": 1,
  "communityId": 1,
  "commerceId": 1,
  "scene": "ADSENSE_TAIL_DEAL",
  "defaultMarketCloseTime": "12:00",
  "targetWecomGroupId": 12,
  "targetYgtCampaignId": null
}
```

**响应核心结构：**

```json
{
  "draftId": "...",
  "flowState": "DRAFT_READY | NEED_CLARIFY | GOODS_CHOICE",
  "confirmRequired": true,
  "semanticContract": { "...": "见 §8" },
  "communityGoodsPlan": {
    "action": "MATCH_EXISTING | CREATE_NEW | PENDING",
    "matchedGoodsId": 10460,
    "proposedGoods": { "nxCgGoodsName": "...", "nxCgGoodsStandardWeight": "13斤", "nxCgGoodsStandardname": "箱" }
  },
  "adsensePlan": {
    "nxCgAdsenseStartTime": "09:00",
    "nxCgAdsenseStopTime": "11:00",
    "nxCgAdsenseStockQuantity": 20,
    "nxCgAdsenseRestQuantity": 20,
    "nxCgGoodsPrice": "90",
    "nxCgAdsenseMinOrderQty": 2,
    "nxCgAdsenseOrderMultiple": 2,
    "nxCgAdsenseLimitPerCustomer": 2,
    "homepagePromotionIntent": true,
    "publishToWecomGroupIntent": true
  },
  "publishPlan": {
    "recommendedConfirmAction": "CONFIRM_PUBLISH",
    "targetWecomGroupId": 12
  },
  "missingFields": [],
  "riskWarnings": [],
  "userFacingSummary": "..."
}
```

**parse 永不写库。**

### 7.2 confirmTailDeal

**请求：**

```json
{
  "draftId": "...",
  "operatorUserId": 1,
  "confirmAction": "CONFIRM_SAVE | CONFIRM_PUBLISH",
  "confirmText": "确认发布",
  "selectedGoodsId": 10460,
  "selectedFatherGoodsId": 8801
}
```

**ConfirmSafetyGate 白名单：**

| confirmAction | 允许的前端文案示例 |
|---------------|-------------------|
| `CONFIRM_SAVE` | 确认保存、先保存不发布 |
| `CONFIRM_PUBLISH` | 确认发布、发布到首页、开始抢购 |

拒绝：「可以」「不错」「行」等模糊语句。

**响应（成功）：**

```json
{
  "flowState": "SUCCESS",
  "goodsId": 10460,
  "adsenseId": 456,
  "nxCgIsOpenAdsense": 1,
  "nxCaStatus": 0,
  "visibleOnHomepage": true,
  "goodsDetailPath": "zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=10460&...",
  "sharePath": "/pages/zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=10460&from=tailDeal&adsenseId=456",
  "wecomGroupId": 12,
  "publishToWecomGroup": true,
  "wecomShareCard": {
    "title": "【尾货抢购】福建黑叶荔枝 90元/箱",
    "desc": "共20箱，2箱起订，每人限购2箱，今日11:00截止",
    "miniProgramPath": "..."
  },
  "userFacingSummary": "已发布尾货广告商品，首页可见，可分享到优果团客户群。"
}
```

`CONFIRM_SAVE` 时：`nxCaStatus=1`，`visibleOnHomepage=false`，仍返回 `goodsId/adsenseId` 供管理端跳转 `disEditGoodsNx` 补图。

---

## 8. TailDealAdsenseSemanticContract

LLM 只输出 JSON 槽位；Java 做类型转换与校验。

```json
{
  "intent": "CREATE_ADSENSE_TAIL_DEAL_GOODS",
  "goodsName": "",
  "goodsSpec": "",
  "unit": "",
  "standardWeight": "",
  "dealPrice": null,
  "originalPrice": null,
  "totalStock": null,
  "minOrderQty": null,
  "orderMultiple": null,
  "limitPerCustomer": null,
  "startTimeText": "",
  "endTimeText": "",
  "homepagePromotionIntent": true,
  "publishToWecomGroupIntent": true,
  "targetWecomGroupId": null,
  "targetYgtCampaignId": null,
  "qualityNote": "",
  "afterSaleNote": "",
  "needConfirmation": true
}
```

**时间规则（DeterministicValidator，非 LLM 发布）：**

| 表达 | 解析 |
|------|------|
| 「11点结束」 | 当天 11:00 |
| 「中午前」 | 当天 12:00 |
| 「闭市前」 | `defaultMarketCloseTime` 或 12:00 |
| 无截止时间 | `missingFields += endTimeText`，禁止 confirm |
| endTime ≤ now | 校验失败 |

映射到 Adsense：`nxCgAdsenseStartTime/StopTime`（HH:mm）+ 现有 TimeZone 算法。

---

## 9. confirm 后主链（TailDealAdsensePublishService）

```text
confirmTailDeal(draftId, confirmAction)
  1. 加载 draft，校验未过期、未发布
  2. ConfirmSafetyGate
  3. DeterministicValidator 再跑一遍
  4. communityGoodsPlan:
       MATCH → goodsId
       CREATE_NEW → 内部调 NxCommunityGoodsService.save（等同 comSaveComGoods 逻辑）
  5. 组装 NxCommunityGoodsEntity（价格、Adsense 时段、库存、扩展限购字段）
  6. 内部调 Adsense 主权方法（从 comSaveGoodsAdsense 抽取或封装）:
       - 创建 nx_community_adsense
       - nxCgIsOpenAdsense = 1
       - nxCaStatus = confirmAction 决定（SAVE→1, PUBLISH→0）
  7. 可选写 adsense_ext（群 ID）
  8. 返回 sharePath / wecomShareCard / visibleOnHomepage
```

**为何新增 PublishService 而非 Controller 直调？**

- 现有 `comSaveGoodsAdsense` 写死 `nxCaStatus=0`，且与 HTTP 层耦合。
- 需要同一事务内：Goods 创建/更新 + Adsense + 开关语义 + 扩展字段。
- 建议：**抽取** `NxCommunityAdsensePublishService`（或扩展现有 Service 的 package 方法），AI 与 `disEditGoodsNx` 未来共用。

**第一版不做的：**

- 自动企微发群消息
- AI 绑定图片/视频
- 写入 `ygt_campaign_goods`

---

## 10. 服务端校验矩阵

| 规则 | parse | confirm | 加购/下单（Adsense 路径） |
|------|-------|---------|---------------------------|
| dealPrice > 0 | ✅ | ✅ | — |
| dealPrice < originalPrice（尾货） | ⚠️ 警告 | ✅ | — |
| totalStock > 0 | ✅ | ✅ | ✅ rest > 0 |
| limitPerCustomer ≤ totalStock | ✅ | ✅ | ✅ 新校验 |
| minOrderQty ≥ 1 | ✅ | ✅ | ✅ 新校验 |
| orderMultiple ≥ 1 | ✅ | ✅ | ✅ 新校验 |
| endTime > now | ✅ | ✅ | ✅ 现有 time zone |
| 商品分类存在（新建时） | ✅ | ✅ | — |
| nxCaStatus 与 confirmAction 一致 | — | ✅ | 首页查 status=0 |
| 模糊确认 | — | ❌ 拒绝 | — |

**已抢份数**：`nxCgAdsenseStockQuantity - nxCgAdsenseRestQuantity`，详情页展示；库存轮询 30～60s 调商品详情接口。

---

## 11. 优果团企微群（传播，非主权）

- 草稿/confirm 请求可带 `targetWecomGroupId`、`publishToWecomGroupIntent`。
- confirm 响应返回 `sharePath`、`wecomShareCard`，供管理端复制或后续分享页使用。
- **不**挂到 `api/ygt/*` 商品创建；**不**改 YGT candidate / 企微回调 / 存档。
- 可与现有 `shareLanding` 链路并列，参数带 `goodsId` + `adsenseId` + `from=tailDeal`。

---

## 12. 前端分工

### 12.1 管理端 `jcyx-admin-mini`（新建）

```text
subPackage/pages/tailDeal/aiAddTailDeal/
  - 文字输入框（微信语音输入法 → 文字）
  - 调 parseTailDealDraft
  - 草稿卡片（价/库存/起订/限购/截止/发布意图）
  - 两个按钮：「确认保存（不发布）」「确认发布到首页」
  - 成功后：跳转 disEditGoodsNx 补图 / 展示 sharePath
```

### 12.2 商城 `jcyx-mall-mini`（增量）

| 页面 | 改动 |
|------|------|
| `pages/index/index` | 已有 `adsenseList`；依赖 `nxCaStatus=0` 过滤（服务端补丁） |
| `pages/zeroGoodsPage` | 增量：倒计时条、已抢/剩余、起订/倍数/限购展示；库存定时刷新 |
| 加购 | Adsense 路径增加 min/multiple/limit 服务端校验 |

**不改**：购物车聚合、POS、checkout、coupon 核销、YGT 注册/候选单。

---

## 13. 五条例回放验收用例

| # | rawText | flowState | 关键断言 |
|---|---------|-----------|----------|
| 1 | 福建黑叶荔枝还有二十箱，九十元一箱，十三斤一箱，两箱起订，每人限购两箱，今天十一点结束 | DRAFT_READY | weight=13斤, stock=20, end=11:00 |
| 2 | 把黑叶荔枝做个尾货抢购，剩二十件，价格九十，十点半截止 | DRAFT_READY | end=10:30 |
| 3 | 河北水蜜桃还有五十箱，十七块三毛八，三箱起订，十二点闭市前清完 | DRAFT_READY | price=17.38, close=12:00 |
| 4 | 这个荔枝库存二十，九十元，发首页，发群里，限购两件 | DRAFT_READY | publish intent; CONFIRM_PUBLISH → nxCaStatus=0 |
| 5 | 黑叶荔枝做活动 | NEED_CLARIFY | missing price/stock/end; confirm 拒绝 |

额外：

| # | 动作 | 断言 |
|---|------|------|
| 6 | CONFIRM_SAVE | goods+adsense 创建, nxCaStatus=1, 首页不可见 |
| 7 | CONFIRM_PUBLISH | nxCaStatus=0, customerIndexData 可见 |
| 8 | confirmText=「可以」 | CONFIRM_REJECTED，无写库 |

Replay JSON 放 `src/test/resources/taildeal/replay/`（实施阶段）。

---

## 14. 与旧主链隔离

| 主链 | 策略 |
|------|------|
| 订单 / 购物车 / POS / checkout | 不修改主流程；Adsense 加购仍走现有 sub order |
| couponRule / 核销 | 不调用 `NxCommunityCouponRuleSaveService` |
| YGT candidate / message parse | 不修改 |
| 企微回调 / 存档 | 不修改 |
| 图片/视频上传 | 仍走 `disEditGoodsNx` 现有接口 |

AI 包仅新增 Controller + PublishService 编排，**内部调用**现有 Goods/Adsense Service。

---

## 15. 实施阶段建议

| 阶段 | 内容 | 改动面 |
|------|------|--------|
| **P0** | SQL 3 列扩展 + `customerIndexData` 增加 `status=0` | 小 |
| **P1** | AI parse/confirm + PublishService + Prompt | 中（新包） |
| **P2** | 管理端 aiAddTailDeal 页 | 中 |
| **P3** | 商城 zeroGoodsPage 倒计时/限购展示 + 加购校验 | 小 |
| **P4** | Replay 单测 + adsense_ext（群关联） | 小 |

---

## 16. 关联文档

- API 请求/响应字段细节：`ai-adsense-tail-deal-api-contract.md`（可合并到本文 §7）
- 商品字段参考：`app/jcyx/jcyx-admin-mini/doc/菜品业务模型与字段参考.md`
- Adsense 管理端交互：`disEditGoodsNx` 首页广告区块

---

## 17. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2026-06-30 | 初版 API 契约 |
| v0.2 | 2026-06-30 | 架构方案：Adsense 主权、nxCaStatus 发布开关、SAVE/PUBLISH 双确认、代码核查结论 |
