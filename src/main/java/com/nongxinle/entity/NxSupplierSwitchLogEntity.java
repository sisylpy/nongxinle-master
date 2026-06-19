package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxSupplierSwitchLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxSslId;
    private Integer nxSslMarketId;
    private Integer nxSslDepartmentId;
    private Integer nxSslNxGoodsId;
    private Integer nxSslOrderId;
    private Integer nxSslFromDistributerId;
    private Integer nxSslFromDisGoodsId;
    private Integer nxSslToDistributerId;
    private Integer nxSslToDisGoodsId;
    private String nxSslSwitchScope;
    private String nxSslReasonCode;
    private String nxSslReasonNote;
    private Integer nxSslSnapshotId;
    private String nxSslSnapshotAction;
    private Integer nxSslOperatorId;
    private Date nxSslCreatedAt;
}
