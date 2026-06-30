# 社区 POS Electron 前端接口对接文档

> 后端前缀：`api/nxcommunitypos`  
> 覆盖：堂食下单、会员查询、优惠券核销/用券、微信 Native 二维码支付、0 元结算。  
> **不包含**：打印、统计、退款、并台、换台、外卖、自提、支付宝、取消用券、取消整单、会员注册/发券/积分。

---

## 一、接口基础信息

### 1.1 BASE URL（环境变量配置）

前端通过环境变量配置 API 根地址，**不要把 BASE 写死在业务代码里**。请求时拼接：

```
${VITE_POS_API_BASE}/api/nxcommunitypos/...
```

| 环境 | 环境变量 | 值 |
|------|----------|-----|
| 开发环境 | `VITE_POS_API_BASE` | `http://localhost:8080/nongxinle_war_exploded` |
| 正式/测试环境 | `VITE_POS_API_BASE` | `https://grainservice.club:8443/nongxinle` |

**`.env.development` 示例：**

```env
VITE_POS_API_BASE=http://localhost:8080/nongxinle_war_exploded
```

**`.env.production` 示例：**

```env
VITE_POS_API_BASE=https://grainservice.club:8443/nongxinle
```

> 说明：本地 Tomcat context path 以 IDE 实际部署为准（当前联调成功为 `nongxinle_war_exploded`）；换机器或改部署名时只改环境变量即可。

**完整接口示例（开发环境）：**

```
POST http://localhost:8080/nongxinle_war_exploded/api/nxcommunitypos/order/save
```

**前端封装建议：**

```ts
const API_BASE = import.meta.env.VITE_POS_API_BASE;
const POS_PREFIX = `${API_BASE}/api/nxcommunitypos`;

// 示例
fetch(`${POS_PREFIX}/order/save`, { method: 'POST', ... });
```

### 1.2 通用约定

| 项目 | 约定 |
|------|------|
| Content-Type | `application/json`（POST 请求体） |
| 字符编码 | UTF-8 |
| 鉴权 | 第一阶段 **无 Token**，`communityId` / `operatorId` 由前端本地配置或启动页传入 |
| 金额字段 | 统一 **字符串**，两位小数，如 `"1.98"` |
| 时间字段 | 字符串；订单支付时间字段在库中不写入，前端以支付流水 `paidAt` 为准 |

### 1.3 统一响应结构

后端使用 `R` 对象：

**成功：**

```json
{
  "code": 0,
  "data": { }
}
```

**失败：**

```json
{
  "code": 500,
  "msg": "错误说明"
}
```

前端判断：`code === 0` 为成功，否则取 `msg` 提示用户。

### 1.4 全部 POS 接口一览

| 接口 | 方法 | 路径 | 第一阶段 |
|------|------|------|----------|
| 初始化 | POST | `/api/nxcommunitypos/auth/bootstrap` | ✅ |
| 菜单 | POST | `/api/nxcommunitypos/menu/list` | ✅ |
| 保存订单 | POST | `/api/nxcommunitypos/order/save` | ✅ |
| 订单详情 | GET | `/api/nxcommunitypos/order/{orderId}` | ✅ |
| 今日订单列表 | GET | `/api/nxcommunitypos/order/list/today` | ✅ |
| 会员搜索 | POST | `/api/nxcommunitypos/member/search` | ✅ |
| 会员可用券 | POST | `/api/nxcommunitypos/member/coupons` | ✅ |
| 优惠券查询 | POST | `/api/nxcommunitypos/coupon/lookup` | ✅ |
| 单独核销券 | POST | `/api/nxcommunitypos/coupon/verify` | ✅ |
| 订单用券 | POST | `/api/nxcommunitypos/order/applyCoupon` | ✅ |
| 创建支付 | POST | `/api/nxcommunitypos/payment/create` | ✅ |
| 0 元结算 | POST | `/api/nxcommunitypos/payment/settleZero` | ✅ |
| 支付状态 | GET | `/api/nxcommunitypos/payment/{paymentId}/status` | ✅ |
| 取消支付码 | POST | `/api/nxcommunitypos/payment/cancel` | ✅ |
| 微信回调 | POST | `/api/nxcommunitypos/payment/notify/wechat` | ❌ 服务端专用 |
| 支付宝回调 | POST | `/api/nxcommunitypos/payment/notify/alipay` | ❌ 暂不联调 |

---

## 二、公共数据模型

