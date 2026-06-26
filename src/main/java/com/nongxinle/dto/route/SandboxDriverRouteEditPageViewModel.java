package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxDriverRouteEditPageViewModel {
    private SandboxDriverRouteEditDriverDto driver;
    private List<SandboxDriverRouteEditStopDto> routeStops = new ArrayList<SandboxDriverRouteEditStopDto>();
    private List<SandboxDriverRouteEditAvailableCustomerDto> availableCustomers =
            new ArrayList<SandboxDriverRouteEditAvailableCustomerDto>();
    private List<String> warnings = new ArrayList<String>();
    private SandboxDriverRouteEditActionsDto actions;
    private SandboxTodayMapOverviewDto mapOverview;
    /** 页面标题，如人工调度时为「调整送货顺序」。 */
    private String pageTitle;
    private Boolean manualDispatchMode;
}
