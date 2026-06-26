package com.nongxinle.route;

/**
 * mapOverview.polylines 路线来源（仅展示，不参与派单算法）。
 */
public final class SandboxTodayMapPolylineLineTypes {

    /** 初始直线点（未做道路规划或规划前默认）。 */
    public static final String STRAIGHT = "STRAIGHT";
    /** 腾讯驾车路线规划成功。 */
    public static final String ROAD = "ROAD";
    /** 道路规划失败，保留直线点。 */
    public static final String FALLBACK = "FALLBACK";

    private SandboxTodayMapPolylineLineTypes() {
    }
}
