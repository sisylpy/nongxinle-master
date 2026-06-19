# electron-platform

批发市场平台化 **订单分配** Electron 桌面端（Phase 2a 第一版）。

## 功能

三栏布局：

| 左栏 | 中栏 | 右栏 |
|------|------|------|
| 待分配客户列表 | 客户 PENDING 订单明细 | 候选配送商列表 |
| `POST /api/platform/orders/pending` | `POST /api/platform/orders/detail` | `POST /api/platform/goods/suppliers` |

操作：

- **临时分配本单** → `assign` + `switchScope=ORDER_ONLY`（不更新 default）
- **分配并设为以后默认** → `assign` + `switchScope=ORDER_AND_DEFAULT`

**本版不做：** 快照、小程序、司机调度、运营大屏。

## 前置条件

1. 后端已部署 Phase 2a Round 1 + Round 2 接口
2. 数据库已执行 `docs/sql/patches/upgrade_nx_platform_phase2a.sql`

## 配置

复制或编辑 `.env.development`：

```env
VITE_PLATFORM_API_BASE=http://localhost:8080/nongxinle_war_exploded
```

顶栏可调整 `marketId`、`operatorId`、订货日期。

## 开发

```bash
cd electron-platform
npm install
npm run dev
```

## 构建

```bash
npm run build
```

## 联调建议

1. 左栏应不出现已 ASSIGNED 的订单（如 orderId=200171）
2. 选中客户后中栏显示 `defaultRecommend`
3. 右栏 `isDefaultRecommend=1` 的配送商带「默认」标记
4. 分配成功后自动刷新 pending / detail
