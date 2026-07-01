package com.nongxinle.dispatch.core.view;

import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 派单三页统一 pageViewModel 契约。
 * Nx / 商城 / 平台 adapter 均输出此结构，前端不再适配多套字段。
 */
@Getter
@Setter
public class DispatchPageViewModel {

    private DispatchPageMode pageMode;
    private DispatchTenantRef tenant;
    private String routeDate;
    private String topMetricsScope;
    private DispatchPageHeader pageHeader;
    private DispatchTopMetrics topMetrics;
    private List<DispatchPageSection> sections = new ArrayList<DispatchPageSection>();
    private List<DispatchAvailableDriver> availableDrivers = new ArrayList<DispatchAvailableDriver>();
    private DispatchMapOverview mapOverview;
    /** 司机装车页底部发车条（driver-terminal loading）。 */
    private Boolean showDepartAction;
    private Boolean departActionEnabled;
    private String departActionLabel;
    private Integer driverRouteId;

    @Getter
    @Setter
    public static class DispatchPageHeader {
        private String title;
        private String subtitle;
        private String routeDateLabel;
        private String depotName;
        private String depotAddress;
        private DispatchPageHeaderProgress progress;
    }

    @Getter
    @Setter
    public static class DispatchPageHeaderProgress {
        private String mainLine;
        private String highlightText;
    }

    @Getter
    @Setter
    public static class DispatchTopMetrics {
        private Integer unassignedStopCount;
        private Integer assignedStopCount;
        private Integer activeRouteCount;
        private Integer availableDriverCount;
        private Map<String, Object> extra = new LinkedHashMap<String, Object>();
    }

    @Getter
    @Setter
    public static class DispatchPageSection {
        private String sectionKey;
        private String title;
        private String description;
        private List<DispatchSectionCard> cards = new ArrayList<DispatchSectionCard>();
    }

    @Getter
    @Setter
    public static class DispatchAvailableDriver {
        private Integer driverUserId;
        private String driverName;
        private String driverPhone;
        private String dutyStatus;
        private String dispatchPhaseLabel;
        private String statusLabel;
        private String badgeLabel;
        private boolean selectable;
        private DispatchPrimaryAction routeEditAction;
    }
}
