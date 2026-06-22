package com.nongxinle.service;

import com.nongxinle.dto.route.DisRouteDispatchResult;
import com.nongxinle.dto.route.DisRoutePreviewRequest;
import com.nongxinle.dto.route.DisRouteReoptimizeRequest;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

import java.util.List;

public interface DisRouteDispatchService {

    /**
     * 重新生成派车方案（非订单快照同步）。
     * 职责：task/item 关联 liveOrderId、路线规划、司机/ETA/可执行性；商品展示主权在 live/history 订单表。
     */
    DisRouteDispatchResult simulate(DisRoutePreviewRequest request) throws Exception;

    /** 尊重 manualLocked 的重算（后续实现） */
    NxDisRoutePlanEntity reoptimize(DisRouteReoptimizeRequest request) throws Exception;

    NxDisRoutePlanEntity getPlan(Integer planId);

    NxDisRoutePlanEntity getPlanByRouteDate(Integer disId, String routeDate, String status, String batchCode);

    NxDisRoutePlanEntity getTodayPlan(Integer disId, String status, String batchCode);

    List<NxDistributerUserEntity> listDrivers(Integer disId);
}
