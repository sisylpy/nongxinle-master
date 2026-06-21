package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class NxDisRouteUnassignedStopEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDrusId;
    private Integer nxDrusPlanId;
    private Integer nxDrusDepartmentId;
    private String nxDrusDepartmentName;
    private Integer nxDrusOrderCount;
    private String nxDrusReason;

    private List<Integer> orderIds;
    private List<NxDisRouteUnassignedStopOrderEntity> orders;
}
