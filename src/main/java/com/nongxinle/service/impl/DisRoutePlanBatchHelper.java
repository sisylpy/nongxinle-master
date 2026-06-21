package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dto.route.DisRouteBatchContext;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.route.DisRouteBatchDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Phase 2b-1：旧 plan batch 字段缺失时回填并持久化。
 */
@Component
public class DisRoutePlanBatchHelper {

    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;

    public NxDisRoutePlanEntity ensureBatchPersisted(Integer planId) {
        NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryObject(planId);
        if (plan == null) {
            throw new IllegalArgumentException("路线计划不存在");
        }
        if (!DisRouteBatchDefaults.needsBatchPersist(plan)) {
            return plan;
        }

        String routeDate = DisRouteBatchDefaults.resolveRouteDateFromPlan(plan);
        String batchCode = plan.getNxDrpDispatchBatch();
        DisRouteBatchContext batch = DisRouteBatchDefaults.resolveForRouteDate(routeDate, batchCode);

        NxDisRoutePlanEntity update = new NxDisRoutePlanEntity();
        update.setNxDrpId(planId);
        update.setNxDrpDispatchBatch(batch.getBatchCode());
        update.setNxDrpBatchStartAt(batch.getBatchStartAt());
        update.setNxDrpBatchEndAt(batch.getBatchEndAt());
        update.setNxDrpDefaultDepartAt(batch.getDefaultDepartAt());
        nxDisRoutePlanDao.updateBatch(update);
        return nxDisRoutePlanDao.queryObject(planId);
    }
}