### 2.1 订单状态 `status`

| 值 | 含义 |
|----|------|
| `UNPAID` | 待支付 |
| `PAID` | 已支付 |
| `CANCELLED` | 已取消 |

### 2.2 支付状态 `paymentStatus`（订单级）

| 值 | 含义 |
|----|------|
| `UNPAID` | 未支付 |
| `PAID` | 已支付 |

### 2.3 支付流水状态 `status`（支付单级）

| 值 | 含义 |
|----|------|
| `PENDING` | 待扫码支付 |
| `SUCCESS` | 支付成功 |
| `FAILED` | 支付失败 |
| `CLOSED` | 已关闭（用户取消二维码等） |

### 2.4 桌台状态 `status`

| 值 | 含义 |
|----|------|
| `FREE` | 空闲 |
| `BUSY` | 使用中 |

### 2.5 用户券状态 `status`（lookup 返回）

| 值 | 含义 | 数据库值 |
|----|------|----------|
| `AVAILABLE` | 可用 | 0 |
| `LOCKED` | 已锁定（订单用券中） | 1 |
| `VERIFIED` | 已核销 | 3 |
| `UNAVAILABLE` | 不可用 | 其他 |

### 2.6 订单详情 `data` 结构（save / detail / applyCoupon / settleZero 共用）

```json
{
  "orderId": 8,
  "deskId": 1,
  "deskName": "1号桌",
  "status": "UNPAID",
  "paymentStatus": "UNPAID",
  "goodsSubtotal": "9.90",
  "discountSubtotal": "5.00",
  "payableTotal": "4.90",
  "remark": "少辣",
  "items": [
    {
      "lineId": 17,
      "goodsId": 3,
      "goodsName": "宫保鸡丁",
      "quantity": "1",
      "price": "9.90",
      "subtotal": "9.90",
      "remark": null
    }
  ],
  "coupons": [
    {
      "userCouponId": 5,
      "lineId": 18,
      "couponName": "满减券",
      "discountAmount": "5.00"
    }
  ]
}
```

| 字段 | 含义 | Electron 展示建议 |
|------|------|-------------------|
| orderId | 订单 ID | 内部状态、支付入参 |
| deskId / deskName | 桌号 | 顶部桌台信息 |
| status | 订单状态 | 状态标签 |
| paymentStatus | 是否已付款 | 与 status 配合展示 |
| goodsSubtotal | 商品原价合计 | 小计区「商品金额」 |
| discountSubtotal | 实际抵扣金额 | 小计区「优惠」 |
| payableTotal | 应付金额 | 大字号「应付」 |
| remark | 订单备注 | 备注行 |
| items | 商品明细 | 购物车/账单列表 |
| coupons | 已用优惠券 | 优惠明细区 |

**金额关系：** `payableTotal = goodsSubtotal - discountSubtotal`，最低为 `0.00`。

---

## 三、接口明细

### 3.1 初始化 `POST /auth/bootstrap`

**用途：** POS 启动时加载门店、操作员、桌台/包间。

**请求：**

