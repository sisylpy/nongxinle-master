package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户-司机配送约束（第一版以历史偏好为主，预留后台管理字段）。
 */
@Getter
@Setter
public class DisRouteCustomerDriverConstraintDto {
    private Integer departmentId;
    private List<Integer> allowedDriverUserIds = new ArrayList<Integer>();
    private Integer preferredDriverUserId;
    private List<Integer> forbiddenDriverUserIds = new ArrayList<Integer>();
    /** MUST / PREFER / FORBID */
    private String constraintType;
    private String remark;
}
