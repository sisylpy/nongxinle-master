package com.nongxinle.route;

/** Phase 2b-6：站点预计到达相对服务器时间的语义状态 */
public final class DisRouteStopTemporalStatus {
    public static final String UPCOMING = "UPCOMING";
    public static final String OK = "OK";
    public static final String EXPIRED = "EXPIRED";
    public static final String PAST_DUE = "PAST_DUE";

    private DisRouteStopTemporalStatus() {
    }
}
