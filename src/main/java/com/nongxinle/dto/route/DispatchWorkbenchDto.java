package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2b-4：调度处理台摘要（只读 GET 生成） */
@Setter
@Getter
@ToString
public class DispatchWorkbenchDto {
    private String status;
    private String statusLabel;
    private String title;
    private String subtitle;
    private String severity;
    private String primaryReason;
    private String operationHint;
    /** Phase 2b-6：时间主权摘要 */
    private String routeDate;
    private String routeDateLabel;
    private String dispatchBatch;
    private String dispatchBatchLabel;
    private String planTemporalStatus;
    private String planTemporalStatusLabel;
    private String serverNow;
    private DispatchWorkbenchMetricsDto metrics = new DispatchWorkbenchMetricsDto();
    private List<DispatchWorkbenchIssueDto> topIssues = new ArrayList<DispatchWorkbenchIssueDto>();
    private List<DispatchWorkbenchActionDto> nextActions = new ArrayList<DispatchWorkbenchActionDto>();
}
