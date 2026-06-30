package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
public class DeliveryHistoryPreferenceDto {
    private Integer depFatherId;
    private Integer preferredDriverUserId;
    private String preferredDriverName;
    private Integer deliveredTimes;
    private Date recentDeliveredAt;
    private BigDecimal avgStopSeq;
    private Integer manualLockedTimes;
    private BigDecimal confidence;
    private String reason;

    /** 全历史主导司机（不受今日 eligible 限制）。 */
    private Integer historicalTopDriverUserId;
    private String historicalTopDriverName;
    private Integer historicalTopDeliveredTimes;
    private Date historicalTopRecentDeliveredAt;
    private BigDecimal historicalTopAvgStopSeq;
    private Integer historicalTopManualLockedTimes;
    private String historicalTopNotEligibleReason;

    private Integer totalDeliveredTimesAllDrivers;
    private List<DeliveryHistoryPreferenceCandidateDto> candidateDrivers = new ArrayList<DeliveryHistoryPreferenceCandidateDto>();
}
