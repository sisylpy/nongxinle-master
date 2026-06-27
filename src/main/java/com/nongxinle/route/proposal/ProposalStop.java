package com.nongxinle.route.proposal;

import com.nongxinle.entity.NxDisRouteStopEntity;
import lombok.Getter;
import lombok.Setter;

/** 分派中页面 Proposal 站点：仅本轮未完成待派，不含历史 delivered / active loading-execution。 */
@Getter
@Setter
public class ProposalStop {

    private Integer depFatherId;
    private String customerName;
    private ProposalStopSource proposalSource;
    /** 历史偏好说明（只读评分输入，非历史 stop 实体）。 */
    private String historyPreferenceNote;
    /** 展示/调度用 stop 壳（ephemeral，无 DB routeStop 主权）。 */
    private NxDisRouteStopEntity stop;
}
