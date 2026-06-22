package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map;

/** 人工路线编辑页 — 完整 ViewModel（前端只渲染，不自行计算）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageViewModel {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private Integer operatorUserId;

    private String simulationId;
    private String simulationMode;
    private SandboxManualDispatchConstraintsDto manualConstraints;

    private SandboxManualDispatchCustomerContextDto customer;
    /** 待插入客户的人工时间约束模板（与路线店卡 manualTimeConstraint 同结构） */
    private SandboxManualDispatchManualTimeConstraintDto incomingManualTimeConstraint;
    private SandboxManualDispatchEditPageDriverDto driver;

    /** 司机当前路线（插入前） */
    private SandboxManualDispatchEditPageRouteDto baselineRoute;

    /** 可插入位置及对应模拟路线 */
    private List<SandboxManualDispatchEditPageInsertPositionDto> insertPositions =
            new ArrayList<SandboxManualDispatchEditPageInsertPositionDto>();

    private String recommendedPositionKey;
    private Integer selectedManualStopSeq;
    private SandboxManualDispatchRequiredArrivalDto requiredArrival;
    /** 已选插入位置（manualStopSeq 非空）时的模拟摘要 */
    private SandboxManualDispatchEditPageSimulationSummaryDto simulationSummary;
    private String confirmMode;
    private List<String> riskHints = new ArrayList<String>();

    private SandboxManualDispatchEditPageActionsDto actions;

    /** 是否已选择插入位置（manualStopSeq 非空） */
    private Boolean hasSelectedPosition;
    /** 已选位置一行提示 */
    private String selectedPositionHint;
    /** 单条路线时间轴（start / insert / leg / stop / end） */
    private List<Map<String, Object>> routeTimeline = new ArrayList<Map<String, Object>>();
    /** 约束抽屉副标题 */
    private String drawerSubtitle;
    /** 底部操作区提示 */
    private String bottomHint;
}
