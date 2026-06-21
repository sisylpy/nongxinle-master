package com.nongxinle.service;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

/**
 * 配送任务主权服务：task/item 生命周期、bill 回填、plan 状态推导。
 * Phase 1.5a 仅骨架；simulate/reoptimize/bill 主逻辑后续实现。
 */
public interface DisShipmentTaskService {

    NxDisShipmentTaskEntity queryTaskDetail(Integer taskId);

    List<NxDisShipmentTaskEntity> queryTasksByPlanId(Integer planId);

    /**
     * 人工确认分车：ASSIGNED + manualLocked=1。
     */
    NxDisShipmentTaskEntity assignTask(AssignTaskRequest request);

    /**
     * 人工调线（含强制修改 locked task 的唯一入口）。
     */
    NxDisShipmentTaskEntity moveTask(MoveTaskRequest request);

    /**
     * 解除锁定，允许后续 reoptimize 调整 suggested/顺序。
     */
    NxDisShipmentTaskEntity unlockTask(UnlockTaskRequest request);

    /**
     * bill 打印成功后内部调用：回填 item bill/history，晋升 READY_TO_GO，不改 assigned。
     */
    void onBillPrinted(Integer billId, List<BillPrintedOrderRef> refs);

    /**
     * bill 删除回退内部调用：清 item bill/history，task 退回 ASSIGNED，恢复 open_key。
     */
    void onBillReverted(Integer billId);

    /**
     * 根据 ACTIVE task/item 推导 plan 状态；READY 仅由此方法写入，禁止前端直改。
     */
    void reconcilePlanStatus(Integer planId);
}
