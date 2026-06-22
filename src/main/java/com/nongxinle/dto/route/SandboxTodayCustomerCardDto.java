package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxTodayCustomerCardDto {
    private String cardKey;
    private String customerName;
    private Integer departmentId;
    private String goodsSummary;
    private List<SandboxTodayCardItemDto> items = new ArrayList<SandboxTodayCardItemDto>();
    private String driverLabel;
    private Integer suggestedDriverUserId;
    private String suggestedDriverName;
    private String distanceText;
    private String durationText;
    private String timeLabel;
    private String statusLabel;
    private String badgeLabel;
    private SandboxTodayPrimaryActionDto primaryAction;
}
