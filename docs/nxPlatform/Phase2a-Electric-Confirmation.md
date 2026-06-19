# Phase 2a Electron 桌面端验收（electron-platform）

> 应用目录：`electron-platform/`（与社区 POS `electron-pos` 隔离）

## 三栏布局

| 左栏 | 中栏 | 右栏 |
|------|------|------|
| 待分配客户 | 客户订单明细 | 候选配送商 |
| `POST /api/platform/orders/pending` | `POST /api/platform/orders/detail` | `POST /api/platform/goods/suppliers` |

## 分配操作文案

| 按钮 | `switchScope` | 含义 |
|------|---------------|------|
| **临时分配本单** | `ORDER_ONLY` | 只改本单 + 写 switch_log，**不**更新 default |
| **分配并设为以后默认** | `ORDER_AND_DEFAULT` | 本单分配 + upsert `nx_department_nx_goods_default` |

## 交互主权（已验收）

### PENDING 行（可操作）

- 默认进入客户详情时选中**第一条 PENDING** 行
- 中栏排序：**PENDING 在前 → ASSIGNED 在后**（同状态按 orderId）
- 右栏加载 suppliers，可点选配送商
- 底部显示两个分配按钮（仅 `canAssign` 时可用）

### ASSIGNED 行（只读）

- 灰色弱化展示，不作为默认操作对象
- **不显示**分配按钮
- 右栏只读展示：配送商 ID、配送商商品 ID、成交价
- 底部状态栏：`已分配 · 配送商 X · 商品 Y · ¥价格`
- 前端 `handleAssign` 守卫 `isLinePending`；后端 assign 亦校验 PENDING

### 禁止项（Phase 2a 不做）

- 已分配订单改派 / 重新分配
- 快照、司机调度、运营大屏、小程序接入、供应商评分

## 联调配置

`.env.development`：

```env
VITE_PLATFORM_API_BASE=http://localhost:8080/nongxinle_war_exploded
```

顶栏：`marketId`、`operatorId`、订货日期。

## 启动

```bash
cd electron-platform
npm install
npm run dev
```

## 验收场景记录

| 场景 | 预期 | 状态 |
|------|------|------|
| 左栏 pending 客户数 | 仅含仍有 PENDING 行的客户 | ✅ |
| 同客户 ASSIGNED + PENDING | 默认选中 PENDING（如 200173） | ✅ |
| 点击 ASSIGNED（如 200172） | 右栏只读，无分配按钮 | ✅ |
| 临时分配本单 | default 表不变 | ✅ 见后端确认 §4 |
| 分配并设为以后默认 | default upsert + switch_log | ✅ orderId=200171 |
| suppliers 默认标记 | `isDefaultRecommend=1` 精确到 disGoodsId | ✅ |
| 0.1 无效价 | 不出现在 suppliers，assign 拒绝 | ✅ |

## 相关文档

- [Phase2a-Backend-Confirmation.md](./Phase2a-Backend-Confirmation.md) — 后端主链、表主权、接口
- [Phase2a-Round1-验收指南.md](./Phase2a-Round1-验收指南.md)
- [Phase2a-Round2-验收指南.md](./Phase2a-Round2-验收指南.md)
