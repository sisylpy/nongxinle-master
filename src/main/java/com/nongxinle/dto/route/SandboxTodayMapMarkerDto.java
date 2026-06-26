package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SandboxTodayMapMarkerDto {
    private String markerKey;
    /** DEPOT / CUSTOMER / UNASSIGNED */
    private String markerType;
    private Double lat;
    private Double lng;
    private String customerName;
    private Integer stopSeq;
    private String driverName;
    private Integer driverUserId;
    private Integer departmentId;
    private Integer depFatherId;
    private String colorKey;
    private String color;
    private String assignmentLabel;
    /** 地图气泡主标题，如客户名。 */
    private String displayTitle;
    /** 地图气泡副标题，如「21:34 到达」或「未分配」。 */
    private String displaySubtitle;
    /** 地图气泡副标题，如「21:34 到达」。 */
    private String arrivalLabel;
    /** 地图 marker 序号角标：1、2…；未分配为 ?。 */
    private String badgeText;
}
