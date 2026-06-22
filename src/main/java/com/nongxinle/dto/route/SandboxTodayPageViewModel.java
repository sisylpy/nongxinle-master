package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** GET /dispatch/sandbox/today 老板端页面展示模型（前端主链只读此结构）。 */
@Getter
@Setter
public class SandboxTodayPageViewModel {
    private String topMetricsScope;
    private SandboxTodayPageHeaderDto pageHeader;
    private SandboxTodayTopMetricsDto topMetrics;
    private List<SandboxTodaySectionDto> sections = new ArrayList<SandboxTodaySectionDto>();
    private List<SandboxTodayAvailableDriverDto> availableDrivers = new ArrayList<SandboxTodayAvailableDriverDto>();
}
