package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** 司机建议路线下的站点卡。 */
@Getter
@Setter
public class SandboxTodayStopCardDto {
    private String cardKey;
    private Integer stopSeq;
    private String customerName;
    private Integer departmentId;
    private String goodsSummary;
    private List<SandboxTodayCardItemDto> items = new ArrayList<SandboxTodayCardItemDto>();
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String customerWindowLabel;
    private String deliveryWindowLabel;
    private String windowStatusLabel;
    /** 站点卡头：要求 06:00-06:06 */
    private String windowRequirementLabel;
    /** 预计到达下方：准时 / 早到 X 分钟 */
    private String arrivalStatusLabel;
    private String arrivalStatusTone;
    private Date plannedDepartureAt;
    private String serviceDurationLabel;
    private String distanceText;
    private String durationText;
    private String timeLabel;
    private String statusLabel;
    private Map<String, Object> primaryAction;
}
