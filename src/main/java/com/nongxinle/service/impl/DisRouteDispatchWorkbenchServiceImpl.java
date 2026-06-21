package com.nongxinle.service.impl;

import com.nongxinle.dto.route.*;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.*;
import com.nongxinle.service.DisRouteDispatchWorkbenchService;
import com.nongxinle.service.DisRouteDriverDispatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nongxinle.route.DisDriverDutyStatus.OFF_DUTY;

@Service
public class DisRouteDispatchWorkbenchServiceImpl implements DisRouteDispatchWorkbenchService {

    private static final int MAX_TOP_ISSUES = 3;

    @Autowired
    private DisRouteDriverDispatchListService disRouteDriverDispatchListService;

    @Override
    public DispatchWorkbenchDto buildEmpty(String routeDate, String dispatchBatch) {
        String batchLabel = DisRouteDispatchLabels.label(normalizeBatch(dispatchBatch));
        DispatchWorkbenchDto workbench = new DispatchWorkbenchDto();
        workbench.setStatus(DisRouteWorkbenchStatus.EMPTY);
        workbench.setStatusLabel("暂无计划");
        workbench.setTitle("今日暂无派车计划");
        workbench.setSubtitle("请先生成或选择路线计划");
        workbench.setSeverity("INFO");
        workbench.setPrimaryReason("当前日期与批次下没有路线计划");
        workbench.setOperationHint("可先执行沙盘分派生成计划");
        workbench.setTopIssues(Collections.<DispatchWorkbenchIssueDto>emptyList());
        workbench.setNextActions(buildEmptyActions());
        return workbench;
    }

    @Override
    public DispatchWorkbenchDto build(NxDisRoutePlanEntity plan,
                                        List<NxDisShipmentTaskEntity> tasks,
                                        RouteFeasibilityResult feasibility,
                                        String routeDate,
                                        String dispatchBatch) {
        String batch = normalizeBatch(dispatchBatch != null ? dispatchBatch : plan.getNxDrpDispatchBatch());
        String batchLabel = DisRouteDispatchLabels.label(batch);
        List<RouteDispatchWarning> warnings = feasibility != null && feasibility.getWarnings() != null
                ? feasibility.getWarnings() : Collections.<RouteDispatchWarning>emptyList();
        String feasibilityStatus = feasibility != null ? feasibility.getFeasibilityStatus() : null;

        DriverDispatchListResponse driverList = disRouteDriverDispatchListService.listDriversForBatch(
                plan.getNxDrpDistributerId(), routeDate, batch);

        DispatchWorkbenchMetricsDto metrics = buildMetrics(plan, warnings, driverList, tasks);
        List<IssueCandidate> issueCandidates = buildIssueCandidates(warnings, driverList, metrics);
        List<DispatchWorkbenchIssueDto> topIssues = pickTopIssues(issueCandidates);

        String status = resolveStatus(plan, feasibilityStatus, metrics, topIssues);
        String statusLabel = resolveStatusLabel(status);
        String severity = resolveSeverity(status);

        DispatchWorkbenchDto workbench = new DispatchWorkbenchDto();
        workbench.setStatus(status);
        workbench.setStatusLabel(statusLabel);
        workbench.setTitle(buildTitle(batchLabel, status, statusLabel));
        workbench.setSubtitle(buildSubtitle(status, topIssues, metrics, driverList));
        workbench.setSeverity(severity);
        workbench.setPrimaryReason(buildPrimaryReason(topIssues, status, metrics));
        workbench.setOperationHint(buildOperationHint(status, plan, topIssues, metrics));
        workbench.setMetrics(metrics);
        workbench.setTopIssues(topIssues);
        workbench.setNextActions(buildNextActions(plan, tasks, metrics, driverList, status));
        return workbench;
    }

