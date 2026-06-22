package com.nongxinle.route;

/** 路线距离来源与类型标记（读模型 / API 对外）。 */
public final class DisRouteDistanceTypes {

    private DisRouteDistanceTypes() {
    }

    public static final String PROVIDER_TENCENT_MATRIX = "TENCENT_MATRIX";
    public static final String PROVIDER_HAVERSINE = "HAVERSINE";

    /** 腾讯驾车路线矩阵距离 */
    public static final String ROUTE_DISTANCE = "ROUTE_DISTANCE";
    /** 经纬度直线估算（接口失败 fallback） */
    public static final String ESTIMATED_STRAIGHT_DISTANCE = "ESTIMATED_STRAIGHT_DISTANCE";
}
