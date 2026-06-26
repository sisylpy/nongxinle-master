package com.nongxinle.route;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 司机路线编辑页 ViewModel 组装。 */
public final class DisRouteSandboxDriverRouteEditPageBuilder {

    private DisRouteSandboxDriverRouteEditPageBuilder() {
    }

    public static SandboxDriverRouteEditPageViewModel build(DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditPageViewModel vm = new SandboxDriverRouteEditPageViewModel();
        if (ctx == null) {
            return vm;
        }
        vm.setDriver(buildDriver(ctx));
        vm.setRouteStops(buildRouteStops(ctx));
        vm.setAvailableCustomers(buildAvailableCustomers(ctx));
        vm.setWarnings(new ArrayList<String>(ctx.getWarnings()));
        vm.setActions(buildActions(ctx));
        return vm;
    }

    private static SandboxDriverRouteEditDriverDto buildDriver(DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditDriverDto driver = new SandboxDriverRouteEditDriverDto();
        driver.setDriverUserId(ctx.getDriverUserId());
        driver.setDriverName(ctx.getDriverName());
        if (ctx.getPreview() != null) {
            String distance = DisRouteSandboxDisplayFormatHelper.formatDistanceText(ctx.getPreview().totalDistanceM);
            String duration = DisRouteSandboxDisplayFormatHelper.formatDurationText(ctx.getPreview().totalDurationS);
            int stopCount = ctx.getPreview().stops != null ? ctx.getPreview().stops.size() : 0;
            driver.setRouteSummary(buildRouteSummary(stopCount, distance, duration));
            driver.setCustomerStopCount(stopCount);
            driver.setTotalDistanceText(distance);
            driver.setTotalDurationText(duration);
        } else if (ctx.getBaselineStops() != null) {
            driver.setRouteSummary(ctx.getBaselineStops().size() + " 个客户");
        }
        return driver;
    }

    private static String buildRouteSummary(int stopCount, String distance, String duration) {
        StringBuilder sb = new StringBuilder();
        sb.append(stopCount).append(" 个客户");
        if (distance != null && !distance.isEmpty()) {
            sb.append(" · ").append(distance);
        }
        if (duration != null && !duration.isEmpty()) {
            sb.append(" · ").append(duration);
        }
        return sb.toString();
    }

    private static List<SandboxDriverRouteEditStopDto> buildRouteStops(DriverRouteEditBuildContext ctx) {
        List<SandboxDriverRouteEditStopDto> list = new ArrayList<SandboxDriverRouteEditStopDto>();
        List<DisRouteSandboxDriverRouteEditPreviewHelper.StopPreview> previews =
                ctx.getPreview() != null ? ctx.getPreview().stopPreviews : null;
        if (previews != null && !previews.isEmpty()) {
            for (DisRouteSandboxDriverRouteEditPreviewHelper.StopPreview preview : previews) {
                if (preview == null || preview.stop == null) {
                    continue;
                }
                list.add(toRouteStopDto(preview, ctx));
            }
            return list;
        }
        if (ctx.getBaselineStops() != null) {
            for (NxDisRouteStopEntity stop : ctx.getBaselineStops()) {
                if (stop == null) {
                    continue;
                }
                list.add(toRouteStopDto(stop, ctx));
            }
        }
        return list;
    }

