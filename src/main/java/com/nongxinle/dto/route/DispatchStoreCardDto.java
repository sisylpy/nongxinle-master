package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

/** 人工调度页 · 待分派客户卡（与 timeline stop / 未分配卡字段对齐）。 */
@Getter
@Setter
public class DispatchStoreCardDto {
    private String cardKey;
    private Integer departmentId;
    private Integer depFatherId;
    private String sandboxStopKey;
    private String customerName;
    private String goodsSummary;
    private String distanceText;
    private String durationText;
    private String legText;
    private String plannedArrivalLabel;
    private String plannedDepartureLabel;
    private String customerWindowLabel;
    private String windowRequirementLabel;
    private Boolean windowRequirementModified;
    private String serviceDurationLabel;
    private String timeLabel;
    private String arrivalStatusLabel;
    private String arrivalStatusTone;
    private String dispatchStatusLabel;
}
