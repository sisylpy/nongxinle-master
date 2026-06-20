package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxPlatformOrderAssignEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxPoaId;
    private Integer nxPoaMarketId;
    private Integer nxPoaOrderId;
    private Integer nxPoaDepartmentId;
    private Integer nxPoaNxGoodsId;
    private String nxPoaAssignStatus;
    private String nxPoaAssignMode;
    private String nxPoaAssignSource;
    private String nxPoaSourceType;
    private Integer nxPoaGbDepartmentId;
    private Integer nxPoaGbDepartmentFatherId;
    private Integer nxPoaGbDepartmentOrderId;
    private Integer nxPoaAssignedDistributerId;
    private Integer nxPoaAssignedDisGoodsId;
    private BigDecimal nxPoaAssignedPrice;
    private Date nxPoaAssignedAt;
    private Integer nxPoaAssignedBy;
    private Integer nxPoaDefaultId;
    private Integer nxPoaSwitchLogId;
    private Integer nxPoaSnapshotId;
    private String nxPoaDeviationFlags;
    private Date nxPoaCreatedAt;
    private Date nxPoaUpdatedAt;
}
