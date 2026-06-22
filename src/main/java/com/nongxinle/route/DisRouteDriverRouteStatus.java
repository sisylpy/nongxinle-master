package com.nongxinle.route;

/** Phase 3D：司机趟次路线执行态（nx_dis_driver_route.nx_ddr_route_status） */
public final class DisRouteDriverRouteStatus {

    /** 无 DB 路线且无站点（空闲司机） */
    public static final String IDLE = "IDLE";

    /** 今日派单页：站点已确认分派，尚未进入装车流程 */
    public static final String DISPATCH_CONFIRMED = "DISPATCH_CONFIRMED";

    public static final String LOADING = "LOADING";
    public static final String READY_TO_DEPART = "READY_TO_DEPART";
    public static final String IN_DELIVERY = "IN_DELIVERY";
    public static final String DELIVERED = "DELIVERED";
    /** 读模型：全店已送达 */
    public static final String COMPLETED = "COMPLETED";
    public static final String CLOSED = "CLOSED";

    private DisRouteDriverRouteStatus() {
    }

    public static boolean isDeparted(String status) {
        return IN_DELIVERY.equals(status) || DELIVERED.equals(status) || COMPLETED.equals(status);
    }

    public static boolean isTerminal(String status) {
        return DELIVERED.equals(status) || COMPLETED.equals(status) || CLOSED.equals(status);
    }
}
