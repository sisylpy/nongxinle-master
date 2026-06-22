package com.nongxinle.route;

import com.nongxinle.dto.route.SandboxComputeResult;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.DriverStopCounts;
import com.nongxinle.route.DisRouteSandboxManualDispatchPanoramaHelper.ManualDispatchPanoramaCapabilities;

/** 人工调度 simulate / edit-page 共享准备结果（避免重复 compute）。 */
public class ManualDispatchPrepareResult {
    private ManualDispatchSimulateCommand command;
    private SandboxComputeResult compute;
    private NxDisDriverRouteEntity route;
    private DriverStopCounts stopCounts;
    private ManualDispatchPanoramaCapabilities capabilities;
    private String operationHint;

    public ManualDispatchSimulateCommand getCommand() {
        return command;
    }

    public void setCommand(ManualDispatchSimulateCommand command) {
        this.command = command;
    }

    public SandboxComputeResult getCompute() {
        return compute;
    }

    public void setCompute(SandboxComputeResult compute) {
        this.compute = compute;
    }

    public NxDisDriverRouteEntity getRoute() {
        return route;
    }

    public void setRoute(NxDisDriverRouteEntity route) {
        this.route = route;
    }

    public DriverStopCounts getStopCounts() {
        return stopCounts;
    }

    public void setStopCounts(DriverStopCounts stopCounts) {
        this.stopCounts = stopCounts;
    }

    public ManualDispatchPanoramaCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ManualDispatchPanoramaCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public String getOperationHint() {
        return operationHint;
    }

    public void setOperationHint(String operationHint) {
        this.operationHint = operationHint;
    }
}
