package com.nongxinle.route;

import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Phase 2B-2：人工插单模拟入参（内存模拟，不写 DB）。 */
@Setter
@Getter
public class ManualDispatchSimulateCommand {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer departmentId;
    private String sandboxStopKey;
    private Integer driverUserId;
    private String driverName;
    private String dispatchStage;
    private NxDisRouteStopEntity incomingStop;
    private List<NxDisRouteStopEntity> baselineStops;
    private NxDisRoutePlanEntity plan;
    private Integer manualStopSeq;
    private String requiredLatestArrivalAt;
}
