package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/** DAO 聚合行：按 (depFatherId, driverUserId) 分组的历史送达统计。 */
@Setter
@Getter
@ToString
public class DeliveryHistoryPreferenceAggRow {
    private Integer depFatherId;
    private Integer driverUserId;
    private Integer deliveredTimes;
    private Date recentDeliveredAt;
    private Integer manualLockedTimes;
    private BigDecimal weightedDeliveredScore;
    private BigDecimal weightedAvgStopSeq;
}
