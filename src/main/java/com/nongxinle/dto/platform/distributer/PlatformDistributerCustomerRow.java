package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 配送商平台客户列表 SQL 聚合行。
 */
@Getter
@Setter
@ToString
public class PlatformDistributerCustomerRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** GB=京采饭店；NX=旧 nx_department 平台客户 */
    private String customerSource;
    private Integer groupKey;
    private Integer gbDepartmentId;
    private String gbDepartmentName;
    private String gbDepartmentAttrName;
    private Integer marketId;
    private Integer nxDepFatherId;
    private String nxDepAttrName;
    private Integer undoCount;
    private Integer hasPrice;
    private Integer hasWeight;
    private Integer totalCount;
    private Integer finishCount;
    private Double totalSubtotal;
}
