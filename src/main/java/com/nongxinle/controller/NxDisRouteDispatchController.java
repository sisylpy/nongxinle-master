package com.nongxinle.controller;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteDispatchWorkbenchService;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import com.nongxinle.service.DisRouteDispatchOperationPolicy;
import com.nongxinle.service.DisRouteFeasibilityService;
import com.nongxinle.service.DisRouteScheduleService;
import com.nongxinle.service.DisRouteTaskTimeWindowService;
import com.nongxinle.service.DisShipmentTaskService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private DisRouteTaskTimeWindowService disRouteTaskTimeWindowService;

    /** 配送商下全部司机账号（含未上岗），不含 simulate 派车过滤 */
    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    @ResponseBody
    public R listDrivers(@RequestParam Integer disId) {
        try {
            return R.ok().put("data", disRouteDispatchService.listDrivers(disId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** Phase 2b-3：司机可派列表（含上岗/批次可派性/当前路线摘要，读-only） */
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

    /** Phase 1.5a：沙盘自动分派（骨架，业务后续实现） */
    @RequestMapping(value = "/simulate", method = RequestMethod.POST)
    @ResponseBody
    public R simulate(@RequestBody DisRoutePreviewRequest request) {
        try {
            return R.ok().put("data", disRouteDispatchService.simulate(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** @deprecated 旧主链已下线，请用 POST /simulate */
    @RequestMapping(value = "/preview", method = RequestMethod.POST)
    @ResponseBody
    public R preview(@RequestBody DisRoutePreviewRequest request) {
        return R.error(-1, "旧 preview 已下线，请使用 POST /api/nxdisroutedispatch/simulate");
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
        data.put("routeDate", routeDate);
        data.put("dispatchBatch", normalizedBatch);
        if (plan == null) {
            data.put("plan", null);
            data.put("tasks", Collections.emptyList());
            data.put("feasibilityStatus", null);
            data.put("warnings", Collections.emptyList());
            data.put("dispatchWorkbench", disRouteDispatchWorkbenchService.buildEmpty(routeDate, normalizedBatch));
            return data;
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
        RouteDispatchReadModelAssembler.linkSharedTaskInstances(plan, tasks);
        disRouteDispatchOperationPolicy.enrichTasksReadModel(tasks, plan, feasibilityResult);
        disRouteDispatchOperationPolicy.enrichPlanReadModel(plan, feasibilityResult);
        data.put("plan", RouteDispatchReadModelAssembler.toPlanMap(plan));
        data.put("tasks", RouteDispatchReadModelAssembler.toTaskMaps(tasks));
        data.put("dispatchWorkbench", disRouteDispatchWorkbenchService.build(
                plan, tasks, feasibilityResult, routeDate, normalizedBatch));
        return data;
    }

    private String normalizeDispatchBatchParam(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return "MORNING";
        }
        return batchCode.trim().toUpperCase();
    }

    /** @deprecated 旧主链已下线，请用 POST /tasks/{taskId}/assign */
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @ResponseBody
    public R confirm(@RequestBody Map<String, Object> body) {
        return R.error(-1, "旧 confirm 已下线，请使用 POST /api/nxdisroutedispatch/tasks/{taskId}/assign");
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
            NxDisShipmentTaskEntity task = disRouteTaskTimeWindowService.updateTimeWindow(taskId, request);
            if (task.getNxDstPlanId() == null) {
                return R.ok().put("data", RouteDispatchReadModelAssembler.toTaskMap(task));
            }
            NxDisRoutePlanEntity plan = disRouteDispatchService.getPlan(task.getNxDstPlanId());
            String routeDate = plan != null ? plan.getNxDrpRouteDate() : task.getNxDstRouteDate();
            String dispatchBatch = plan != null && plan.getNxDrpDispatchBatch() != null
                    ? plan.getNxDrpDispatchBatch() : "MORNING";
            RouteFeasibilityResult feasibility = disRouteFeasibilityService.preview(task.getNxDstPlanId());
            return R.ok().put("data", buildPlanQueryData(plan, routeDate, dispatchBatch, feasibility));
        } catch (Exception e) {
            return R.error(e.getMessage());
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

    @RequestMapping(value = "/driver/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverLoadingToday(@RequestParam Integer driverUserId) {
        try {
            return R.ok().put("data", toDriverTasksData(disRouteDispatchService.getDriverLoadingToday(driverUserId)));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/driver/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverDeliveryToday(@RequestParam Integer driverUserId) {
        try {
            return R.ok().put("data", toDriverTasksData(disRouteDispatchService.getDriverDeliveryToday(driverUserId)));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /** 显式 Map 输出，确保 Fastjson 序列化 data.driverRoute */
    private Map<String, Object> toDriverTasksData(DriverRouteTasksResponse response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (response == null) {
            return data;
        }
        data.put("routeDate", response.getRouteDate());
        data.put("planId", response.getPlanId());
        data.put("planStatus", response.getPlanStatus());
        data.put("driverRoute", response.getDriverRoute());
        data.put("tasks", response.getTasks());
        return data;
    }

    /** @deprecated 请用 /driver/loading/today 或 /driver/delivery/today */
    @RequestMapping(value = "/driver/route/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverRouteToday(@RequestParam Integer driverUserId) {
        return R.error(-1, "旧 driver/route/today 已下线，请使用 /driver/loading/today 或 /driver/delivery/today");
    }
}
