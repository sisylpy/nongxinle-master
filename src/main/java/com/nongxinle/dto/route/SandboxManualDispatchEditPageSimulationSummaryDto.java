package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** 人工路线编辑页 — 选中插入位置后的模拟摘要（司机卡下方一行）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageSimulationSummaryDto {
    /** 整行摘要，优先于 totalDistanceText + totalDurationText */
    private String routeSummary;
    private String totalDistanceText;
    private String totalDurationText;
    /** 相对基线变化，如「较原路线 +1.2 公里 · +6 分钟」 */
    private String deltaLine;
    private List<String> riskHints = new ArrayList<String>();
}