```json
{
  "communityId": 1,
  "operatorId": 1
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| communityId | 是 | 社区/门店 ID |
| operatorId | 否 | POS 操作员工 ID |

**响应 `data`：**

```json
{
  "store": {
    "communityId": 1,
    "communityName": "测试门店",
    "commerceId": 1,
    "openTime": "09:00",
    "closeTime": "22:00"
  },
  "operator": {
    "operatorId": 1,
    "name": "张三",
    "phone": "13800000000"
  },
  "tables": [
    { "deskId": 1, "deskName": "1号桌", "chairNum": 4, "status": "FREE" }
  ],
  "rooms": [
    { "deskId": 10, "deskName": "包间A", "chairNum": 8, "status": "BUSY" }
  ]
}
```

| 展示字段 | 用途 |
|----------|------|
| store.communityName | 页头店名 |
| tables / rooms | 桌台选择页；`status=BUSY` 可标红或禁用 |
| operator.name | 右上角操作员 |

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| communityId 不能为空 | 请配置门店 ID |
| 社区不存在 | 门店不存在，请检查配置 |

---

### 3.2 菜单 `POST /menu/list`

**请求：**

```json
{
  "communityId": 1
}
```

**响应 `data`：**

```json
{
  "categories": [
    {
      "categoryId": 10,
      "categoryName": "热菜",
      "sort": 1,
      "goods": [
        {
          "goodsId": 3,
          "goodsName": "宫保鸡丁",
          "standardName": "份",
          "price": "9.90",
          "goodsType": "COMMON",
          "sellStatus": "ON_SALE",
          "soldOut": false,
          "imageUrl": "/path/to/image.jpg"
        }
      ]
    }
  ]
}
```

| 字段 | 展示 |
|------|------|
| categoryName | 左侧分类名 |
| goodsName / price / standardName | 菜品卡片 |
| imageUrl | 菜品图（需拼接图片域名） |
| goodsType | `COMMON`/`DUOPIN`/`TAOCAN`，可按需角标 |

**说明：** 已过滤优惠券类商品、已下架商品。

---

### 3.3 保存订单 `POST /order/save`

**用途：** 桌号堂食一次性下单。

**请求：**

```json
{
  "communityId": 1,
  "operatorId": 1,
  "deskId": 1,
  "remark": "少辣",
  "items": [
    { "goodsId": 3, "quantity": 1, "remark": "" }
  ]
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| communityId | 是 | 门店 ID |
| deskId | 是 | 桌台 ID |
| operatorId | 否 | 建议传，写入订单 |
| remark | 否 | 整单备注 |
| items | 是 | 至少 1 项 |
| items[].goodsId | 是 | 商品 ID |
| items[].quantity | 否 | 默认 1 |
| items[].remark | 否 | 行备注 |

**响应：** `data` 为 **订单详情结构**（见 2.6），新建后 `status=UNPAID`，`discountSubtotal=0`。

**副作用：** 桌台置为 `BUSY`（deskId 不为 -1/99 时）。

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| communityId / deskId / items 不能为空 | 请完善下单信息 |
| 社区未绑定商户 | 门店配置异常 |
| 商品不存在 / 不属于当前门店 | 菜品已失效，请刷新菜单 |

---

### 3.4 订单详情 `GET /order/{orderId}`

**请求：** 路径参数 `orderId`

**响应：** `data` 为订单详情结构。

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 订单不存在 | 订单不存在 |
| 非 POS 订单 | 订单类型错误 |

---

### 3.5 今日订单列表 `GET /order/list/today`

**用途：** 收银台/订单列表页（可选）。

**Query 参数：**

| 参数 | 必填 | 默认 | 说明 |
|------|------|------|------|
| communityId | 是 | - | 门店 ID |
| deskId | 否 | - | 按桌筛选 |
| status | 否 | ALL | `ALL` / `UNPAID` / `PAID` / `CANCELLED` |
| page | 否 | 1 | 页码 |
| limit | 否 | 20 | 每页条数 |

**示例：**

```
GET /api/nxcommunitypos/order/list/today?communityId=1&status=UNPAID&page=1&limit=20
```

**响应 `data`：**

```json
{
  "list": [
    {
      "orderId": 8,
      "deskId": 1,
      "status": "UNPAID",
      "paymentStatus": "UNPAID",
      "payableTotal": "4.90",
      "paidAt": null,
      "itemCount": 2
    }
  ],
  "page": 1,
  "limit": 20,
  "total": 1
}
```

> **注意：** `total` 当前为**本页条数**，非全量总数。列表页如需精确分页需后续后端增强。

---

### 3.6 会员搜索 `POST /member/search`

**用途：** 收银员输入手机号或会员编号，定位会员并展示可用券数量。

**请求：**

```json
{
  "communityId": 1,
  "keyword": "13800138000"
}
```

| 字段 | 说明 |
|------|------|
| communityId | 当前门店 |
| keyword | 纯数字：手机号（≥4 位模糊）或会员编号（≤6 位可精确匹配 `customerUserId`） |

**响应 `data`：**

```json
{
  "customerUserId": 1,
  "name": "微信昵称",
  "phone": "13800138000",
  "couponCount": 2
}
```

| 字段 | 说明 |
|------|------|
| customerUserId | 会员 ID，供 `/member/coupons` 与 `applyCoupon` |
| name | 微信昵称，无则 `""` |
| phone | 绑定手机号 |
| couponCount | 当前门店**可用券**数量（与 `/member/coupons` 口径一致） |

**会员范围：**

- `nx_customer_user.nx_CU_community_id = communityId`（本店注册），或
- 该用户在 `nx_customer_user_coupon` 中存在 `nx_cuc_community_id = communityId` 的记录（跨店但有本店券）

**couponCount 统计口径（仅 AVAILABLE）：**

- `nx_cuc_status = 0`
- `nx_cuc_community_id = communityId`
- 在有效期内（未生效/已过期不计入）
- 不含已锁定(1)、已核销(3)、已失效(&lt;0)、非本店券

**多条匹配：** 不返回列表；`>1` 条时直接报错，避免收银员选错人。

**无券会员：** 仍返回会员信息，`couponCount = 0`；前端提示「该会员暂无当前门店可用优惠券」。

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| communityId 不能为空 | 参数错误 |
| keyword 不能为空 | 请输入手机号或会员编号 |
| 仅支持手机号或会员编号 | 请输入数字 |
| 会员不存在 | 未找到会员 |
| 匹配到多个会员，请补全手机号 | 请补全手机号后重试 |

---

### 3.7 会员可用券 `POST /member/coupons`

**用途：** 展示某会员在当前门店可直接用于 `applyCoupon` 的券列表。

**请求：**

```json
{
  "communityId": 1,
  "customerUserId": 1
}
```

**响应 `data`：**

```json
{
  "customerUserId": 1,
  "coupons": [
    {
      "userCouponId": 5,
      "couponName": "5元代金券",
      "status": "AVAILABLE",
      "amount": "5.00",
      "expireTime": "2026-12-31"
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| userCouponId | 传给 `order/applyCoupon` |
| couponName | 券名称 |
| status | 固定 `AVAILABLE`（列表已过滤） |
| amount | 抵扣面额，与 `applyCoupon` 一致（模板 `discount_amount`） |
| expireTime | 截止日 `yyyy-MM-dd`（实例日期优先，否则模板日期） |

**排序：** 临期优先（`expireTime` 升序），同到期按 `amount` 降序。

**过滤规则（第一阶段不返回）：**

| 类型 | 条件 |
|------|------|
| 已锁定 | `nx_cuc_status = 1`（可能在其他未支付单上） |
| 已核销 | `nx_cuc_status = 3` |
| 已失效 | `nx_cuc_status < 0` |
| 非本店 | `nx_cuc_community_id != communityId` |
| 未生效/已过期 | 超出 `nx_cuc_start_date` / `nx_cuc_stop_date`（或模板日期） |

**无券：** `coupons: []`，不算失败。

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| customerUserId 不能为空 | 参数错误 |
| 会员不存在 | 会员不存在 |
| 会员与当前门店无关 | 该会员不适用于本店 |

---

### 3.8 优惠券查询 `POST /coupon/lookup`

**用途：** 扫码或输入券码后查询是否可用。

**请求：**

```json
{
  "code": "6",
  "communityId": 1
}
```

| 字段 | 说明 |
|------|------|
| code | 第一阶段仅支持 **数字 userCouponId** |
| communityId | 当前门店 |

**响应 `data`：**

```json
{
  "userCouponId": 6,
  "couponName": "5元代金券",
  "status": "AVAILABLE",
  "validStart": "2026-01-01",
  "validEnd": "2026-12-31",
  "canStandaloneVerify": true,
  "canUseInOrder": true,
  "unavailableReason": null
}
```

| 字段 | 展示 |
|------|------|
| couponName | 券名称 |
| status | 状态标签 |
| validStart / validEnd | 有效期 |
| canStandaloneVerify | 是否可单独核销按钮 |
| canUseInOrder | 是否可「用到本单」 |
| unavailableReason | 不可用原因 |

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 优惠券不存在 | 券码无效 |
| 优惠券不属于当前门店 | 该券不适用于本店 |

---

### 3.9 单独核销 `POST /coupon/verify`

**用途：** 不关联订单，直接核销一张可用券。

**请求：**

```json
{
  "userCouponId": 6,
  "communityId": 1,
  "operatorId": 1,
  "deskId": 1,
  "remark": "前台核销"
}
```

**响应 `data`：**

```json
{
  "verifyLogId": 12,
  "userCouponId": 6,
  "status": "VERIFIED",
  "verifiedAt": "2026-06-19 14:30"
}
```

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 用户券不存在 | 券不存在 |
| 优惠券不属于当前门店 | 该券不适用于本店 |
| 优惠券已过期或未生效 | 券已过期 |
| 优惠券当前不可核销 | 券状态不可核销 |

---

### 3.10 订单用券 `POST /order/applyCoupon`

**用途：** 将一张可用券绑定到当前未支付订单。

**请求：**

```json
{
  "orderId": 8,
  "userCouponId": 5,
  "operatorId": 1
}
```

**响应：** `data` 为更新后的 **订单详情结构**。

**业务效果：**

- 用户券 `0 → 1`（LOCKED）
- 订单增加优惠券子行
- 重算 `discountSubtotal` / `payableTotal`
- 每张订单第一阶段 **只能用一张券**

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 仅未支付订单可使用优惠券 | 订单已支付，无法用券 |
| 用户券不存在 | 券不存在 |
| 优惠券不可用 / 已失效 / 不属于当前门店 | 展示具体 msg |
| 订单已应用优惠券… | 本单已有优惠券 |
| 用户券关联的优惠券模板不存在 | 券数据异常，请联系管理员 |

---

### 3.11 创建支付 `POST /payment/create`

**用途：** 应付金额 > 0 时，创建微信 Native 收款二维码。

**请求：**

```json
{
  "orderId": 8,
  "payChannel": "WECHAT"
}
```

| 字段 | 说明 |
|------|------|
| payChannel | 第一阶段传 `WECHAT`；`ALIPAY` 后端有接口但 **不进入联调** |

**响应 `data`：**

```json
{
  "paymentId": 2,
  "orderId": 8,
  "payChannel": "WECHAT",
  "qrCodeUrl": "weixin://wxpay/bizpayurl?pr=xxxxx",
  "outTradeNo": "POS2026061914305212312345678ab",
  "amount": "4.90",
  "expireAt": "2026-06-19 14:45:00",
  "status": "PENDING"
}
```

| 字段 | 展示 |
|------|------|
| paymentId | 轮询/取消入参 |
| qrCodeUrl | 生成二维码图片 |
| amount | 展示收款金额 |
| expireAt | 二维码有效期（15 分钟） |
| status | 固定 `PENDING` |

**副作用：**

- 若该订单已有**未过期**且同渠道的 `PENDING` 支付单 → **直接返回原 payment**（`reused: true`），不重复调微信下单
- 若已有 `PENDING` 但**已过期**、或渠道不一致、或存在多个 `PENDING` → 自动关闭旧单后创建新支付
- 同一订单**最多只有一个有效 PENDING**，避免多码并存导致状态混乱
- 订单已 `PAID` 时**禁止**再次 create

**响应字段补充：**

| 字段 | 说明 |
|------|------|
| reused | 可选；`true` 表示返回的是已有未过期 PENDING，前端应直接展示原 `qrCodeUrl` / `paymentId` |

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 订单已支付，禁止重复创建支付 | 本单已收款完成 |
| 订单不是待支付状态 | 订单状态已变更 |
| payChannel 仅支持 WECHAT / ALIPAY | 支付方式错误 |
| 订单应付金额为0…settleZero | **改走 0 元结算按钮** |
| 创建收款码失败: 微信下单失败: … | 展示后端 msg |

---

### 3.12 0 元结算 `POST /payment/settleZero`

**用途：** `payableTotal = 0` 时完成订单，不调微信支付。

**请求：**

```json
{
  "orderId": 7,
  "operatorId": 1
}
```

**响应：** `data` 为订单详情结构，并额外包含：

```json
{
  "settleType": "ZERO",
  "status": "PAID",
  "paymentStatus": "PAID",
  "payableTotal": "0.00"
}
```

**业务效果：**

- 订单/子单 → 已支付
- 锁定券 `1 → 3`
- 写核销流水
- 释放桌台

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 订单不是待支付状态 | 订单状态已变更 |
| 订单应付金额大于0，请走正常支付流程 | 请使用微信支付 |

---

### 3.13 支付状态 `GET /payment/{paymentId}/status`

**用途：** 前端轮询扫码支付结果；**本地联调时承担微信查单补偿**（见 §5.2）。

**响应 `data`：**

```json
{
  "paymentId": 2,
  "orderId": 8,
  "payChannel": "WECHAT",
  "status": "SUCCESS",
  "paidAt": "2026-06-19 14:32:10",
  "transactionId": "4200001234567890",
  "outTradeNo": "POS2026061914305212312345678ab",
  "syncNote": "已从微信查单补偿为 SUCCESS"
}
```

| 字段 | 说明 |
|------|------|
| syncNote | 可选；本次轮询从微信查单并补偿为 SUCCESS 时出现 |
| outTradeNo | 商户订单号，便于对账 |

**查单补偿规则（仅 WECHAT）：**

| 本地 status | 行为 |
|-------------|------|
| `PENDING` + `WECHAT` | 主动向微信 `orderQuery`；若微信已支付 → 本地补偿为 `SUCCESS`，并完成订单 PAID、核销券、释放桌台 |
| `SUCCESS` | 只读本地，**不再查微信** |
| `CLOSED` / `FAILED` | 只读本地，**不查微信** |
| 非 WECHAT | 只读本地 |

**幂等保证：**

- 多次轮询 `SUCCESS` 支付单：不重复查微信、不重复核销券、不重复改订单
- 并发补偿：`markPaymentSuccess` 对已 SUCCESS 的流水直接返回；券核销跳过已 `VERIFIED` 状态

| status | 前端动作 |
|--------|----------|
| `PENDING` | 继续轮询（建议 2~3 秒一次） |
| `SUCCESS` | 停止轮询，跳转支付成功页，可再拉 `order/{orderId}` |
| `CLOSED` | 二维码已关闭，提示用户重新收款 |
| `FAILED` | 支付失败提示 |

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 支付单不存在 | 支付记录不存在 |
| 微信支付未配置 appId/mchId/API_KEY… | 联系管理员检查后端配置 |
| 微信查单通信失败 / 微信查单失败: … | 展示后端 msg，可稍后重试 |

---

### 3.14 取消支付 `POST /payment/cancel`

**用途：** 用户关闭扫码页 / 放弃当前二维码。

**请求：**

```json
{
  "paymentId": 2
}
```

**响应 `data`：**

```json
{
  "paymentId": 2,
  "status": "CLOSED"
}
```

### ⚠️ 重要语义（方案 A）

`payment/cancel` **只关闭当前支付流水**，不会：

- 取消订单（订单仍 `UNPAID`）
- 解锁优惠券（券仍 `LOCKED`）
- 移除订单优惠子行
- 重算订单金额
- 释放桌台

因此 cancel 后可直接再次调用 `payment/create` 生成新二维码（将产生新 `paymentId`）。

**保护规则：**

| 支付单 status | cancel 结果 |
|---------------|-------------|
| `PENDING` | 允许 → `CLOSED` |
| `SUCCESS` | **禁止**，返回「支付已成功，禁止取消」 |
| `CLOSED` | **禁止**，返回「仅待支付收款单可取消」 |

**失败场景：**

| msg | 前端提示 |
|-----|----------|
| 支付单不存在 | 支付记录不存在 |
| 支付已成功，禁止取消 | 本单已收款，无法关闭 |
| 仅待支付收款单可取消 | 该支付单已结束 |

---

## 四、前端业务流程

```
┌─────────────┐
│ 1. 启动应用  │
└──────┬──────┘
       ▼
 POST /auth/bootstrap          ← 读 store / tables / operator
       ▼
 POST /menu/list              ← 加载菜单
       ▼
 选择桌号 + 加购菜品
       ▼
 POST /order/save              ← 得到 orderId
       ▼
 ┌─────────────────────────────────────┐
 │ 可选：POST /member/search            │  ← 手机号查会员
 │ 可选：POST /member/coupons           │  ← 会员券列表 → applyCoupon
 │ 可选：POST /coupon/lookup            │  ← 扫码/手输 userCouponId
 │ 可选：POST /coupon/verify（单独核销）│
 │ 可选：POST /order/applyCoupon        │
 └─────────────────────────────────────┘
       ▼
 读取 payableTotal
       │
       ├─ payableTotal == "0.00"
       │       ▼
       │  POST /payment/settleZero  → 完成，展示成功页
       │
       └─ payableTotal > 0
               ▼
          POST /payment/create
               ▼
          展示 qrCodeUrl 二维码
               ▼
          轮询 GET /payment/{id}/status
               │
               ├─ SUCCESS → 支付成功页
               │
               └─ 用户关闭扫码页
                       ▼
                  POST /payment/cancel
                       ▼
                  订单/优惠保留，可再次 payment/create
```

### 4.1 推荐页面与接口映射

| 页面 | 接口 |
|------|------|
| 启动/设置 | bootstrap |
| 点餐 | menu/list |
| 桌台选择 | bootstrap.tables / rooms |
| 购物车确认 | order/save |
| 账单详情 | order/{orderId} |
| 会员查询 | member/search → member/coupons → applyCoupon |
| 优惠券弹窗 | coupon/lookup → applyCoupon 或 verify |
| 收银页 | payment/create 或 settleZero |
| 扫码页 | payment/status 轮询 + payment/cancel |
| 今日订单（可选） | order/list/today |

### 4.2 前端状态机建议

**订单页：**

```
UNPAID + 无券 → 可 applyCoupon
UNPAID + 有券 → 可支付（不可再 applyCoupon）
PAID → 只读
```

**支付页：**

```
UNPAID + 无有效 PENDING → 可 createPayment / settleZero
UNPAID + 有未过期 PENDING → create 返回 reused=true，直接展示原二维码 + 轮询
UNPAID + PENDING 已过期 → create 关闭旧单并生成新二维码
用户关闭扫码页 → cancel（仅关流水，订单/券不变）
cancel 后 → 可再次 createPayment（新 paymentId）
PAID → 禁止 createPayment / cancel
```

**payment/create 重复调用决策表：**

| 场景 | 后端行为 |
|------|----------|
| 订单已 PAID | 拒绝：「订单已支付，禁止重复创建支付」 |
| 有未过期 PENDING（同 WECHAT） | 返回原 payment，`reused: true` |
| 有已过期 PENDING | 关闭旧 PENDING → 创建新支付 |
| 用户 cancel 后（旧单 CLOSED） | 创建新支付 |
| SUCCESS 后再 create | 拒绝（订单已 PAID） |
| SUCCESS 后 cancel | 拒绝：「支付已成功，禁止取消」 |

---

## 五、特别说明

### 5.1 payment/cancel 边界

- **只做：** `PENDING → CLOSED`
- **不做：** 取消用券、取消订单、释放桌台、改订单金额
- 如需「取消用券」「取消整单」→ **非第一阶段功能**，需后续独立接口

### 5.2 微信支付回调、查单补偿与本地联调

**notify_url 配置（后端 `application.properties`）：**

```properties
pos.wechat.notify-url=https://grainservice.club:8443/nongxinle/api/nxcommunitypos/payment/notify/wechat
```

| 配置项 | 说明 |
|--------|------|
| `pos.wechat.notify-url` | POS 微信 Native 支付异步通知地址；未配置时创建支付会报错 |
| 默认值 | 与上表相同（写在配置文件与常量中，Java 业务代码不再硬编码） |

**正式环境：**

- 微信付款成功后，服务器回调 `POST {pos.wechat.notify-url}`
- 须公网 HTTPS + 微信商户平台配置 Native 支付回调 URL
- 回调与 `payment/status` 查单补偿**均可**将本地流水补偿为 SUCCESS（幂等）

**本地联调（`localhost`）：**

- 微信**无法**回调 `http://localhost:8080/...`
- 本地端到端验证支付成功，主要依赖 **`GET /payment/{paymentId}/status` 主动查微信**
- 本地仍可验证：创建二维码、重复 create 复用 PENDING、cancel、过期后重建、查单补偿 SUCCESS

**前端轮询建议：** `PENDING` 时每 2~3 秒调一次 `payment/status`，收到 `SUCCESS` 后停止。

### 5.3 支付宝

- 后端 `payChannel=ALIPAY` 有预留，**第一阶段 Electron 不接**

### 5.4 第一阶段范围外

不做：打印、统计、退款、并台、换台、外卖、自提、removeCoupon、cancelOrder

### 5.5 本地联调参考 ID

以本地库实际数据为准，常见测试：

| 类型 | 示例 |
|------|------|
| communityId | 1 |
| operatorId | 1 |
| deskId | 1 |
| goodsId | 查菜单返回 |
| userCouponId | 查 `nx_customer_user_coupon` 中 status=0 的记录 |
| customerUserId | 查 `nx_customer_user` 中 `nx_CU_wx_phone_number` |
| keyword | 会员搜索用手机号或 `customerUserId` |

---

## 六、curl 快速参考

```bash
# 与 VITE_POS_API_BASE 对应，按环境切换
BASE=http://localhost:8080/nongxinle_war_exploded/api/nxcommunitypos
# BASE=https://grainservice.club:8443/nongxinle/api/nxcommunitypos

# 初始化
curl -s -X POST "$BASE/auth/bootstrap" -H "Content-Type: application/json" \
  -d '{"communityId":1,"operatorId":1}'

# 菜单
curl -s -X POST "$BASE/menu/list" -H "Content-Type: application/json" \
  -d '{"communityId":1}'

# 下单
curl -s -X POST "$BASE/order/save" -H "Content-Type: application/json" \
  -d '{"communityId":1,"operatorId":1,"deskId":1,"items":[{"goodsId":3,"quantity":1}]}'

# 订单详情
curl -s "$BASE/order/8"

# 用券
curl -s -X POST "$BASE/order/applyCoupon" -H "Content-Type: application/json" \
  -d '{"orderId":8,"userCouponId":5,"operatorId":1}'

# 创建支付
curl -s -X POST "$BASE/payment/create" -H "Content-Type: application/json" \
  -d '{"orderId":8,"payChannel":"WECHAT"}'

# 0 元结算
curl -s -X POST "$BASE/payment/settleZero" -H "Content-Type: application/json" \
  -d '{"orderId":7,"operatorId":1}'

# 轮询支付
curl -s "$BASE/payment/2/status"

# 取消支付码
curl -s -X POST "$BASE/payment/cancel" -H "Content-Type: application/json" \
  -d '{"paymentId":2}'
```

### 6.1 支付稳定性回归用例

```bash
ORDER_ID=9   # 未支付订单
PAY_ID=4     # 已有 PENDING 或 SUCCESS 的支付单

# 1) 已有 PENDING 再 create → 应返回同一 paymentId，data.reused=true
curl -s -X POST "$BASE/payment/create" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"payChannel\":\"WECHAT\"}"

# 2) cancel 后再 create → 旧单 CLOSED，新 paymentId
curl -s -X POST "$BASE/payment/cancel" -H "Content-Type: application/json" \
  -d "{\"paymentId\":$PAY_ID}"
curl -s -X POST "$BASE/payment/create" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"payChannel\":\"WECHAT\"}"

# 3) SUCCESS 后再 create → 应失败：订单已支付，禁止重复创建支付
curl -s -X POST "$BASE/payment/create" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"payChannel\":\"WECHAT\"}"

# 4) SUCCESS 后 cancel → 应失败：支付已成功，禁止取消
curl -s -X POST "$BASE/payment/cancel" -H "Content-Type: application/json" \
  -d "{\"paymentId\":$PAY_ID}"

# 查单补偿（本地联调支付成功主要靠这个）
curl -s "$BASE/payment/$PAY_ID/status"
```

### 6.2 会员查券 + 用券 + 支付联调

```bash
BASE=http://localhost:8080/nongxinle_war_exploded/api/nxcommunitypos
COMMUNITY_ID=1
OPERATOR_ID=1
PHONE=13800138000          # 换成本地 nx_customer_user.nx_CU_wx_phone_number
CUSTOMER_USER_ID=1         # 搜会员后从响应取
USER_COUPON_ID=5           # 会员券列表中取 userCouponId
ORDER_ID=10                # 未支付、未用券的 POS 订单

# 0) 若无订单，先下单
curl -s -X POST "$BASE/order/save" -H "Content-Type: application/json" \
  -d "{\"communityId\":$COMMUNITY_ID,\"operatorId\":$OPERATOR_ID,\"deskId\":1,\"items\":[{\"goodsId\":3,\"quantity\":1}]}"

# 1) 搜会员（手机号或 ≤6 位会员编号）
curl -s -X POST "$BASE/member/search" -H "Content-Type: application/json" \
  -d "{\"communityId\":$COMMUNITY_ID,\"keyword\":\"$PHONE\"}"

# 2) 查会员可用券
curl -s -X POST "$BASE/member/coupons" -H "Content-Type: application/json" \
  -d "{\"communityId\":$COMMUNITY_ID,\"customerUserId\":$CUSTOMER_USER_ID}"

# 3) 选一张券应用到订单（沿用现有 applyCoupon，不改）
curl -s -X POST "$BASE/order/applyCoupon" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"userCouponId\":$USER_COUPON_ID,\"operatorId\":$OPERATOR_ID}"

# 4a) 若 apply 后 payableTotal > 0 → 微信支付
curl -s -X POST "$BASE/payment/create" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"payChannel\":\"WECHAT\"}"
# 轮询直到 SUCCESS
curl -s "$BASE/payment/<paymentId>/status"

# 4b) 若 apply 后 payableTotal = 0 → 0 元结算
curl -s -X POST "$BASE/payment/settleZero" -H "Content-Type: application/json" \
  -d "{\"orderId\":$ORDER_ID,\"operatorId\":$OPERATOR_ID}"
```

---

*文档版本：与本地联调通过版本一致（2026-06）*