    private DispatchWorkbenchMetricsDto buildMetrics(NxDisRoutePlanEntity plan,
                                                     List<RouteDispatchWarning> warnings,
                                                     DriverDispatchListResponse driverList,
                                                     List<NxDisShipmentTaskEntity> tasks) {
        DispatchWorkbenchMetricsDto metrics = new DispatchWorkbenchMetricsDto();
        int totalStopCount = 0;
        int driverCount = 0;
        int routeEligibleCount = 0;
        if (plan.getDriverRoutes() != null) {
            driverCount = plan.getDriverRoutes().size();
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                int stops = route.getNxDdrStopCount() != null ? route.getNxDdrStopCount() : 0;
                totalStopCount += stops;
                if (route.getNxDdrDispatchEligible() != null && route.getNxDdrDispatchEligible() == 1) {
                    routeEligibleCount++;
                }
            }
        }
        metrics.setTotalStopCount(totalStopCount);
        metrics.setDriverCount(driverCount);
        metrics.setEligibleDriverCount(driverList.getSummary() != null
                ? driverList.getSummary().getEligibleCount() : routeEligibleCount);
        metrics.setTotalLateMinutes(plan.getNxDrpTotalLateMinutes() != null ? plan.getNxDrpTotalLateMinutes() : 0);
        metrics.setTotalWaitMinutes(plan.getNxDrpTotalWaitMinutes() != null ? plan.getNxDrpTotalWaitMinutes() : 0);

