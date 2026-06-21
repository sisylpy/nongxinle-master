package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class NxDisRouteStopOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDrsoId;
    private Integer nxDrsoStopId;
    private Integer nxDrsoOrderId;
    private Integer nxDrsoDepartmentId;
    private String nxDrsoGoodsName;
    private String nxDrsoQuantity;
    private String nxDrsoStandard;
    private String nxDrsoRemark;
}
