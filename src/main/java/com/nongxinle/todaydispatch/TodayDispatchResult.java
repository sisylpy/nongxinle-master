package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.SandboxComputeResult;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** todaydispatch compute 输出聚合。 */
@Getter
@Setter
public class TodayDispatchResult {

    public static final String PAGE_MODE_DISPATCH = "DISPATCH";
    public static final String PAGE_MODE_LOADING = "LOADING";
    public static final String PAGE_MODE_DELIVERY = "DELIVERY";

    private String pageMode = PAGE_MODE_DISPATCH;
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;
    private Date serverNow;
    private SandboxComputeResult compute;
    private List<DriverRoutePlan> suggestedRoutes = new ArrayList<DriverRoutePlan>();
    private List<CustomerStopPlan> unassignedStops = new ArrayList<CustomerStopPlan>();
    private List<Map<String, Object>> availableDrivers = new ArrayList<Map<String, Object>>();
    private Double depotLat;
    private Double depotLng;
    private String depotName;
    private String depotAddress;
    private String mapSubkey;
    private String mapLayerStyle;
}
