package com.nongxinle.controller;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteDispatchWorkbenchService;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import com.nongxinle.service.DisRouteDispatchOperationPolicy;
import com.nongxinle.service.impl.DisRouteDispatchReadIntegrityHelper;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteScheduleService;
import com.nongxinle.service.DisRouteTaskTimeWindowService;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.service.DisRouteSandboxConfirmLoadingService;
import com.nongxinle.service.DisRouteSandboxConfirmService;
import com.nongxinle.service.DisRouteSandboxDeliveryExecutionService;
import com.nongxinle.service.DisRouteSandboxDriverDepartService;
import com.nongxinle.service.DisRouteSandboxDriverRouteEditService;
import com.nongxinle.service.DisRouteSandboxDriverTerminalService;
import com.nongxinle.service.DisRouteSandboxRouteLoadingGateService;
import com.nongxinle.service.DisRouteSandboxManualDispatchService;
import com.nongxinle.service.DisRouteSandboxReturnService;
import com.nongxinle.service.DisRouteSandboxTodayService;
import com.nongxinle.service.DisShipmentTaskService;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRouteDispatchLabels;
import com.nongxinle.service.DisRouteSandboxStopTimeWindowService;
import com.nongxinle.route.DisRouteTemporalHelper;
import com.nongxinle.route.RouteDispatchDateFormat;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * 配送商多司机路线派单 Phase 1 / 1.5a
 */
@RestController
@RequestMapping("api/nxdisroutedispatch")
public class NxDisRouteDispatchController {