    private static SandboxDriverRouteEditStopDto toRouteStopDto(
            DisRouteSandboxDriverRouteEditPreviewHelper.StopPreview preview,
            DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditStopDto dto = new SandboxDriverRouteEditStopDto();
        Integer depId = preview.departmentId;
        dto.setDepartmentId(depId);
        dto.setStopKey(DisRouteSandboxStopKeyUtils.build(depId));
        dto.setCustomerName(resolveCustomerName(preview.stop));
        dto.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(preview.stop.getShipmentTask()));
        dto.setPlannedArrivalLabel(preview.plannedArrivalLabel);
        dto.setPlannedDepartureLabel(preview.plannedDepartureLabel);
        dto.setWindowLabel(preview.windowLabel);
        applyLockState(dto, preview.stop, ctx);
        return dto;
    }

    private static SandboxDriverRouteEditStopDto toRouteStopDto(NxDisRouteStopEntity stop,
                                                               DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditStopDto dto = new SandboxDriverRouteEditStopDto();
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        dto.setDepartmentId(depId);
        dto.setStopKey(DisRouteSandboxStopKeyUtils.build(depId));
        dto.setCustomerName(resolveCustomerName(stop));
        dto.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(stop.getShipmentTask()));
        dto.setPlannedArrivalLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedArrivalLabel(
                stop, ctx.getServerNow()));
        dto.setPlannedDepartureLabel(DisRouteSandboxTodayStopScheduleHelper.resolvePlannedDepartureLabel(
                stop, ctx.getServerNow()));
        dto.setWindowLabel(DisRouteSandboxTodayStopScheduleHelper.resolveCustomerWindowLabel(
                stop, ctx.getServerNow()));
        applyLockState(dto, stop, ctx);
        return dto;
    }

    private static void applyLockState(SandboxDriverRouteEditStopDto dto,
                                       NxDisRouteStopEntity stop,
                                       DriverRouteEditBuildContext ctx) {
        String reason = ctx.resolveLockReason(stop);
        if (reason != null) {
            dto.setLocked(Boolean.TRUE);
            dto.setLockReason(reason);
        } else {
            dto.setLocked(Boolean.FALSE);
            dto.setLockReason("");
        }
    }

    private static List<SandboxDriverRouteEditAvailableCustomerDto> buildAvailableCustomers(
            DriverRouteEditBuildContext ctx) {
        List<SandboxDriverRouteEditAvailableCustomerDto> list =
                new ArrayList<SandboxDriverRouteEditAvailableCustomerDto>();
        if (ctx.getAvailableStops() == null) {
            return list;
        }
        DisRouteCustomerDriverConstraintService constraintService = ctx.getConstraintService();
        Map<Integer, DisRouteCustomerDriverConstraintDto> constraints = ctx.getConstraints();
        for (NxDisRouteStopEntity stop : ctx.getAvailableStops()) {
            if (stop == null) {
                continue;
            }
            Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
            SandboxDriverRouteEditAvailableCustomerDto dto = new SandboxDriverRouteEditAvailableCustomerDto();
            dto.setDepartmentId(depId);
            dto.setStopKey(DisRouteSandboxStopKeyUtils.build(depId));
            dto.setCustomerName(resolveCustomerName(stop));
            dto.setGoodsSummary(DisRouteSandboxDisplayFormatHelper.buildGoodsSummary(stop.getShipmentTask()));
            if (constraintService != null && constraints != null && depId != null) {
                DisRouteCustomerDriverConstraintDto constraint = constraints.get(depId);
                dto.setConstraintLabel(constraintService.buildConstraintLabel(ctx.getDriverUserId(), constraint));
            }
            list.add(dto);
        }
        return list;
    }

    private static SandboxDriverRouteEditActionsDto buildActions(DriverRouteEditBuildContext ctx) {
        SandboxDriverRouteEditActionsDto actions = new SandboxDriverRouteEditActionsDto();
        actions.setPreviewPath(DriverRouteEditPrimaryActionMaps.PREVIEW_PATH);
        actions.setConfirmPath(DriverRouteEditPrimaryActionMaps.CONFIRM_PATH);
        actions.setConfirmEnabled(Boolean.valueOf(!ctx.hasBlockingWarnings()));
        return actions;
    }

    private static String resolveCustomerName(NxDisRouteStopEntity stop) {
        if (stop.getNxDrsDepartmentName() != null && !stop.getNxDrsDepartmentName().trim().isEmpty()) {
            return stop.getNxDrsDepartmentName().trim();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        if (task != null && task.getNxDstDepName() != null && !task.getNxDstDepName().trim().isEmpty()) {
            return task.getNxDstDepName().trim();
        }
        Integer depId = DisRouteSandboxUnassignedStopHelper.resolveDepartmentId(stop);
        return depId != null ? ("客户" + depId) : "客户";
    }
}
