package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCommunityDispatchStopItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxCommunityDispatchStopItemId;
    private Integer nxCdsiStopId;
    private Integer nxCdsiCommunityOrderId;
    private String nxCdsiGoodsSummary;
    private String nxCdsiOrderTotal;
    private Date nxCdsiCreatedAt;

    private NxCommunityOrdersEntity orderEntity;
}
