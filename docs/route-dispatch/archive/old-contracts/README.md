# 派单旧合同归档

> **已废弃，不可作为当前正式合同。**  
> 当前正式口径见：
> - [`Route-Dispatch-Backend-Handoff-20260622.md`](../../Route-Dispatch-Backend-Handoff-20260622.md)
> - [`Route-Dispatch-Future-Plugin-Boundary-20260622.md`](../../Route-Dispatch-Future-Plugin-Boundary-20260622.md)
> - [`Route-Dispatch-Manual-Dispatch-Phase2B-Contract.md`](../../../nxPlatform/Route-Dispatch-Manual-Dispatch-Phase2B-Contract.md)（人工调度补充，以 Handoff 为准）

## 归档文件

| 文件 | 废弃原因 |
|------|----------|
| `Phase1.5a-Route-Dispatch-API-ARCHIVED.md` | 仍描述「simulate 写库为主链」「POST /preview」等 Phase 1.5 口径 |
| `Route-Dispatch-Sandbox-Today-Phase3a1c-Frontend-ARCHIVED.md` | 前端从 `dispatchWorkbench` / 全量 sandbox 组装页面，已被 `pageViewModel` 取代 |

## 已删除 HTTP 入口（2026-06-22 起勿再引用）

- `POST /api/nxdisroutedispatch/preview`
- `POST /api/nxdisroutedispatch/confirm`（顶层旧 confirm）
- `POST /api/nxdisroutedispatch/simulate`（2026-06-23；Service 层 simulate 保留）
- `GET /api/nxdisroutedispatch/driver/loading/today`
- `GET /api/nxdisroutedispatch/driver/delivery/today`
- `GET /api/nxdisroutedispatch/driver/route/today`
