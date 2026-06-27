package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** {@link DisRouteSandboxTodayPageViewModelBuilder} 输入上下文。 */
public class SandboxTodayPageBuildContext {

    private Integer disId;
    private String batchCode;
    private Integer operatorUserId;
    private String routeDate;
    private String routeDateLabel;
    private String dispatchBatchLabel;
    private String scheduleBannerLine;
    private String feasibilityStatus;
    private int customerStopCount;
    private int totalCustomerStopCount;
    private int confirmedCustomerStopCount;
    private Date serverNow;
    private Map<String, Object> sandboxSummary;
    private DispatchWorkbenchDto workbench;
    private DriverDispatchListResponse drivers;
    private NxDisRoutePlanEntity mergedPlan;
    private String depotName;
    private List<NxDisRouteStopEntity> suggestedStops;
    private List<NxDisRouteStopEntity> unassignedStops;
    private List<NxDisRouteStopEntity> confirmedStops;
    private List<NxDisRouteStopEntity> loadingStops;
    private List<NxDisRouteStopEntity> executionStops;
    private List<InvalidDispatchStopDto> invalidStops;
    /** PR-2c：sections / map / debug 唯一主权源（建议派车 + 已确认待装车）。 */
    private List<VisibleDriverRouteSnapshot> visibleDriverRoutes;

    public Integer getDisId() {
        return disId;
    }

    public void setDisId(Integer disId) {
        this.disId = disId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Integer getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Integer operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getRouteDate() {
        return routeDate;
    }

    public void setRouteDate(String routeDate) {
        this.routeDate = routeDate;
    }

    public String getRouteDateLabel() {
        return routeDateLabel;
    }

    public void setRouteDateLabel(String routeDateLabel) {
        this.routeDateLabel = routeDateLabel;
    }

    public String getDispatchBatchLabel() {
        return dispatchBatchLabel;
    }

    public void setDispatchBatchLabel(String dispatchBatchLabel) {
        this.dispatchBatchLabel = dispatchBatchLabel;
    }

    public String getScheduleBannerLine() {
        return scheduleBannerLine;
    }

    public void setScheduleBannerLine(String scheduleBannerLine) {
        this.scheduleBannerLine = scheduleBannerLine;
    }

    public String getFeasibilityStatus() {
        return feasibilityStatus;
    }

    public void setFeasibilityStatus(String feasibilityStatus) {
        this.feasibilityStatus = feasibilityStatus;
    }

    public int getCustomerStopCount() {
        return customerStopCount;
    }

    public void setCustomerStopCount(int customerStopCount) {
        this.customerStopCount = customerStopCount;
    }

    public int getTotalCustomerStopCount() {
        return totalCustomerStopCount;
    }

    public void setTotalCustomerStopCount(int totalCustomerStopCount) {
        this.totalCustomerStopCount = totalCustomerStopCount;
    }

    public int getConfirmedCustomerStopCount() {
        return confirmedCustomerStopCount;
    }

    public void setConfirmedCustomerStopCount(int confirmedCustomerStopCount) {
        this.confirmedCustomerStopCount = confirmedCustomerStopCount;
    }

    public Date getServerNow() {
        return serverNow;
    }

    public void setServerNow(Date serverNow) {
        this.serverNow = serverNow;
    }

    public Map<String, Object> getSandboxSummary() {
        return sandboxSummary;
    }

    public void setSandboxSummary(Map<String, Object> sandboxSummary) {
        this.sandboxSummary = sandboxSummary;
    }

    public DispatchWorkbenchDto getWorkbench() {
        return workbench;
    }

    public void setWorkbench(DispatchWorkbenchDto workbench) {
        this.workbench = workbench;
    }

    public DriverDispatchListResponse getDrivers() {
        return drivers;
    }

    public void setDrivers(DriverDispatchListResponse drivers) {
        this.drivers = drivers;
    }

    public NxDisRoutePlanEntity getMergedPlan() {
        return mergedPlan;
    }

    public void setMergedPlan(NxDisRoutePlanEntity mergedPlan) {
        this.mergedPlan = mergedPlan;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public List<NxDisRouteStopEntity> getSuggestedStops() {
        return suggestedStops;
    }

    public void setSuggestedStops(List<NxDisRouteStopEntity> suggestedStops) {
        this.suggestedStops = suggestedStops;
    }

    public List<NxDisRouteStopEntity> getUnassignedStops() {
        return unassignedStops;
    }

    public void setUnassignedStops(List<NxDisRouteStopEntity> unassignedStops) {
        this.unassignedStops = unassignedStops;
    }

    public List<NxDisRouteStopEntity> getConfirmedStops() {
        return confirmedStops;
    }

    public void setConfirmedStops(List<NxDisRouteStopEntity> confirmedStops) {
        this.confirmedStops = confirmedStops;
    }

    public List<NxDisRouteStopEntity> getLoadingStops() {
        return loadingStops;
    }

    public void setLoadingStops(List<NxDisRouteStopEntity> loadingStops) {
        this.loadingStops = loadingStops;
    }

    public List<NxDisRouteStopEntity> getExecutionStops() {
        return executionStops;
    }

    public void setExecutionStops(List<NxDisRouteStopEntity> executionStops) {
        this.executionStops = executionStops;
    }

    public List<InvalidDispatchStopDto> getInvalidStops() {
        return invalidStops;
    }

    public void setInvalidStops(List<InvalidDispatchStopDto> invalidStops) {
        this.invalidStops = invalidStops;
    }

    public List<VisibleDriverRouteSnapshot> getVisibleDriverRoutes() {
        return visibleDriverRoutes;
    }

    public void setVisibleDriverRoutes(List<VisibleDriverRouteSnapshot> visibleDriverRoutes) {
        this.visibleDriverRoutes = visibleDriverRoutes;
    }
}
