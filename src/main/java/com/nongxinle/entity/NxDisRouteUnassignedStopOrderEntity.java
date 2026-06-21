package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class NxDisRouteUnassignedStopOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDruoId;
    private Integer nxDruoUnassignedStopId;
    private Integer nxDruoOrderId;
    private Integer nxDruoDepartmentId;
    private String nxDruoGoodsName;
    private String nxDruoQuantity;
    private String nxDruoStandard;
    private String nxDruoRemark;
}
