# AI 文本添加 Adsense 尾货广告商品 API 契约

> **版本**：v0.1（架构契约，不含实现代码）  
> **适用范围**：批发市场尾货抢购场景；优果团企微群为传播/抢购渠道，**不是商品落库主权**  
> **主权边界**：`NxCommunityGoods`（商品资料）→ `nx_community_adsense` + Goods Adsense 字段（限时限量展示）→ 首页/详情页/群分享

---

## 0. 一句话定义

用 AI 把运营输入的自然语言（`rawText`）解析为 **Adsense 尾货广告商品草稿**；用户**明确确认**后，先创建/匹配 `NxCommunityGoods`，再调用现有 Adsense 保存能力开启广告商品，用于首页展示与优果团企微群抢购。

**不是**：新建独立 tailDeal 商品体系；不是语音识别后端；不是 YGT 商品主权；不是规则券 couponRule。

---

## 1. 业务主链

```text
rawText（前端文字/语音输入法转文字）
  -> Intake
  -> TailDealAdsenseSemanticContract（LLM 填槽）
  -> TailDealAdsenseDraft（确定性 Draft Builder）
  -> 商品匹配 / 创建建议
  -> Adsense 字段确定性校验
  -> 用户预览（Renderer）
  -> Confirm Safety Gate（必须明确确认）
  -> TailDealAdsensePublishService.confirmPublish
       -> 匹配或创建 NxCommunityGoods
       -> 复用 comSaveGoodsAdsense / updateAdsendse
       -> 返回首页/详情/优果团群分享字段
```

### 主权分层

| 层 | 主权 | 说明 |
|----|------|------|
| 商品基础资料 | `NxCommunityGoods` + `NxCommunityGoodsService` | 名称、规格、单位、分类、常规价格 |
| 限时限量广告 | Goods 上 Adsense 字段 + `nx_community_adsense` | 推爆品、首页 banner、库存扣减 |
| 传播场景 | 优果团企微群 / YGT Campaign（可选关联） | 分享路径、群 ID；第一版不自动发群消息 |
| AI | 仅草稿 | **不直写** goods / adsense / order / coupon 表 |

---

## 2. API 命名与路径

### 2.1 推荐路径（挂在 Adsense 业务域）

