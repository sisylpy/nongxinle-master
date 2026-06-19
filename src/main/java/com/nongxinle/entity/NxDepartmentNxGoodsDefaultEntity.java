package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxDepartmentNxGoodsDefaultEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDngdId;
    private Integer nxDngdMarketId;
    private Integer nxDngdDepartmentId;
    private Integer nxDngdNxGoodsId;
    private Integer nxDngdDefaultDistributerId;
    private Integer nxDngdDefaultDisGoodsId;
    private String nxDngdSource;
    private String nxDngdStatus;
    private Integer nxDngdLastOrderId;
    private Integer nxDngdLastSwitchLogId;
    private Integer nxDngdActiveSnapshotId;
    private Date nxDngdCreatedAt;
    private Integer nxDngdCreatedBy;
    private Date nxDngdUpdatedAt;
    private Integer nxDngdUpdatedBy;
}
