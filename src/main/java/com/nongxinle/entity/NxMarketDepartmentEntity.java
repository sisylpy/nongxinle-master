package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxMarketDepartmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxMarketDepartmentId;
    private Integer nxMdMarketId;
    private Integer nxMdDepartmentId;
    private String nxMdStatus;
    private String nxMdSource;
    private Date nxMdCreatedAt;
    private Integer nxMdCreatedBy;
}
