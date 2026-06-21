package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 2b-5：优先级 preview 结果（只读） */
@Setter
@Getter
@ToString
public class RouteDispatchPriorityPreview {
    private Integer priorityScorePreview;
    private String priorityReason;
}
