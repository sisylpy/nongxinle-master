package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** 可插入位置 + 该位置下的模拟路线。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageInsertPositionDto {
    private String insertPositionKey;
    private Integer manualStopSeq;
    private String label;
    /** 如「插到二号店前面」；无锚点店时为 null */
    private String anchorLabel;
    private Boolean recommended;
    private Boolean manualSeqLocked;
    private SandboxManualDispatchEditPageRouteDto simulatedRoute;
    private SandboxManualDispatchIncomingStopPreviewDto incomingStop;
    private SandboxManualDispatchRequiredArrivalDto requiredArrival;
    private String confirmMode;
    private List<String> riskHints = new ArrayList<String>();
}
