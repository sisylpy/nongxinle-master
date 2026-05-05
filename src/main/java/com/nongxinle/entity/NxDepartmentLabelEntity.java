package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 客户部门与配送商私有标签关联表
 *
 * @author lpy
 * @date 2026-04-16
 */
@Setter
@Getter
@ToString
public class NxDepartmentLabelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDepartmentDistributerLabelId;
    private Integer nxDdlDepartmentId;
    private Integer nxDdlDistributerLabelId;
    private Integer nxDdlDistributerId;
}
