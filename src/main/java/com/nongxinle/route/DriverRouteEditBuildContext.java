package com.nongxinle.route;

import com.nongxinle.dto.route.DisRouteCustomerDriverConstraintDto;
import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.route.DisRouteSandboxDriverRouteEditPreviewHelper.DriverRoutePreviewResult;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService.ConstraintCheckResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 司机路线编辑页组装上下文。 */
public class DriverRouteEditBuildContext {

    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer driverUserId;
    private String driverName;
    private Integer operatorUserId;
    private String dispatchStage;
    private NxDisRoutePlanEntity plan;
    private NxDisDriverRouteEntity route;
    private List<NxDisRouteStopEntity> baselineStops = new ArrayList<NxDisRouteStopEntity>();
    private List<NxDisRouteStopEntity> initialBaselineStops = new ArrayList<NxDisRouteStopEntity>();
    private List<NxDisRouteStopEntity> availableStops = new ArrayList<NxDisRouteStopEntity>();
    private DriverRoutePreviewResult preview;
    private Map<Integer, DisRouteCustomerDriverConstraintDto> constraints;
    private Map<Integer, Integer> confirmedDriverByDep = new LinkedHashMap<Integer, Integer>();
    private DisRouteCustomerDriverConstraintService constraintService;
    private SandboxComputeResult compute;
    private List<String> warnings = new ArrayList<String>();
    private Date serverNow = new Date();
    private boolean blocking;
    private boolean manualDispatchMode;
    private Integer manualDispatchIncomingDepId;

    public Integer getDisId() {
        return disId;
    }

    public void setDisId(Integer disId) {
        this.disId = disId;
    }

    public String getRouteDate() {
        return routeDate;
    }

    public void setRouteDate(String routeDate) {
        this.routeDate = routeDate;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Integer getDriverUserId() {
        return driverUserId;
    }

    public void setDriverUserId(Integer driverUserId) {
        this.driverUserId = driverUserId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Integer getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Integer operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getDispatchStage() {
        return dispatchStage;
    }

    public void setDispatchStage(String dispatchStage) {
        this.dispatchStage = dispatchStage;
    }

    public NxDisRoutePlanEntity getPlan() {
        return plan;
    }

    public void setPlan(NxDisRoutePlanEntity plan) {
        this.plan = plan;
    }

    public NxDisDriverRouteEntity getRoute() {
        return route;
    }

    public void setRoute(NxDisDriverRouteEntity route) {
        this.route = route;
    }

    public List<NxDisRouteStopEntity> getBaselineStops() {
        return baselineStops;
    }

    public void setBaselineStops(List<NxDisRouteStopEntity> baselineStops) {
        this.baselineStops = baselineStops;
    }

    public List<NxDisRouteStopEntity> getInitialBaselineStops() {
        return initialBaselineStops;
    }

    public void setInitialBaselineStops(List<NxDisRouteStopEntity> initialBaselineStops) {
        this.initialBaselineStops = initialBaselineStops;
    }

    public Set<Integer> initialDepartmentIds() {
        Set<Integer> ids = new HashSet<Integer>();
        if (initialBaselineStops != null) {
            for (NxDisRouteStopEntity stop : initialBaselineStops) {
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null) {
                    ids.add(depId);
                }
            }
        }
        return ids;
    }

    public List<NxDisRouteStopEntity> getAvailableStops() {
        return availableStops;
    }

    public void setAvailableStops(List<NxDisRouteStopEntity> availableStops) {
        this.availableStops = availableStops;
    }

    public DriverRoutePreviewResult getPreview() {
        return preview;
    }

    public void setPreview(DriverRoutePreviewResult preview) {
        this.preview = preview;
    }

    public Map<Integer, DisRouteCustomerDriverConstraintDto> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<Integer, DisRouteCustomerDriverConstraintDto> constraints) {
        this.constraints = constraints;
    }

    public Map<Integer, Integer> getConfirmedDriverByDep() {
        return confirmedDriverByDep;
    }

    public void setConfirmedDriverByDep(Map<Integer, Integer> confirmedDriverByDep) {
        this.confirmedDriverByDep = confirmedDriverByDep != null
                ? confirmedDriverByDep : new LinkedHashMap<Integer, Integer>();
    }

    public DisRouteCustomerDriverConstraintService getConstraintService() {
        return constraintService;
    }

    public void setConstraintService(DisRouteCustomerDriverConstraintService constraintService) {
        this.constraintService = constraintService;
    }

    public SandboxComputeResult getCompute() {
        return compute;
    }

    public void setCompute(SandboxComputeResult compute) {
        this.compute = compute;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<String>();
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty() && !warnings.contains(warning)) {
            warnings.add(warning.trim());
        }
    }

    public void addBlockingWarning(String warning) {
        addWarning(warning);
        blocking = true;
    }

    public boolean hasBlockingWarnings() {
        return blocking;
    }

    public Date getServerNow() {
        return serverNow;
    }

    public void setServerNow(Date serverNow) {
        this.serverNow = serverNow != null ? serverNow : new Date();
    }

    public boolean isManualDispatchMode() {
        return manualDispatchMode;
    }

    public void setManualDispatchMode(boolean manualDispatchMode) {
        this.manualDispatchMode = manualDispatchMode;
    }

    public Integer getManualDispatchIncomingDepId() {
        return manualDispatchIncomingDepId;
    }

    public void setManualDispatchIncomingDepId(Integer manualDispatchIncomingDepId) {
        this.manualDispatchIncomingDepId = manualDispatchIncomingDepId;
    }

    public String resolveLockReason(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (manualDispatchMode && ManualDispatchDispatchStage.LOADING.equals(dispatchStage)) {
            return null;
        }
        if (ManualDispatchDispatchStage.LOADING.equals(dispatchStage)
                || ManualDispatchDispatchStage.EXECUTION.equals(dispatchStage)) {
            return "路线已进入装车/配送，不可调整";
        }
        return null;
    }

    public Set<Integer> baselineDepartmentIds() {
        Set<Integer> ids = new HashSet<Integer>();
        if (baselineStops != null) {
            for (NxDisRouteStopEntity stop : baselineStops) {
                Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
                if (depId != null) {
                    ids.add(depId);
                }
            }
        }
        return ids;
    }

    public void evaluateConstraintWarnings(Integer departmentId) {
        if (constraintService == null || constraints == null || departmentId == null) {
            return;
        }
        DisRouteCustomerDriverConstraintDto constraint = constraints.get(departmentId);
        ConstraintCheckResult result = constraintService.check(driverUserId, constraint);
        if (result.isBlocked()) {
            addBlockingWarning(result.getMessage() + "（" + resolveCustomerLabel(departmentId) + "）");
        } else if (result.isWarning()) {
            addWarning(result.getMessage() + "（" + resolveCustomerLabel(departmentId) + "）");
        }
    }

    private String resolveCustomerLabel(Integer departmentId) {
        if (baselineStops != null) {
            for (NxDisRouteStopEntity stop : baselineStops) {
                if (departmentId.equals(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop))) {
                    if (stop.getNxDrsDepartmentName() != null) {
                        return stop.getNxDrsDepartmentName();
                    }
                }
            }
        }
        if (availableStops != null) {
            for (NxDisRouteStopEntity stop : availableStops) {
                if (departmentId.equals(DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop))) {
                    if (stop.getNxDrsDepartmentName() != null) {
                        return stop.getNxDrsDepartmentName();
                    }
                }
            }
        }
        return "客户" + departmentId;
    }
}
