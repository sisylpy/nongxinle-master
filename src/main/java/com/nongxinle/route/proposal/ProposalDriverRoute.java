package com.nongxinle.route.proposal;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分派中 Proposal 司机路线：本轮未完成新任务，禁止 DB routeId / routeStatus。
 */
@Getter
@Setter
public class ProposalDriverRoute {

    private Integer driverUserId;
    private String driverName;
    private List<ProposalStop> stops = new ArrayList<ProposalStop>();

    private Long totalDistanceM;
    private Long totalDurationS;
    private String totalDistanceText;
    private String totalDurationText;
    private String scheduleHeadline;
    private String plannedDepartLabel;
    private String plannedReturnLabel;
}
