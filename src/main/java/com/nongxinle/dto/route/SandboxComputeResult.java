package com.nongxinle.dto.route;

import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SandboxComputeResult {
    private String routeDate;
    private String dispatchBatch;
    private String orderVersion;
    private String sandboxVersion;
    private String dutyVersion;
    private boolean hasNewOrders;
    private boolean hasOrderChanges;
    private boolean hasLockedStops;

    private NxDisRoutePlanEntity planContext;
    private List<NxDistributerUserEntity> onDutyDrivers = new ArrayList<NxDistributerUserEntity>();

    private List<NxDisRouteStopEntity> confirmedStops = new ArrayList<NxDisRouteStopEntity>();
    /** Phase 3D+：已确认待装车门店 — 司机装车页读模型。 */
    private List<NxDisRouteStopEntity> loadingStops = new ArrayList<NxDisRouteStopEntity>();
    /** Phase 3D：已出发门店 — 不在沙箱 confirmedStops。 */
    private List<NxDisRouteStopEntity> executionStops = new ArrayList<NxDisRouteStopEntity>();
    private List<NxDisRouteStopEntity> sandboxSuggestedStops = new ArrayList<NxDisRouteStopEntity>();
    private List<NxDisRouteStopEntity> unassignedStops = new ArrayList<NxDisRouteStopEntity>();
    private List<InvalidDispatchStopDto> invalidStops = new ArrayList<InvalidDispatchStopDto>();
    private List<DisRouteOrderSnapshotDto> effectiveOrders = new ArrayList<DisRouteOrderSnapshotDto>();

    /** 合并后的展示 plan（内存，含 driverRoutes） */
    private NxDisRoutePlanEntity mergedPlan;
    private List<NxDisShipmentTaskEntity> allDisplayTasks = new ArrayList<NxDisShipmentTaskEntity>();
}
