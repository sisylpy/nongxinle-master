package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/** eligible 候选司机的历史统计（debug / 解释用）。 */
@Setter
@Getter
@ToString
public class DeliveryHistoryPreferenceCandidateDto {
    private Integer driverUserId;
    private String driverName;
    private Integer deliveredTimes;
    private Date recentDeliveredAt;
    private Integer manualLockedTimes;
    private BigDecimal weightedDeliveredScore;
    private BigDecimal weightedAvgStopSeq;
}