    @Autowired
    private DisRouteDispatchService disRouteDispatchService;
    @Autowired
    private DisShipmentTaskService disShipmentTaskService;
    @Autowired
    private DisDriverDutyService disDriverDutyService;
    @Autowired
    private DisRouteScheduleService disRouteScheduleService;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;
    @Autowired
    private DisRouteDispatchOperationPolicy disRouteDispatchOperationPolicy;
    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;
    @Autowired
    private DisRouteDispatchWorkbenchService disRouteDispatchWorkbenchService;
    @Autowired
    private DisRouteSandboxTodayService disRouteSandboxTodayService;
    @Autowired
    private DisRouteSandboxConfirmService disRouteSandboxConfirmService;
    @Autowired
    private DisRouteSandboxReturnService disRouteSandboxReturnService;
    @Autowired
    private DisRouteSandboxManualDispatchService disRouteSandboxManualDispatchService;
    @Autowired
    private DisRouteSandboxDriverRouteEditService disRouteSandboxDriverRouteEditService;
    @Autowired
    private DisRouteSandboxRouteLoadingGateService disRouteSandboxRouteLoadingGateService;
    @Autowired
    private DisRouteSandboxDriverDepartService disRouteSandboxDriverDepartService;
    @Autowired
    private DisRouteSandboxDriverTerminalService disRouteSandboxDriverTerminalService;
    @Autowired
    private DisRouteSandboxConfirmLoadingService disRouteSandboxConfirmLoadingService;
    @Autowired
    private DisRouteSandboxDeliveryExecutionService disRouteSandboxDeliveryExecutionService;
    @Autowired
    private DisRouteDispatchReadIntegrityHelper disRouteDispatchReadIntegrityHelper;
    @Autowired
    private DisRouteTaskTimeWindowService disRouteTaskTimeWindowService;
    @Autowired
    private DisRouteSandboxStopTimeWindowService disRouteSandboxStopTimeWindowService;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    /** 配送商下全部司机账号（含不可派），不含 simulate 派车过滤 */
    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    @ResponseBody
    public R listDrivers(@RequestParam Integer disId) {
        try {
            return R.ok().put("data", disRouteDispatchService.listDrivers(disId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** Phase 2b-3：司机可派列表。正式合同：driverCards[] + summary（不含 drivers[]）。 */
    @RequestMapping(value = "/drivers/available", method = RequestMethod.GET)
    @ResponseBody
    public R listAvailableDrivers(@RequestParam Integer disId,
                                  @RequestParam(required = false) String routeDate,
                                  @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            return R.ok().put("data", disRouteDriverDispatchListService.listDriversForBatch(
                    disId, routeDate, batchCode));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/on", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckIn(@PathVariable Integer driverUserId, @RequestBody DriverDutyRequest request) {
        try {
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disDriverDutyService.checkIn(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/off", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckOut(@PathVariable Integer driverUserId, @RequestBody DriverDutyRequest request) {
        try {
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disDriverDutyService.checkOut(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/plan/{planId}/reoptimize", method = RequestMethod.POST)
    @ResponseBody
    public R reoptimize(@PathVariable Integer planId, @RequestBody DisRouteReoptimizeRequest request) {
        try {
            request.setPlanId(planId);
            return R.ok().put("data", disRouteDispatchService.reoptimize(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** Phase 2a：固定顺序重算配送时间窗排程（不改 stop 顺序 / 司机 / task） */
    @RequestMapping(value = "/plan/{planId}/schedule", method = RequestMethod.POST)
    @ResponseBody
    public R computeSchedule(@PathVariable Integer planId) {
        try {
            disRouteScheduleService.computeSchedule(planId);
            RouteFeasibilityResult feasibility = disRouteFeasibilityService.assess(planId);
            return R.ok().put("data", buildPlanDetailData(planId, feasibility));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** Phase 2b-1：重算可执行性与 warnings（不改分配 / stopSeq / task） */
    @RequestMapping(value = "/plan/{planId}/assess", method = RequestMethod.POST)
    @ResponseBody
    public R assessPlan(@PathVariable Integer planId) {
        try {
            RouteFeasibilityResult feasibility = disRouteFeasibilityService.assess(planId);
            return R.ok().put("data", buildPlanDetailData(planId, feasibility));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    private Map<String, Object> buildPlanDetailData(Integer planId, RouteFeasibilityResult feasibility) {
        NxDisRoutePlanEntity plan = disRouteDispatchService.getPlan(planId);
        String routeDate = plan != null
                ? (plan.getNxDrpRouteDate() != null ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate())
                : null;
        String dispatchBatch = plan != null && plan.getNxDrpDispatchBatch() != null
                && !plan.getNxDrpDispatchBatch().trim().isEmpty()
                ? plan.getNxDrpDispatchBatch().trim().toUpperCase() : "MORNING";
        return buildPlanQueryData(plan, routeDate, dispatchBatch, feasibility);
    }

    /** Phase 2b-1.1：plan/today、plan、plan/{id} 统一读模型 */
    private Map<String, Object> buildPlanQueryData(NxDisRoutePlanEntity plan,
                                                   String routeDate,
                                                   String dispatchBatch,
                                                   RouteFeasibilityResult feasibility) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        String normalizedBatch = normalizeDispatchBatchParam(dispatchBatch);
        java.util.Date serverNow = new java.util.Date();
        String effectiveRouteDate = routeDate;
        if (plan != null && (effectiveRouteDate == null || effectiveRouteDate.trim().isEmpty())) {
            effectiveRouteDate = plan.getNxDrpRouteDate() != null ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate();
        }
        data.put("routeDate", effectiveRouteDate);
        data.put("routeDateLabel", DisRouteTemporalHelper.formatRouteDateLabel(effectiveRouteDate, serverNow));
        data.put("dispatchBatch", normalizedBatch);
        data.put("dispatchBatchLabel", DisRouteDispatchLabels.label(normalizedBatch));
        data.put("serverNow", RouteDispatchDateFormat.format(serverNow));
        data.put("currentServerTime", RouteDispatchDateFormat.format(serverNow));
        if (plan == null) {
            data.put("plan", null);
            data.put("planTemporalStatus", null);
            data.put("planTemporalStatusLabel", null);
            data.put("tasks", Collections.emptyList());
            data.put("feasibilityStatus", null);
            data.put("warnings", Collections.emptyList());
            data.put("dispatchWorkbench", disRouteDispatchWorkbenchService.buildEmpty(effectiveRouteDate, normalizedBatch));
            return data;
        }
        if (plan.getNxDrpBatchStartAt() != null) {
            data.put("batchStartAt", RouteDispatchDateFormat.format(plan.getNxDrpBatchStartAt()));
        }
        if (plan.getNxDrpBatchEndAt() != null) {
            data.put("batchEndAt", RouteDispatchDateFormat.format(plan.getNxDrpBatchEndAt()));
        }
        if (plan.getNxDrpDefaultDepartAt() != null) {
            data.put("defaultDepartAt", RouteDispatchDateFormat.format(plan.getNxDrpDefaultDepartAt()));
        }
        RouteFeasibilityResult feasibilityResult;
        if (feasibility != null) {
            feasibilityResult = feasibility;
            data.put("feasibilityStatus", feasibility.getFeasibilityStatus());
            data.put("warnings", feasibility.getWarnings());
        } else {
            feasibilityResult = disRouteFeasibilityService.preview(plan.getNxDrpId());
            data.put("feasibilityStatus", feasibilityResult.getFeasibilityStatus());
            data.put("warnings", feasibilityResult.getWarnings());
        }
        List<NxDisShipmentTaskEntity> tasks = disShipmentTaskService.queryTasksByPlanId(plan.getNxDrpId());
        DisRouteDispatchIntegrityResult integrity = disRouteDispatchReadIntegrityHelper.apply(
                plan, tasks, effectiveRouteDate);
        tasks = integrity.getDisplayTasks();
        plan.setShipmentTasks(tasks);
        RouteDispatchReadModelAssembler.linkSharedTaskInstances(plan, tasks);
        disRouteDispatchOperationPolicy.enrichTasksReadModel(tasks, plan, feasibilityResult);
        disRouteDispatchOperationPolicy.enrichPlanReadModel(plan, feasibilityResult);
        data.put("plan", RouteDispatchReadModelAssembler.toPlanMap(plan));
        data.put("tasks", RouteDispatchReadModelAssembler.toTaskMaps(tasks));
        data.put("invalidStops", integrity.getInvalidStops());
        data.put("invalidStopCount", integrity.getInvalidStopCount());
        data.put("planTemporalStatus", plan.getPlanTemporalStatus());
        data.put("planTemporalStatusLabel", plan.getPlanTemporalStatusLabel());
        DispatchWorkbenchDto workbench = disRouteDispatchWorkbenchService.build(
                plan, tasks, feasibilityResult, effectiveRouteDate, normalizedBatch);
        appendInvalidStopWorkbenchHint(workbench, integrity);
        if ((tasks == null || tasks.isEmpty()) && integrity.getInvalidStopCount() > 0) {
            appendNoEligibleOrdersHint(workbench);
        }
        data.put("dispatchWorkbench", workbench);
        return data;
    }

    private void appendInvalidStopWorkbenchHint(DispatchWorkbenchDto workbench,
                                                DisRouteDispatchIntegrityResult integrity) {
        if (workbench == null || integrity == null || integrity.getInvalidStopCount() <= 0) {
            return;
        }
        List<DispatchWorkbenchIssueDto> issues = workbench.getTopIssues() != null
                ? new ArrayList<DispatchWorkbenchIssueDto>(workbench.getTopIssues())
                : new ArrayList<DispatchWorkbenchIssueDto>();
        DispatchWorkbenchIssueDto issue = new DispatchWorkbenchIssueDto();
        issue.setType("STALE_DISPATCH_STOPS");
        issue.setSeverity("WARN");
        issue.setTitle("存在无效旧派车站点");
        issue.setDescription("有 " + integrity.getInvalidStopCount()
                + " 个站点无当前有效订单，已从今日派车视图隐藏");
        issue.setSuggestion("请刷新沙盘；旧 SIMULATED 残留站点需人工清理");
        issues.add(0, issue);
        if (issues.size() > 3) {
            issues = issues.subList(0, 3);
        }
        workbench.setTopIssues(issues);
        if (workbench.getOperationHint() == null || workbench.getOperationHint().trim().isEmpty()) {
            workbench.setOperationHint(issue.getDescription() + "。" + issue.getSuggestion());
        }
    }

    private void appendNoEligibleOrdersHint(DispatchWorkbenchDto workbench) {
        if (workbench == null) {
            return;
        }
        workbench.setPrimaryReason("今日暂无有效订货订单");
        workbench.setOperationHint("当前无客户订货，动态沙盘为空；有新订单后打开页面即自动计算建议");
    }

    private String normalizeDispatchBatchParam(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return "MORNING";
        }
        return batchCode.trim().toUpperCase();
    }

    @RequestMapping(value = "/tasks/{taskId}/assign", method = RequestMethod.POST)
    @ResponseBody
    public R assignTask(@PathVariable Integer taskId, @RequestBody AssignTaskRequest request) {
        try {
            request.setTaskId(taskId);
            NxDisShipmentTaskEntity task = disShipmentTaskService.assignTask(request);
            return R.ok().put("data", task);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/tasks/{taskId}/move", method = RequestMethod.POST)
    @ResponseBody
    public R moveTask(@PathVariable Integer taskId, @RequestBody MoveTaskRequest request) {
        try {
            request.setTaskId(taskId);
            return R.ok().put("data", disShipmentTaskService.moveTask(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/tasks/{taskId}/unlock", method = RequestMethod.POST)
    @ResponseBody
    public R unlockTask(@PathVariable Integer taskId, @RequestBody UnlockTaskRequest request) {
        try {
            request.setTaskId(taskId);
            return R.ok().put("data", disShipmentTaskService.unlockTask(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** Phase 2b-5：当日送达窗口临时调整（不改 department 默认值） */
    @RequestMapping(value = "/tasks/{taskId}/time-window", method = RequestMethod.POST)
    @ResponseBody
    public R updateTaskTimeWindow(@PathVariable Integer taskId,
                                  @RequestBody TaskTimeWindowRequest request) {
        try {
            disRouteTaskTimeWindowService.updateTimeWindow(taskId, request);
            NxDisShipmentTaskEntity task = nxDisShipmentTaskDao.queryObject(taskId);
            if (task == null) {
                return R.error("配送任务不存在");
            }
            String routeDate = task.getNxDstRouteDate() != null ? task.getNxDstRouteDate() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxTodayService.buildDispatchSandboxToday(
                    task.getNxDstDistributerId(), routeDate, DisRouteDispatchBatch.MORNING,
                    request.getOperatorUserId()));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** 沙箱站点当日送达时间窗 override（confirm 前按 depFatherId；confirm 后可带 deliveryStopId） */
    @RequestMapping(value = "/dispatch/sandbox/stops/time-window", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R updateSandboxStopTimeWindow(@RequestBody SandboxStopTimeWindowRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxStopTimeWindowService.updateStopTimeWindow(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/tasks/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public R getTask(@PathVariable Integer taskId) {
        try {
            NxDisShipmentTaskEntity task = disShipmentTaskService.queryTaskDetail(taskId);
            if (task == null) {
                return R.error("配送任务不存在");
            }
            return R.ok().put("data", task);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/plan/{planId}", method = RequestMethod.GET)
    @ResponseBody
    public R getPlan(@PathVariable Integer planId) {
        try {
            NxDisRoutePlanEntity plan = disRouteDispatchService.getPlan(planId);
            if (plan == null) {
                return R.error("路线计划不存在");
            }
            String routeDate = plan.getNxDrpRouteDate() != null
                    ? plan.getNxDrpRouteDate() : plan.getNxDrpPlanDate();
            String dispatchBatch = plan.getNxDrpDispatchBatch();
            return R.ok().put("data", buildPlanQueryData(plan, routeDate, dispatchBatch, null));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/plan", method = RequestMethod.GET)
    @ResponseBody
    public R getPlanByRouteDate(@RequestParam Integer disId,
                                @RequestParam String routeDate,
                                @RequestParam(required = false, defaultValue = "SIMULATED") String status,
                                @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            NxDisRoutePlanEntity plan = disRouteDispatchService.getPlanByRouteDate(
                    disId, routeDate, status, batchCode);
            return R.ok().put("data", buildPlanQueryData(plan, routeDate, batchCode, null));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * Debug-only：全量沙盘快照（plan / workbench / executionDriverRoutes 等）。
     * 正式老板页合同：{@link #getDispatchSandboxToday} → {@code data.pageViewModel} only。
     * jcJieDan today.js 仍调用本路径但只读 pageViewModel，应迁正式路径。
     */
    @Deprecated
    @RequestMapping(value = "/sandbox/today", method = RequestMethod.GET)
    @ResponseBody
    public R getSandboxToday(@RequestParam Integer disId,
                             @RequestParam(required = false) String routeDate,
                             @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                             @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            Map<String, Object> data = disRouteSandboxTodayService.buildToday(
                    disId, effectiveRouteDate, batchCode, operatorUserId);
            return R.ok()
                    .put("deprecated", true)
                    .put("debugOnly", true)
                    .put("formalEndpoint", "/api/nxdisroutedispatch/dispatch/sandbox/today")
                    .put("message", "GET /sandbox/today 为 debug-only 全量快照；正式页面请用 GET /dispatch/sandbox/today")
                    .put("data", data);
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 今日派单页正式接口：仅返回收缩后的 pageViewModel。调试全量见 GET /sandbox/today（deprecated）。 */
    @RequestMapping(value = "/dispatch/sandbox/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchSandboxToday(@RequestParam Integer disId,
                                     @RequestParam(required = false) String routeDate,
                                     @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                     @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxTodayService.buildDispatchSandboxToday(
                    disId, effectiveRouteDate, batchCode, operatorUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    private static String formatDispatchError(Exception e) {
        if (e == null) {
            return "未知错误";
        }
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return e.getClass().getSimpleName();
    }

    /** Phase 3f：老板确认司机路线进入装车流程（路线级，保留站点确认关系） */
    @RequestMapping(value = "/driver-routes/{driverRouteId}/enter-loading", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R enterDriverRouteLoading(@PathVariable("driverRouteId") Integer driverRouteId,
                                     @RequestBody DriverRouteLoadingGateRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxRouteLoadingGateService.enterLoading(driverRouteId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3f：撤销装车，整条路线回到今日派单（路线级，保留站点确认关系） */
    @RequestMapping(value = "/driver-routes/{driverRouteId}/return-to-dispatch", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R returnDriverRouteToDispatch(@PathVariable("driverRouteId") Integer driverRouteId,
                                         @RequestBody DriverRouteLoadingGateRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxRouteLoadingGateService.returnToDispatch(driverRouteId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3a：确认沙盘客户装车/分派（无需预先 taskId） */
    @RequestMapping(value = "/sandbox/stops/confirm", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmSandboxStop(@RequestBody SandboxStopConfirmRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxConfirmService.confirmStop(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 2B-1：未分配客户 — 今日 ON_DUTY 司机全景 */
    @RequestMapping(value = "/sandbox/manual-dispatch/driver-panorama", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R listManualDispatchDriverPanorama(@RequestBody SandboxManualDispatchBaseRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxManualDispatchService.listDriverPanorama(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 未分配客户：选择司机后的沙盘模拟与影响评估 */
    @RequestMapping(value = "/sandbox/manual-dispatch/simulate", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R simulateManualDispatch(@RequestBody SandboxManualDispatchSimulateRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxManualDispatchService.simulate(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 未分配客户：人工路线编辑页 ViewModel（Phase 2B） */
    @RequestMapping(value = "/sandbox/manual-dispatch/edit-page", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R buildManualDispatchEditPage(@RequestBody SandboxManualDispatchEditPageRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxManualDispatchService.buildEditPage(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 未分配客户：确认人工排单（写入正式任务快照） */
    @RequestMapping(value = "/sandbox/manual-dispatch/confirm", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmManualDispatch(@RequestBody SandboxStopConfirmRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxManualDispatchService.confirmManualDispatch(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 司机路线人工编辑：打开编辑页 */
    @RequestMapping(value = "/sandbox/driver-route-edit/page", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R buildDriverRouteEditPage(@RequestBody SandboxDriverRouteEditBaseRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverRouteEditService.buildEditPage(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 司机路线人工编辑：试算预览 */
    @RequestMapping(value = "/sandbox/driver-route-edit/preview", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R previewDriverRouteEdit(@RequestBody SandboxDriverRouteEditPreviewRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverRouteEditService.preview(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 司机路线人工编辑：确认写库 */
    @RequestMapping(value = "/sandbox/driver-route-edit/confirm", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmDriverRouteEdit(@RequestBody SandboxDriverRouteEditConfirmRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverRouteEditService.confirm(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3c：已确认店返回动态沙盘（撤销派车确认，不删订单/配送单） */
    @RequestMapping(value = "/sandbox/stops/{deliveryStopId}/return-to-sandbox", method = RequestMethod.POST)
    @ResponseBody
    public R returnSandboxStopToSandbox(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                        @RequestBody SandboxStopReturnToSandboxRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxReturnService.returnToSandbox(deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3D：老板确认司机整车出发（driverRoute 级） */
    @RequestMapping(value = "/driver-routes/{driverRouteId}/depart", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R departDriverRoute(@PathVariable("driverRouteId") Integer driverRouteId,
                               @RequestBody DriverRouteDepartRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverDepartService.depart(driverRouteId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 装车页：按司机确认出发，返回司机配送读模型 */
    @RequestMapping(value = "/drivers/{driverUserId}/depart", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R departDriverByUserId(@PathVariable("driverUserId") Integer driverUserId,
                                  @RequestBody DriverRouteDepartRequest request) {
        try {
            if (request == null) {
                request = new DriverRouteDepartRequest();
            }
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disRouteSandboxDriverDepartService.departByDriverUserId(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 单任务装车确认：ASSIGNED → READY_TO_GO，可选写 nx_do_purchase_status=5 */
    @RequestMapping(value = "/tasks/{taskId}/confirm-loading", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmTaskLoading(@PathVariable("taskId") Integer taskId,
                                @RequestBody ConfirmLoadingRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxConfirmLoadingService.confirmTaskLoading(taskId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 整车装车确认：该 driverRoute 下全部 ASSIGNED task → READY_TO_GO */
    @RequestMapping(value = "/driver-routes/{driverRouteId}/confirm-loading-all", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmRouteLoadingAll(@PathVariable("driverRouteId") Integer driverRouteId,
                                    @RequestBody ConfirmLoadingRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxConfirmLoadingService.confirmRouteLoadingAll(
                    driverRouteId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 装车页正式接口：仅返回收缩后的 pageViewModel。调试数据见 GET /loading/today。 */
    @RequestMapping(value = "/dispatch/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchLoadingSandboxToday(@RequestParam Integer disId,
                                            @RequestParam(required = false) String routeDate,
                                            @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                            @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxTodayService.buildLoadingSandboxToday(
                    disId, effectiveRouteDate, batchCode, operatorUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 配送商今日装车读模型（老板视角） */
    @RequestMapping(value = "/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchLoadingToday(@RequestParam Integer disId,
                                     @RequestParam(required = false) String routeDate,
                                     @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                     @RequestParam(required = false) Integer driverUserId,
                                     @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxTodayService.buildLoadingToday(
                    disId, effectiveRouteDate, batchCode, driverUserId, operatorUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 配送商今日配送读模型（老板视角，已出发路线） */
    @RequestMapping(value = "/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchDeliveryToday(@RequestParam Integer disId,
                                      @RequestParam(required = false) String routeDate,
                                      @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                      @RequestParam(required = false) Integer driverUserId) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxTodayService.buildDeliveryToday(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 司机终端装车页：扁平 pageViewModel（stopList + mapOverview） */
    @RequestMapping(value = "/driver-terminal/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalLoadingToday(@RequestParam Integer disId,
                                           @RequestParam Integer driverUserId,
                                           @RequestParam(required = false) String routeDate,
                                           @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxDriverTerminalService.buildDriverLoadingToday(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** 司机终端配送页：扁平 pageViewModel（stopList + timeline + mapOverview） */
    @RequestMapping(value = "/driver-terminal/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalDeliveryToday(@RequestParam Integer disId,
                                            @RequestParam Integer driverUserId,
                                            @RequestParam(required = false) String routeDate,
                                            @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            String effectiveRouteDate = routeDate != null && !routeDate.trim().isEmpty()
                    ? routeDate.trim() : formatWhatDay(0);
            return R.ok().put("data", disRouteSandboxDriverTerminalService.buildDriverDeliveryToday(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3E：司机端单店送货完成 */
    @RequestMapping(value = "/delivery/stops/{deliveryStopId}/complete", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R completeDeliveryStop(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                  @RequestBody DeliveryStopCompleteRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDeliveryExecutionService.completeStop(deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    /** Phase 3E：司机端配送异常记录 */
    @RequestMapping(value = "/delivery/stops/{deliveryStopId}/exception", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R markDeliveryStopException(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                       @RequestBody DeliveryStopExceptionRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDeliveryExecutionService.markException(deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/plan/today", method = RequestMethod.GET)
    @ResponseBody
    public R getTodayPlan(@RequestParam Integer disId,
                          @RequestParam(required = false, defaultValue = "SIMULATED") String status,
                          @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            String routeDate = formatWhatDay(0);
            NxDisRoutePlanEntity plan = disRouteDispatchService.getTodayPlan(disId, status, batchCode);
            return R.ok().put("data", buildPlanQueryData(plan, routeDate, batchCode, null));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}
