package com.nongxinle.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxStopResolvedTimeWindow {
    private Integer resolvedEarliestDeliveryTimeS;
    private Integer resolvedLatestDeliveryTimeS;
    private Integer resolvedServiceMinutes;
    private SandboxStopTimeWindowSource windowSource;

    public boolean hasWindow() {
        return resolvedEarliestDeliveryTimeS != null || resolvedLatestDeliveryTimeS != null;
    }

    public static SandboxStopResolvedTimeWindow none() {
        SandboxStopResolvedTimeWindow window = new SandboxStopResolvedTimeWindow();
        window.setWindowSource(SandboxStopTimeWindowSource.NONE);
        return window;
    }
}
