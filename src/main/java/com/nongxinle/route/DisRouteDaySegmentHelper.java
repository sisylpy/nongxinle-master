package com.nongxinle.route;

import java.util.Calendar;
import java.util.Date;

/**
 * 按服务器当前时刻划分当日派车展示时段（仅影响读模型文案，与 DB dispatchBatch 解耦）。
 * <ul>
 *   <li>00:00–11:59 早班</li>
 *   <li>12:00–17:59 中班</li>
 *   <li>18:00–23:59 晚班</li>
 * </ul>
 */
public final class DisRouteDaySegmentHelper {

    public static final String SEGMENT_MORNING = "MORNING";
    public static final String SEGMENT_MIDDAY = "MIDDAY";
    public static final String SEGMENT_EVENING = "EVENING";

    private DisRouteDaySegmentHelper() {
    }

    public static String resolveSegmentCode(Date serverNow) {
        if (serverNow == null) {
            return SEGMENT_MORNING;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(serverNow);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return SEGMENT_MORNING;
        }
        if (hour < 18) {
            return SEGMENT_MIDDAY;
        }
        return SEGMENT_EVENING;
    }

    public static String resolveSegmentLabel(String segmentCode) {
        if (segmentCode == null || segmentCode.trim().isEmpty()) {
            return null;
        }
        String code = segmentCode.trim().toUpperCase();
        if (SEGMENT_MORNING.equals(code)) {
            return "早班";
        }
        if (SEGMENT_MIDDAY.equals(code)) {
            return "中班";
        }
        if (SEGMENT_EVENING.equals(code)) {
            return "晚班";
        }
        String legacy = DisRouteDispatchLabels.label(code);
        return legacy != null ? legacy : code;
    }

    public static String resolveDisplayShiftLabel(String routeDate, Date serverNow) {
        if (!isRouteDateToday(routeDate, serverNow)) {
            return null;
        }
        return resolveSegmentLabel(resolveSegmentCode(serverNow));
    }

    public static boolean isRouteDateToday(String routeDate, Date serverNow) {
        if (routeDate == null || routeDate.trim().isEmpty() || serverNow == null) {
            return false;
        }
        return routeDate.trim().equals(DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow));
    }
}
