package com.nongxinle.service;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisShipmentTaskEntity;

import java.util.List;

/**
 * 配送任务主权服务：task/item 查询、bill 回填、plan 状态推导。
 */
public interface DisShipmentTaskService {

    NxDisShipmentTaskEntity queryTaskDetail(Integer taskId);

    List<NxDisShipmentTaskEntity> queryTasksByPlanId(Integer planId);

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