与现有 Controller 风格一致（现有 Adsense 基路径为 `api/nxadsense`）：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/nxadsense/ai/parseTailDealDraft` | 解析草稿 |
| POST | `/api/nxadsense/ai/confirmPublishTailDeal` | 确认发布 |

备选（语义更清晰）：

| 方法 | 路径 |
|------|------|
| POST | `/api/community/adsense/ai/parseTailDealDraft` |
| POST | `/api/community/adsense/ai/confirmPublishTailDeal` |

### 2.2 禁止使用的路径

```text
/api/ygt/tail-deal/...     # YGT 不是商品主权
/api/nxcommunitycoupon/... # 规则券域
```

### 2.3 能力命名（产品/代码）

- AI 文本添加广告抢购商品
- AI 文本添加尾货广告商品
- AI 创建 Adsense 抢购商品草稿

---

## 3. parseTailDealDraft

### 3.1 请求

```json
{
  "rawText": "福建黑叶荔枝还有20箱，90一箱，2箱起订，每人限购2箱，11点结束，发首页，发优果团群里",
  "operatorUserId": 1,
  "communityId": 1,
  "commerceId": 1,
  "scene": "ADSENSE_TAIL_DEAL",
  "defaultMarketCloseTime": "12:00",
  "targetWecomGroupId": 12,
  "targetYgtCampaignId": null,
  "publishToWecomGroup": true,
  "homepagePromotion": true
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `rawText` | 是 | 用户输入全文；后端不做 ASR |
| `operatorUserId` | 是 | 操作人 |
| `communityId` | 是 | 社区/门店 |
| `commerceId` | 建议 | 创建 Goods 时需要 |
| `scene` | 是 | 固定 `ADSENSE_TAIL_DEAL` |
| `defaultMarketCloseTime` | 否 | 「闭市前」默认时间，如 `12:00` |
| `targetWecomGroupId` | 否 | 优果团企微群 ID（传播目标） |
| `targetYgtCampaignId` | 否 | 可选关联团期，不写入商品主权 |
| `publishToWecomGroup` | 否 | 是否计划发群 |
| `homepagePromotion` | 否 | 是否上首页 Adsense |

### 3.2 响应（成功 — 草稿就绪）

```json
{
  "code": 0,
  "data": {
    "draftId": "draft_20260630_abc123",
    "status": "DRAFT_READY",
    "flowState": "DRAFT_READY",
    "confirmRequired": true,
    "semanticContract": {
      "intent": "CREATE_ADSENSE_TAIL_DEAL_GOODS",
      "goodsName": "福建黑叶荔枝",
      "goodsSpec": "",
      "unit": "箱",
      "dealPrice": 90,
      "originalPrice": 100,
      "totalStock": 20,
      "minOrderQty": 2,
      "orderMultiple": 2,
      "limitPerCustomer": 2,
      "startTimeText": "今日09:00",
      "endTimeText": "今日11:00",
      "homepagePromotion": true,
      "publishToWecomGroup": true,
      "targetWecomGroupId": 12,
      "targetYgtCampaignId": null,
      "qualityNote": "",
      "afterSaleNote": "",
      "needConfirmation": true
    },
    "communityGoodsPlan": {
      "action": "MATCH_EXISTING",
      "matchedGoodsId": 10460,
      "matchedGoodsName": "福建A级黑叶荔枝",
      "confidence": 0.92,
      "createNewGoodsRequired": false,
      "suggestedFatherGoodsId": null,
      "suggestedGoodsType": 0
    },
    "adsensePlan": {
      "action": "ENABLE_ADSENSE",
      "nxCgIsOpenAdsense": 1,
      "nxCgAdsenseStartTime": "09:00",
      "nxCgAdsenseStopTime": "11:00",
      "nxCgAdsenseStockQuantity": 20,
      "nxCgAdsenseRestQuantity": 20,
      "nxCgGoodsPrice": "90",
      "nxCgPromotionPrice": "90",
      "homepagePromotion": true
    },
    "deal": {
      "dealPrice": 90,
      "originalPrice": 100,
      "totalStock": 20,
      "minOrderQty": 2,
      "orderMultiple": 2,
      "limitPerCustomer": 2,
      "startTime": "2026-06-30 09:00:00",
      "endTime": "2026-06-30 11:00:00",
      "deadlineText": "今日11:00结束"
    },
    "matchedGoods": {
      "goodsId": 10460,
      "goodsName": "福建A级黑叶荔枝",
      "confidence": 0.92
    },
    "goodsCandidates": [],
    "missingFields": [],
    "riskWarnings": [],
    "userFacingSummary": "已生成尾货广告商品草稿：福建A级黑叶荔枝，90元/箱，共20箱，2箱起订，每人限购2箱，今日11点结束。确认后将创建/匹配商品并开启 Adsense 首页展示，可分享到优果团客户群。"
  }
}
```

### 3.3 响应（字段不足 — 需补充）

`flowState = NEED_CLARIFY`

```json
{
  "code": 0,
  "data": {
    "draftId": "draft_20260630_def456",
    "status": "NEED_CLARIFY",
    "flowState": "NEED_CLARIFY",
    "confirmRequired": false,
    "semanticContract": {
      "intent": "CREATE_ADSENSE_TAIL_DEAL_GOODS",
      "goodsName": "黑叶荔枝",
      "dealPrice": null,
      "totalStock": null,
      "endTimeText": ""
    },
    "communityGoodsPlan": {
      "action": "PENDING",
      "createNewGoodsRequired": null
    },
    "adsensePlan": null,
    "missingFields": ["dealPrice", "totalStock", "endTimeText"],
    "riskWarnings": [],
    "userFacingSummary": "还缺少：抢购价、总库存、截止时间。请补充后再生成草稿。"
  }
}
```

### 3.4 响应（商品匹配不确定）

`flowState = GOODS_CHOICE`

```json
{
  "code": 0,
  "data": {
    "draftId": "draft_20260630_ghi789",
    "status": "GOODS_CHOICE",
    "flowState": "GOODS_CHOICE",
    "confirmRequired": false,
    "goodsCandidates": [
      { "goodsId": 10460, "goodsName": "福建A级黑叶荔枝", "confidence": 0.71 },
      { "goodsId": 10461, "goodsName": "黑叶荔枝 普通", "confidence": 0.68 }
    ],
    "missingFields": [],
    "userFacingSummary": "找到多个相似商品，请选择要开启尾货广告的商品，或选择创建新商品。"
  }
}
```

### 3.5 响应（无匹配 — 允许创建新 Goods 草稿）

`flowState = DRAFT_READY`，`communityGoodsPlan.action = CREATE_NEW`

```json
{
  "communityGoodsPlan": {
    "action": "CREATE_NEW",
    "matchedGoodsId": null,
    "createNewGoodsRequired": true,
    "proposedGoods": {
      "nxCgGoodsName": "福建黑叶荔枝",
      "nxCgGoodsStandardname": "箱",
      "nxCgGoodsPrice": "90",
      "nxCgGoodsType": 0,
      "nxCgCfgGoodsFatherId": null,
      "nxCgGoodsDetail": "",
      "missingGoodsFields": ["nxCgCfgGoodsFatherId"]
    }
  },
  "missingFields": ["nxCgCfgGoodsFatherId"],
  "userFacingSummary": "未匹配到现有商品，将创建新商品并开启 Adsense；请先选择商品分类。"
}
```

---

## 4. confirmPublishTailDeal

### 4.1 请求

```json
{
  "draftId": "draft_20260630_abc123",
  "operatorUserId": 1,
  "confirmAction": "CONFIRM_PUBLISH",
  "confirmText": "确认发布",
  "selectedGoodsId": 10460,
  "selectedFatherGoodsId": 8801,
  "override": {
    "dealPrice": 90,
    "totalStock": 20,
    "endTime": "2026-06-30 11:00:00"
  }
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `draftId` | 是 | parse 返回的草稿 ID |
| `operatorUserId` | 是 | 操作人 |
| `confirmAction` | 是 | 必须为枚举白名单（见 §10） |
| `confirmText` | 建议 | 前端按钮文案，供审计 |
| `selectedGoodsId` | 条件 | `GOODS_CHOICE` 时必选 |
| `selectedFatherGoodsId` | 条件 | 创建新 Goods 且缺分类时必选 |
| `override` | 否 | 用户在预览页微调字段 |

### 4.2 响应（发布成功）

```json
{
  "code": 0,
  "data": {
    "status": "PUBLISHED",
    "flowState": "SUCCESS",
    "goodsId": 10460,
    "adsenseId": 456,
    "goodsName": "福建A级黑叶荔枝",
    "nxCgIsOpenAdsense": 1,
    "nxCgAdsenseRestQuantity": 20,
    "nxCgAdsenseStockQuantity": 20,
    "startTime": "2026-06-30 09:00:00",
    "endTime": "2026-06-30 11:00:00",
    "homepagePromotion": true,
    "goodsDetailPath": "zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=10460&from=index&orderType=0&spId=-1&pindanId=-1",
    "sharePath": "/pages/zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=10460&from=tailDeal&adsenseId=456",
    "wecomGroupId": 12,
    "targetYgtCampaignId": null,
    "publishToWecomGroup": true,
    "wecomShareCard": {
      "title": "【尾货抢购】福建A级黑叶荔枝 90元/箱",
      "desc": "共20箱，2箱起订，每人限购2箱，今日11:00截止",
      "path": "subPackage/pages/youguotuan/shareLanding/index?shareCode=...",
      "note": "第一版仅返回字段，不自动调用企微发消息 API"
    },
    "userFacingSummary": "已发布尾货广告商品，已开启 Adsense 首页展示，可发送到优果团客户群抢购。"
  }
}
```

### 4.3 响应（确认被拒绝）

```json
{
  "code": -1,
  "msg": "未检测到明确确认动作，禁止发布",
  "data": {
    "flowState": "CONFIRM_REJECTED",
    "allowedConfirmActions": ["CONFIRM_PUBLISH", "CONFIRM_SAVE_ADSENSE"]
  }
}
```

---

## 5. TailDealAdsenseSemanticContract 字段

LLM 只允许输出以下 JSON 槽位；Java 层做类型转换与校验，**不用 if/else 关键词路由业务**。

```json
{
  "intent": "CREATE_ADSENSE_TAIL_DEAL_GOODS",
  "goodsName": "",
  "goodsSpec": "",
  "unit": "",
  "dealPrice": null,
  "originalPrice": null,
  "totalStock": null,
  "minOrderQty": null,
  "orderMultiple": null,
  "limitPerCustomer": null,
  "startTimeText": "",
  "endTimeText": "",
  "homepagePromotion": true,
  "publishToWecomGroup": true,
  "targetWecomGroupId": null,
  "targetYgtCampaignId": null,
  "qualityNote": "",
  "afterSaleNote": "",
  "needConfirmation": true
}
```

### 5.1 时间解析规则（确定性 Java，非 LLM 猜测发布）

| 表达 | 默认解析 |
|------|----------|
| 「11点结束」 | 当天 11:00 |
| 「中午前」 | 当天 12:00 |
| 「闭市前」 | `defaultMarketCloseTime` 或 12:00 |
| 仅「做活动」无时间 | `missingFields` 含 `endTimeText`，**禁止 confirm** |
| `endTime <= now` | 校验失败，禁止 confirm |

Adsense 现有时间模型为 **HH:mm + 当日 minute zone**（见 §6），Draft Builder 负责把 datetime 映射为：

- `nxCgAdsenseStartTime` / `nxCgAdsenseStopTime`（如 `09:00` / `11:00`）
- `nxCgAdsenseStartTimeZone` / `nxCgAdsenseStopTimeZone`（分钟数，由现有 Controller 算法生成）

---

## 6. 草稿如何表示「先 Community Goods，再升级 Adsense」

草稿分三块，职责分离：

### 6.1 `communityGoodsPlan`

| `action` | 含义 |
|----------|------|
| `MATCH_EXISTING` | 匹配到已有 `nxCommunityGoodsId` |
| `CREATE_NEW` | confirm 时调用 `comSaveComGoods` 创建 |
| `PENDING` | 信息不足，尚未决策 |
| `USER_SELECTED` | 用户从 `goodsCandidates` 中选定 |

### 6.2 `adsensePlan`

表示 confirm 后要写入的 Adsense 字段（映射到现有 Entity）：

| 草稿字段 | 现有 Entity 字段 |
|----------|------------------|
| 开启广告 | `nxCgIsOpenAdsense = 1` |
| 开始/结束时刻 | `nxCgAdsenseStartTime` / `nxCgAdsenseStopTime` |
| 分钟 zone | `nxCgAdsenseStartTimeZone` / `nxCgAdsenseStopTimeZone` |
| 总库存 | `nxCgAdsenseStockQuantity` |
| 剩余库存 | `nxCgAdsenseRestQuantity`（初始 = totalStock） |
| 抢购价 | `nxCgGoodsPrice`（及 `nxCgGoodsPriceInteger/Decimal` 由 Service 拆分） |
| 促销展示价/文案 | `nxCgPromotionPrice` / `nxCgPromotionWords`（可选） |
| 原价参考 | `nxCgGoodsHuaxianPrice` 或 `nxCgPromotionType`（按现有促销模型） |
| 首页 banner | 由 `comSaveGoodsAdsense` 写 `nx_community_adsense` |

### 6.3 `publishPlan`（传播，非落库主权）

```json
{
  "homepagePromotion": true,
  "publishToWecomGroup": true,
  "targetWecomGroupId": 12,
  "targetYgtCampaignId": null
}
```

仅影响 confirm 响应中的 `sharePath` / `wecomShareCard`，第一版**不写入** YGT 商品表。

---

## 7. 现有 Adsense 字段复用清单

### 7.1 `NxCommunityGoodsEntity`（已有）

| 字段 | 尾货用途 | 现状 |
|------|----------|------|
| `nxCgIsOpenAdsense` | 是否广告商品 | ✅ 已有 |
| `nxCgAdsenseStartTime` / `StopTime` | 当日抢购窗口 | ✅ 已有（HH:mm） |
| `nxCgAdsenseStartTimeZone` / `StopTimeZone` | 分钟比较 | ✅ 已有 |
| `nxCgAdsenseStockQuantity` | 总库存 | ✅ 已有 |
| `nxCgAdsenseRestQuantity` | 剩余库存 | ✅ 已有；支付时 `checkAdsenseGoods` 扣减 |
| `nxCgGoodsPrice` | 抢购价 | ✅ 已有 |
| `nxCgPromotionPrice` / `nxCgPromotionWords` / `nxCgPromotionType` | 促销展示 | ✅ 已有 |
| `nxCgGoodsHuaxianPrice` | 划线原价 | ✅ 已有 |
| `nxCgGoodsDetail` | 售后/质量说明 | ✅ 已有 |
| `nxCgGoodsName` / `Standardname` / `Type` / `CfgGoodsFatherId` | 商品基础 | ✅ 已有 |

### 7.2 `NxCommunityAdsenseEntity`（已有）

| 字段 | 用途 |
|------|------|
| `nxCaCgGoodsId` | 关联商品 |
| `nxCaClickTo` | 小程序详情 path（`comSaveGoodsAdsense` 按 `nxCgGoodsType` 生成） |
| `nxCaFilePath` | banner 图 |
| `nxCaStartTime` / `StopTime` + Zone | 与 Goods 同步 |
| `nxCaCommunityId` | 社区 |

### 7.3 现有保存入口（confirm 必须复用）

| API | 路径 | 用途 |
|-----|------|------|
| 创建商品 | `POST api/nxcommunitygoods/comSaveComGoods` | 新 Goods |
| 更新商品 | `POST api/nxcommunitygoods/updateComGoods` | 改价/详情 |
| 开启 Adsense | `POST api/nxadsense/comSaveGoodsAdsense` | 写 banner + 开广告 |
| 更新 Adsense 时段/库存 | `POST api/nxcommunitygoods/updateAdsendse` | 已有商品改广告参数 |
| 关闭 Adsense | `POST api/nxadsense/closeGoodsAdsense` | 非本需求主链 |

管理端参考：`disEditGoodsNx.confirmSaveAdsense()` → `comSaveGoodsAdsense`。

---

## 8. 缺失字段与扩展建议

### 8.1 当前 Adsense **没有**的尾货字段

| 需求字段 | 现状 | 第一版建议 |
|----------|------|------------|
| `minOrderQty` 起订量 | ❌ 无专用字段 | **小扩展**：Goods 表新增 `nx_cg_adsense_min_order_qty`；或暂存 `nxCgGoodsDetail` 结构化 JSON（不推荐长期） |
| `orderMultiple` 购买倍数 | ❌ 无 | 同上：`nx_cg_adsense_order_multiple` |
| `limitPerCustomer` 每人限购 | ❌ 无；现有 `userCheckAdsenseQuantity` 逻辑与「已下单总量 vs rest」相关，**不是** per-customer cap | **必须扩展** + 下单前校验 |
| `startTime/endTime` 完整 datetime | ⚠️ 仅有当日 HH:mm zone | 尾货场景可接受「当日闭市前」；跨日需扩展 `nx_cg_adsense_end_date` 或 datetime 字段 |
| 已抢数量 | ⚠️ 可计算 `stock - rest` | 可不存字段，详情页 `stock - rest` |
| 优果团群关联 | ❌ 无 | **不写入 Adsense 表**；放 confirm 响应 + 可选扩展表 `nx_community_adsense_publish_ext`（仅 `adsenseId/groupId/campaignId`） |
| 倒计时 | ⚠️ 前端用 `endTime` + server time | 详情页本地每秒刷新；接口返回 `endTime` ISO |

### 8.2 推荐第一版扩展（最小）

**方案 A（推荐）**：在 `nx_community_goods` 增加 3 个 Adsense 附属列：

```sql
nx_cg_adsense_min_order_qty      INT NULL
nx_cg_adsense_order_multiple     INT NULL DEFAULT 1
nx_cg_adsense_limit_per_customer INT NULL
```

**方案 B（更干净）**：新建 **Adsense 扩展表**（不是 tailDeal 主表）：

```text
nx_community_adsense_ext
  adsense_id / goods_id
  min_order_qty, order_multiple, limit_per_customer
  publish_wecom_group_id, publish_ygt_campaign_id
  after_sale_note, quality_note
```

主权仍在 Adsense 广告商品；扩展表只挂附加规则。

### 8.3 校验落点

| 规则 | parse 阶段 | confirm 阶段 | 下单阶段 |
|------|------------|--------------|----------|
| 价格 > 0 | ✅ | ✅ | — |
| 抢购价 < 原价 | ✅ 警告 | ✅ | — |
| totalStock > 0 | ✅ | ✅ | ✅ |
| limitPerCustomer ≤ totalStock | ✅ | ✅ | ✅ 新增校验 |
| minOrderQty / multiple | ✅ | ✅ | ✅ 新增校验 |
| endTime > now | ✅ | ✅ | ✅ 现有 time zone |
| 库存扣减 | — | 初始化 rest=stock | ✅ `checkAdsenseGoods` |

---

## 9. 商品匹配不到时的 Community Goods 草稿

### 9.1 匹配策略（确定性 Tool，非关键词 if/else）

1. `queryComGoodsByQuickSearch` / 名称模糊 + 别名
2. 唯一高置信 → `MATCH_EXISTING`
3. 多候选 → `GOODS_CHOICE`
4. 无匹配 → `CREATE_NEW` + `proposedGoods`

### 9.2 创建新 Goods 最低必填

| 字段 | 来源 |
|------|------|
| `nxCgCommunityId` / `nxCgCommerceId` | 请求上下文 |
| `nxCgGoodsName` | contract |
| `nxCgGoodsStandardname` | contract.unit |
| `nxCgGoodsPrice` | contract.dealPrice |
| `nxCgCfgGoodsFatherId` | 匹配分类或用户选择 |
| `nxCgGoodsType` | 默认 0（普通）→ `zeroGoodsPage` |
| `nxCgGoodsDetail` | afterSaleNote + qualityNote |

缺 `nxCgCfgGoodsFatherId` → `missingFields`，**禁止 confirm**。

### 9.3 confirm 时创建顺序

```text
if CREATE_NEW:
  comSaveComGoods(proposedGoods)
  -> goodsId
else:
  updateComGoods(价格/详情)  // 可选
  -> goodsId

comSaveGoodsAdsense(goods with adsense fields)
  -> adsenseId + nxCgIsOpenAdsense=1
```

---

## 10. Confirm Safety Gate（无明确确认不落库）

### 10.1 允许的 confirmAction 白名单

```text
CONFIRM_PUBLISH
CONFIRM_SAVE_ADSENSE
```

### 10.2 允许的 confirmText 白名单（前端按钮）

```text
确认发布
确认保存
发布这个抢购
发布尾货广告
```

### 10.3 必须拒绝的表述

```text
可以 / 好的 / 看着不错 / 行吧 / OK
```

### 10.3 硬规则

- `parseTailDealDraft` **永不写库**
- `confirmPublishTailDeal` 必须：
  - 校验 `draftId` 有效、未过期、未发布
  - 重新跑一遍 DeterministicValidator
  - 通过 ConfirmSafetyGate
  - 才调用 `TailDealAdsensePublishService`

---

## 11. 优果团企微群分享字段

优果团是**传播渠道**，不是商品主权。

### 11.1 confirm 响应必备

```json
{
  "goodsId": 123,
  "adsenseId": 456,
  "goodsName": "福建黑叶荔枝",
  "goodsDetailPath": "zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=123&...",
  "sharePath": "/pages/zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId=123&from=tailDeal&adsenseId=456",
  "wecomGroupId": 12,
  "publishToWecomGroup": true,
  "targetYgtCampaignId": null,
  "wecomShareCard": {
    "title": "【尾货抢购】...",
    "desc": "...",
    "miniProgramPath": "...",
    "imageUrl": "..."
  }
}
```

### 11.2 第一版范围

- ✅ 返回分享 path / 卡片字段
- ❌ 不自动调用企微群发 API
- ❌ 不写入 `ygt_campaign_goods`
- 可选：运营复制 `sharePath` 到优果团分享链路（与现有 `shareLanding` 并列）

---

## 12. 不影响现有主链的保证

| 主链 | 策略 |
|------|------|
| 老商城订单 | 仅复用现有 Adsense 下单/扣库存；不改 `NxCommunityOrdersController` 主流程 |
| 购物车 | 不改 cart 聚合逻辑；Adsense 商品仍走现有 sub order |
| POS / checkout | 不接入本 AI |
| couponRule / 核销 | 不调用 `NxCommunityCouponRuleSaveService` |
| YGT 候选单 confirm | 不调用 `YgtOrderCandidateService` |
| 企微回调 / 存档 | 不修改 ingest 主链 |
| 优果团会员注册 | 不修改 share/register 主链 |

**实现原则**：

- 新增包：`community/marketing/ai/`（或 `community/adsense/ai/`）
- 新增 Service：`TailDealAdsensePublishService` **内部调用**现有 Goods/Adsense Service
- 不修改 Controller 签名；必要时仅在 Goods/Adsense Service 增加 package-private 方法供 PublishService 复用

---

## 13. 验收用例（Replay Case）

| # | rawText | 期望 flowState | 期望结果 |
|---|---------|----------------|----------|
| 1 | 福建黑叶荔枝还有二十箱，九十元一箱，两箱起订，每人限购两箱，今天十一点结束 | DRAFT_READY | 完整草稿；matched 或 create |
| 2 | 把黑叶荔枝做个尾货抢购，剩二十件，价格九十，十点半截止 | DRAFT_READY | endTime=10:30 |
| 3 | 河北水蜜桃还有五十箱，十七块三毛八，三箱起订，十二点闭市前清完 | DRAFT_READY | dealPrice=17.38 |
| 4 | 这个荔枝库存二十，九十元，发首页，发群里，限购两件 | DRAFT_READY | homepage+wecom=true |
| 5 | 黑叶荔枝做活动 | NEED_CLARIFY | missing: price/stock/endTime；**禁止 confirm** |

---

## 14. AI Harness 轻量模块划分（实现参考，非代码）

```text
community/marketing/ai/taildeal/
  TailDealAdsenseAiController          # parse + confirm 入口
  TailDealAdsenseParseService          # Intake + LLM + Draft
  TailDealAdsensePublishService        # confirm 主权编排
  TailDealAdsenseSemanticContract      # POJO
  TailDealAdsenseSemanticContractParser
  TailDealAdsenseDraftBuilder
  TailDealAdsenseDeterministicValidator
  TailDealAdsenseDraftRenderer
  TailDealAdsenseConfirmSafetyGate
  TailDealAdsenseDraftSessionStore
  CommunityGoodsMatchAdapter           # 调 NxCommunityGoodsService 搜索
  DeepSeekCompletionClient             # 或复用 OcrController 封装
resources/ai-prompts/taildeal/
  taildeal_adsense_extract.v1.md       # 全新 Prompt，不复制 jczb
```

预估：**12～14 个 Java 文件 + 1～2 个 Prompt + 本契约文档**；业务扩展 SQL **3～6 列或 1 张 adsense_ext 表**。

---

## 15. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | 2026-06-30 | 初版：Adsense 主权 + 优果团传播 + 不复用独立 tailDeal 主表 |
