package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2b-3：GET /drivers/available 响应体 */
@Setter
@Getter
@ToString
public class DriverDispatchListResponse {
    private String routeDate;
    private String dispatchBatch;
    private String dispatchBatchLabel;
    private String latestAllowedCheckInAt;
    private DriverDispatchListSummaryDto summary = new DriverDispatchListSummaryDto();
    private List<DriverDispatchCandidateDto> drivers = new ArrayList<DriverDispatchCandidateDto>();
}
