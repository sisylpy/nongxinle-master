package com.nongxinle.service;

/**
 * Phase 2a：固定路线顺序下的配送时间窗排程（不重排、不换司机）。
 */
public interface DisRouteScheduleService {

    /**
     * 按 plan 当前 stop 顺序与 driverRoute 分配，计算 ETA / 等待 / 迟到并写库。
     */
    void computeSchedule(Integer planId);
}