        int lateStopCount = 0;
        int maxLateMinutes = 0;
        Set<Integer> lateStopIds = new HashSet<Integer>();
        int lockedConflictCount = 0;
        for (RouteDispatchWarning warning : warnings) {
            if (DisRouteWarningCode.STOP_LATE.equals(warning.getCode()) && warning.getStopId() != null) {
                if (lateStopIds.add(warning.getStopId())) {
                    lateStopCount++;
                }
                if (warning.getLateMinutes() != null && warning.getLateMinutes() > maxLateMinutes) {
                    maxLateMinutes = warning.getLateMinutes();
                }
            }
            if (DisRouteWarningCode.LOCKED_ON_INELIGIBLE_DRIVER.equals(warning.getCode())) {
                lockedConflictCount++;
            }
        }
        if (lateStopCount == 0 && plan.getDriverRoutes() != null) {
            for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
                if (route.getStops() == null) {
                    continue;
                }
                for (NxDisRouteStopEntity stop : route.getStops()) {
                    if (DisRouteStopTimeWindowStatus.LATE.equals(stop.getNxDrsTimeWindowStatus())) {
                        lateStopCount++;
                        if (stop.getNxDrsLateMinutes() != null && stop.getNxDrsLateMinutes() > maxLateMinutes) {
                            maxLateMinutes = stop.getNxDrsLateMinutes();
                        }
                    }
                }
            }
        }
        metrics.setLateStopCount(lateStopCount);
        metrics.setMaxLateMinutes(maxLateMinutes);
        metrics.setLockedConflictCount(lockedConflictCount);
        return metrics;
    }

    private List<IssueCandidate> buildIssueCandidates(List<RouteDispatchWarning> warnings,
                                                      DriverDispatchListResponse driverList,
                                                      DispatchWorkbenchMetricsDto metrics) {
        List<IssueCandidate> candidates = new ArrayList<IssueCandidate>();

        if (containsWarning(warnings, DisRouteWarningCode.NO_AVAILABLE_DRIVER)
                || (metrics.getEligibleDriverCount() != null && metrics.getEligibleDriverCount() == 0
                && metrics.getTotalStopCount() != null && metrics.getTotalStopCount() > 0)) {
            IssueCandidate issue = new IssueCandidate(10, "ERROR");
            issue.type = DisRouteWarningCode.NO_AVAILABLE_DRIVER;
            issue.title = "当前没有可派司机";
            issue.description = buildNoAvailableDriverDescription(driverList);
            issue.suggestion = "请先安排司机上岗，或切换批次";
            candidates.add(issue);
        }

        List<RouteDispatchWarning> lockedWarnings = filterWarnings(warnings, DisRouteWarningCode.LOCKED_ON_INELIGIBLE_DRIVER);
        if (!lockedWarnings.isEmpty()) {
            IssueCandidate issue = new IssueCandidate(20, "ERROR");
            issue.type = DisRouteWarningCode.LOCKED_ON_INELIGIBLE_DRIVER;
            if (lockedWarnings.size() == 1) {
                String dept = lockedWarnings.get(0).getDepartmentName();
                issue.title = (dept != null && !dept.isEmpty() ? dept : "站点") + "已锁定在不可派司机上";
            } else {
                issue.title = lockedWarnings.size() + " 个站点已锁定在不可派司机上";
            }
            issue.description = "系统不会自动换司机";
            issue.suggestion = "请先解锁或改派";
            candidates.add(issue);
        }

        List<RouteDispatchWarning> checkInLate = filterWarnings(warnings, DisRouteWarningCode.DRIVER_CHECKIN_TOO_LATE);
        if (!checkInLate.isEmpty() && !containsWarning(warnings, DisRouteWarningCode.NO_AVAILABLE_DRIVER)) {
            IssueCandidate issue = new IssueCandidate(30, "ERROR");
            issue.type = DisRouteWarningCode.DRIVER_CHECKIN_TOO_LATE;
            issue.title = "有司机上岗过晚";
            issue.description = summarizeDriverNames(checkInLate);
            issue.suggestion = "请切换批次或安排更早到岗";
            candidates.add(issue);
        }

        int lateCount = metrics.getLateStopCount() != null ? metrics.getLateStopCount() : 0;
        if (lateCount > 0) {
            IssueCandidate issue = new IssueCandidate(40, "WARN");
            issue.type = DisRouteWarningCode.STOP_LATE;
            issue.title = lateCount + " 个站点预计迟到";
            issue.description = buildStopLateDescription(warnings, metrics);
            issue.suggestion = "请调整司机、批次或路线";
            candidates.add(issue);
        }

        List<RouteDispatchWarning> overload = filterWarnings(warnings, DisRouteWarningCode.DRIVER_OVERLOAD);
        if (!overload.isEmpty()) {
            IssueCandidate issue = new IssueCandidate(50, "WARN");
            issue.type = DisRouteWarningCode.DRIVER_OVERLOAD;
            issue.title = "部分司机路线负载偏高";
            issue.description = overload.get(0).getMessage();
            issue.suggestion = overload.get(0).getSuggestion();
            candidates.add(issue);
        }

        return candidates;
    }

    private List<DispatchWorkbenchIssueDto> pickTopIssues(List<IssueCandidate> candidates) {
        Collections.sort(candidates, new Comparator<IssueCandidate>() {
            @Override
            public int compare(IssueCandidate a, IssueCandidate b) {
                return Integer.compare(a.priority, b.priority);
            }
        });
        List<DispatchWorkbenchIssueDto> top = new ArrayList<DispatchWorkbenchIssueDto>();
        for (int i = 0; i < candidates.size() && i < MAX_TOP_ISSUES; i++) {
            top.add(candidates.get(i).toDto());
        }
        return top;
    }

    private String resolveStatus(NxDisRoutePlanEntity plan,
                                 String feasibilityStatus,
                                 DispatchWorkbenchMetricsDto metrics,
                                 List<DispatchWorkbenchIssueDto> topIssues) {
        if (DisRouteFeasibilityStatus.INFEASIBLE.equals(feasibilityStatus)) {
            return DisRouteWorkbenchStatus.BLOCKED;
        }
        if (DisRouteScheduleStatus.HAS_LATE.equals(plan.getNxDrpScheduleStatus())
                || DisRouteFeasibilityStatus.HAS_LATE.equals(feasibilityStatus)
                || (metrics.getLateStopCount() != null && metrics.getLateStopCount() > 0)) {
            return DisRouteWorkbenchStatus.WARNING;
        }
        if (hasErrorIssue(topIssues)) {
            return DisRouteWorkbenchStatus.WARNING;
        }
        return DisRouteWorkbenchStatus.READY;
    }

    private String resolveStatusLabel(String status) {
        if (DisRouteWorkbenchStatus.EMPTY.equals(status)) {
            return "暂无计划";
        }
        if (DisRouteWorkbenchStatus.BLOCKED.equals(status)) {
            return "不可发车";
        }
        if (DisRouteWorkbenchStatus.WARNING.equals(status)) {
            return "有风险";
        }
        return "可发车";
    }

    private String resolveSeverity(String status) {
        if (DisRouteWorkbenchStatus.BLOCKED.equals(status)) {
            return "ERROR";
        }
        if (DisRouteWorkbenchStatus.WARNING.equals(status)) {
            return "WARN";
        }
        return "INFO";
    }

    private String buildTitle(String batchLabel, String status, String statusLabel) {
        if (DisRouteWorkbenchStatus.EMPTY.equals(status)) {
            return "今日暂无派车计划";
        }
        return batchLabel + statusLabel;
    }

    private String buildSubtitle(String status,
                                 List<DispatchWorkbenchIssueDto> topIssues,
                                 DispatchWorkbenchMetricsDto metrics,
                                 DriverDispatchListResponse driverList) {
        if (DisRouteWorkbenchStatus.READY.equals(status)) {
            return "当前批次可正常推进装车与出发";
        }
        if (DisRouteWorkbenchStatus.BLOCKED.equals(status)
                && metrics.getEligibleDriverCount() != null && metrics.getEligibleDriverCount() == 0) {
            return "当前没有可派司机，请先处理司机上岗或改派";
        }
        if (!topIssues.isEmpty() && topIssues.get(0).getTitle() != null) {
            return topIssues.get(0).getTitle();
        }
        return "请先处理当前批次异常后再发车";
    }

    private String buildPrimaryReason(List<DispatchWorkbenchIssueDto> topIssues,
                                      String status,
                                      DispatchWorkbenchMetricsDto metrics) {
        if (!topIssues.isEmpty()) {
            return topIssues.get(0).getTitle();
        }
        if (DisRouteWorkbenchStatus.READY.equals(status)) {
            return "当前批次可正常发车";
        }
        if (metrics.getEligibleDriverCount() != null && metrics.getEligibleDriverCount() == 0) {
            return "当前批次无可派司机";
        }
        return "当前批次存在待处理异常";
    }

    private String buildOperationHint(String status,
                                      NxDisRoutePlanEntity plan,
                                      List<DispatchWorkbenchIssueDto> topIssues,
                                      DispatchWorkbenchMetricsDto metrics) {
        if (plan.getOperationHint() != null && !plan.getOperationHint().trim().isEmpty()) {
            return plan.getOperationHint();
        }
        if (DisRouteWorkbenchStatus.READY.equals(status)) {
            return "可按计划推进分派、装车与出发";
        }
        StringBuilder hint = new StringBuilder("请先处理");
        if (metrics.getEligibleDriverCount() != null && metrics.getEligibleDriverCount() == 0) {
            hint.append("司机上岗");
        }
        if (metrics.getLockedConflictCount() != null && metrics.getLockedConflictCount() > 0) {
            if (hint.length() > 3) {
                hint.append("、");
            }
            hint.append("锁定站点");
        }
        if (metrics.getLateStopCount() != null && metrics.getLateStopCount() > 0) {
            if (hint.length() > 3) {
                hint.append("、");
            }
            hint.append("迟到风险");
        }
        if (hint.length() == 3) {
            hint.append("批次异常");
        }
        return hint.toString();
    }

    private List<DispatchWorkbenchActionDto> buildNextActions(NxDisRoutePlanEntity plan,
                                                              List<NxDisShipmentTaskEntity> tasks,
                                                              DispatchWorkbenchMetricsDto metrics,
                                                              DriverDispatchListResponse driverList,
                                                              String status) {
        boolean needCheckIn = hasOffDutyOrIneligibleDrivers(driverList);
        boolean hasUnlock = metrics.getLockedConflictCount() != null && metrics.getLockedConflictCount() > 0
                || hasUnlockableTask(tasks);
        boolean canReassign = hasAssignableOrMovableTask(tasks);
        boolean canGoLoading = Boolean.TRUE.equals(plan.getCanStartLoading());

        List<DispatchWorkbenchActionDto> actions = new ArrayList<DispatchWorkbenchActionDto>();
        actions.add(action(
                DisRouteWorkbenchActionCode.CHECK_IN_DRIVER,
                "司机上岗",
                needCheckIn,
                needCheckIn ? "先让可用司机上岗" : "当前已有可派司机"));
        actions.add(action(
                DisRouteWorkbenchActionCode.UNLOCK_LOCKED_STOP,
                "处理锁定站点",
                hasUnlock,
                hasUnlock ? buildUnlockHint(tasks, metrics) : "当前没有需解锁的锁定站点"));
        actions.add(action(
                DisRouteWorkbenchActionCode.REASSIGN_DRIVER,
                "重新分派",
                canReassign && (metrics.getEligibleDriverCount() == null || metrics.getEligibleDriverCount() > 0),
                canReassign
                        ? "存在可调整分派的站点"
                        : (metrics.getEligibleDriverCount() != null && metrics.getEligibleDriverCount() == 0
                        ? "当前无可派司机" : "当前没有可重新分派的站点")));
        actions.add(action(
                DisRouteWorkbenchActionCode.GO_LOADING,
                "去装车",
                canGoLoading,
                canGoLoading ? "存在可装车站点" : resolveLoadingDisabledHint(plan, status)));
        return actions;
    }

    private List<DispatchWorkbenchActionDto> buildEmptyActions() {
        List<DispatchWorkbenchActionDto> actions = new ArrayList<DispatchWorkbenchActionDto>();
        actions.add(action(DisRouteWorkbenchActionCode.CHECK_IN_DRIVER, "司机上岗", false, "请先生成计划"));
        actions.add(action(DisRouteWorkbenchActionCode.UNLOCK_LOCKED_STOP, "处理锁定站点", false, "暂无计划"));
        actions.add(action(DisRouteWorkbenchActionCode.REASSIGN_DRIVER, "重新分派", false, "暂无计划"));
        actions.add(action(DisRouteWorkbenchActionCode.GO_LOADING, "去装车", false, "暂无计划"));
        return actions;
    }

    private DispatchWorkbenchActionDto action(String code, String label, boolean enabled, String hint) {
        DispatchWorkbenchActionDto dto = new DispatchWorkbenchActionDto();
        dto.setAction(code);
        dto.setLabel(label);
        dto.setEnabled(enabled);
        dto.setHint(hint);
        return dto;
    }

    private String buildNoAvailableDriverDescription(DriverDispatchListResponse driverList) {
        if (driverList.getDrivers() == null || driverList.getDrivers().isEmpty()) {
            return "当前配送商没有司机账号";
        }
        List<String> parts = new ArrayList<String>();
        for (DriverDispatchCandidateDto driver : driverList.getDrivers()) {
            if (Boolean.TRUE.equals(driver.getBatchEligible())) {
                continue;
            }
            String name = driver.getDriverName() != null ? driver.getDriverName() : String.valueOf(driver.getDriverUserId());
            if (OFF_DUTY.equals(driver.getDutyStatus())) {
                parts.add(name + "未上岗");
            } else if (DisRouteBatchEligibility.INELIGIBLE_CHECKIN_TOO_LATE.equals(driver.getIneligibleReason())) {
                parts.add(name + "上岗过晚");
            } else {
                parts.add(name + "不可派");
            }
        }
        if (parts.isEmpty()) {
            return "本批次无可派司机";
        }
        return joinParts(parts);
    }

    private String buildStopLateDescription(List<RouteDispatchWarning> warnings,
                                            DispatchWorkbenchMetricsDto metrics) {
        RouteDispatchWarning worst = null;
        for (RouteDispatchWarning warning : warnings) {
            if (!DisRouteWarningCode.STOP_LATE.equals(warning.getCode())) {
                continue;
            }
            if (worst == null || (warning.getLateMinutes() != null
                    && (worst.getLateMinutes() == null || warning.getLateMinutes() > worst.getLateMinutes()))) {
                worst = warning;
            }
        }
        if (worst != null && worst.getDepartmentName() != null && worst.getLateMinutes() != null) {
            return "最严重：" + worst.getDepartmentName() + "迟到 " + worst.getLateMinutes() + " 分钟";
        }
        if (metrics.getMaxLateMinutes() != null && metrics.getMaxLateMinutes() > 0) {
            return "最大预计迟到 " + metrics.getMaxLateMinutes() + " 分钟";
        }
        return "部分站点预计迟到";
    }

    private String buildUnlockHint(List<NxDisShipmentTaskEntity> tasks, DispatchWorkbenchMetricsDto metrics) {
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (Boolean.TRUE.equals(task.getCanUnlock())) {
                    String dept = task.getNxDstDepName();
                    return (dept != null && !dept.isEmpty() ? dept : "站点") + "已锁定，需先解锁或改派";
                }
            }
        }
        if (metrics.getLockedConflictCount() != null && metrics.getLockedConflictCount() > 0) {
            return "存在 " + metrics.getLockedConflictCount() + " 个锁定冲突站点";
        }
        return "请先解锁或改派锁定站点";
    }

    private String resolveLoadingDisabledHint(NxDisRoutePlanEntity plan, String status) {
        if (plan.getLoadingBlockedReason() != null && !plan.getLoadingBlockedReason().trim().isEmpty()) {
            return plan.getLoadingBlockedReason();
        }
        if (DisRouteWorkbenchStatus.BLOCKED.equals(status)) {
            return "当前批次不可执行，不能装车";
        }
        return "当前暂不可装车";
    }

    private boolean hasOffDutyOrIneligibleDrivers(DriverDispatchListResponse driverList) {
        if (driverList.getSummary() == null) {
            return true;
        }
        Integer eligible = driverList.getSummary().getEligibleCount();
        return eligible == null || eligible == 0
                || (driverList.getSummary().getOnDutyCount() != null
                && driverList.getSummary().getTotalDriverCount() != null
                && driverList.getSummary().getOnDutyCount() < driverList.getSummary().getTotalDriverCount());
    }

    private boolean hasUnlockableTask(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null) {
            return false;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (Boolean.TRUE.equals(task.getCanUnlock())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAssignableOrMovableTask(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null) {
            return false;
        }
        for (NxDisShipmentTaskEntity task : tasks) {
            if (Boolean.TRUE.equals(task.getCanAssign()) || Boolean.TRUE.equals(task.getCanMove())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasErrorIssue(List<DispatchWorkbenchIssueDto> topIssues) {
        for (DispatchWorkbenchIssueDto issue : topIssues) {
            if ("ERROR".equals(issue.getSeverity())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsWarning(List<RouteDispatchWarning> warnings, String code) {
        for (RouteDispatchWarning warning : warnings) {
            if (code.equals(warning.getCode())) {
                return true;
            }
        }
        return false;
    }

    private List<RouteDispatchWarning> filterWarnings(List<RouteDispatchWarning> warnings, String code) {
        List<RouteDispatchWarning> result = new ArrayList<RouteDispatchWarning>();
        for (RouteDispatchWarning warning : warnings) {
            if (code.equals(warning.getCode())) {
                result.add(warning);
            }
        }
        return result;
    }

    private String summarizeDriverNames(List<RouteDispatchWarning> warnings) {
        List<String> names = new ArrayList<String>();
        for (RouteDispatchWarning warning : warnings) {
            if (warning.getDriverName() != null && !warning.getDriverName().trim().isEmpty()) {
                names.add(warning.getDriverName().trim());
            }
        }
        if (names.isEmpty()) {
            return "部分司机上岗时间晚于本批次要求";
        }
        return joinParts(names) + " 上岗过晚";
    }

    private String joinParts(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append("，");
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }

    private String normalizeBatch(String batchCode) {
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return DisRouteDispatchBatch.MORNING;
        }
        return batchCode.trim().toUpperCase();
    }

    private static final class IssueCandidate {
        private final int priority;
        private final String severity;
        private String type;
        private String title;
        private String description;
        private String suggestion;

        private IssueCandidate(int priority, String severity) {
            this.priority = priority;
            this.severity = severity;
        }

        private DispatchWorkbenchIssueDto toDto() {
            DispatchWorkbenchIssueDto dto = new DispatchWorkbenchIssueDto();
            dto.setType(type);
            dto.setSeverity(severity);
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setSuggestion(suggestion);
            return dto;
        }
    }
}
