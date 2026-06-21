package com.nongxinle.service;

import com.nongxinle.dto.route.RouteFeasibilityResult;

/**
 * Phase 2b-1：基于 schedule + batch + duty 的可执行性评估（不改分配）。
 */
public interface DisRouteFeasibilityService {

    /**
     * 写库评估：回填 batch（若缺失）、更新 route/plan feasibilityStatus。
     */
    RouteFeasibilityResult assess(Integer planId);

    /**
     * 只读评估：与 assess 相同规则构造 warnings/feasibilityStatus，不写库。
     */
    RouteFeasibilityResult preview(Integer planId);
}
