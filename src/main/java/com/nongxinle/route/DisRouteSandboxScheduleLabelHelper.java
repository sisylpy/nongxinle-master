package com.nongxinle.route;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Phase 3a：沙盘排程展示文案（前端只读，不自行拼「今天/明天」） */
public final class DisRouteSandboxScheduleLabelHelper {

    private DisRouteSandboxScheduleLabelHelper() {
    }

    public static String modeLabel(String mode) {
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode)) {
            return "临时补单";
        }
        if (DisRouteSandboxScheduleMode.SCHEDULED_BATCH.equals(mode)) {
            return "正常预排";
        }
        return null;
    }

    public static String timeBasisLabel(String basis) {
        if (DisRouteSandboxScheduleTimeBasis.SERVER_NOW.equals(basis)) {
            return "按当前最快送达";
        }
        if (DisRouteSandboxScheduleTimeBasis.CUSTOMER_WINDOW.equals(basis)) {
            return "按客户送达时间窗";
        }
        return null;
    }

    public static String timeAnchorLabel(String mode) {
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode)) {
            return "当前时间";
        }
        return "客户送达时间窗";
    }

    public static String formatTimeHm(Date dateTime) {
        if (dateTime == null) {
            return null;
        }
        return new SimpleDateFormat("HH:mm").format(dateTime);
    }

    public static String formatFastestArrivalLabel(Date arrivalAt, Date serverNow) {
        return formatFastestArrivalLabel(arrivalAt, serverNow, null);
    }

    public static String formatFastestArrivalLabel(Date arrivalAt, Date serverNow, String scheduleMode) {
        if (arrivalAt == null || serverNow == null) {
            return null;
        }
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(scheduleMode)) {
            return formatAdhocFastestArrivalLabel(arrivalAt, serverNow);
        }
        String time = formatTimeHm(arrivalAt);
        String arrivalDate = new SimpleDateFormat("yyyy-MM-dd").format(arrivalAt);
        String today = DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow);
        if (arrivalDate.equals(today)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(serverNow);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour >= 18) {
                return "预计最快今晚 " + time + " 到";
            }
            return "预计最快今天 " + time + " 到";
        }
        String tomorrow = formatTomorrowYyyyMmDd(serverNow);
        if (arrivalDate.equals(tomorrow)) {
            return "预计最快明天 " + time + " 到";
        }
        return "预计最快 " + DisRouteTemporalHelper.formatDateTimeLabel(arrivalAt, serverNow) + " 到";
    }

    /** 临时补单：跨午夜也不显示「明天」，优先分钟数 / 凌晨 HH:mm。 */
    public static String formatAdhocPlannedArrivalLabel(Date arrivalAt, Date serverNow) {
        if (arrivalAt == null || serverNow == null) {
            return null;
        }
        long diffMs = arrivalAt.getTime() - serverNow.getTime();
        int diffMinutes = (int) Math.ceil(Math.max(0, diffMs) / 60000.0);
        if (diffMinutes <= 0) {
            return "现在可送";
        }
        if (diffMinutes <= 180) {
            return "约 " + diffMinutes + " 分钟后到";
        }
        Calendar arr = Calendar.getInstance();
        arr.setTime(arrivalAt);
        int hour = arr.get(Calendar.HOUR_OF_DAY);
        String hm = formatTimeHm(arrivalAt);
        if (hour < 6) {
            return "凌晨 " + hm + " 到";
        }
        String arrivalDate = new SimpleDateFormat("yyyy-MM-dd").format(arrivalAt);
        String today = DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow);
        if (arrivalDate.equals(today)) {
            if (hour >= 18) {
                return "今晚 " + hm + " 到";
            }
            return "今天 " + hm + " 到";
        }
        return "凌晨 " + hm + " 到";
    }

    public static String formatAdhocFastestArrivalLabel(Date arrivalAt, Date serverNow) {
        String shortLabel = formatAdhocPlannedArrivalLabel(arrivalAt, serverNow);
        if (shortLabel == null) {
            return null;
        }
        if ("现在可送".equals(shortLabel)) {
            return shortLabel;
        }
        if (shortLabel.startsWith("约 ")) {
            return "预计最快 " + shortLabel;
        }
        return "预计最快" + shortLabel;
    }

    public static String formatCustomerWindowLabel(String deliveryRouteDate,
                                                   Integer earliestSeconds,
                                                   Integer latestSeconds,
                                                   Date serverNow) {
        if (earliestSeconds == null && latestSeconds == null) {
            return "未配置常规送达窗口";
        }
        String window = formatPlainTimeRange(earliestSeconds, latestSeconds);
        if (deliveryRouteDate == null || serverNow == null) {
            return "常规窗口 " + window;
        }
        String today = DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow);
        if (deliveryRouteDate.compareTo(today) > 0) {
            return "常规窗口 " + window + "（送达日 " + formatRouteDateLabel(deliveryRouteDate, serverNow) + "）";
        }
        return "常规窗口 " + window;
    }

    /** 页面展示用：仅 HH:mm–HH:mm，不含「常规窗口 / 要求」等前缀。 */
    public static String formatPlainTimeRange(Integer earliestSeconds, Integer latestSeconds) {
        if (earliestSeconds == null && latestSeconds == null) {
            return null;
        }
        String start = secondsToHm(earliestSeconds);
        String end = secondsToHm(latestSeconds);
        return (start != null ? start : "--") + "–" + (end != null ? end : "--");
    }

    public static String formatDepartLabel(Date departAt, Date serverNow, String mode) {
        if (departAt == null) {
            return null;
        }
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode)) {
            return "现在可送";
        }
        return DisRouteTemporalHelper.formatDateTimeLabel(departAt, serverNow) + " 出发";
    }

    public static String formatRouteScheduleSummary(String mode,
                                                    Date fastestArrival,
                                                    Date serverNow) {
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode)) {
            String shortLabel = formatAdhocPlannedArrivalLabel(fastestArrival, serverNow);
            if (shortLabel != null) {
                return "现在可送 · " + shortLabel;
            }
            return "现在可送 · 当前按最快送达计算";
        }
        return null;
    }

    public static String formatPlanScheduleBanner(String mode, Date fastestArrival, Date serverNow) {
        if (DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode)) {
            String shortLabel = formatAdhocPlannedArrivalLabel(fastestArrival, serverNow);
            if (shortLabel != null) {
                return "临时补单｜现在可送｜" + shortLabel;
            }
            return "临时补单｜现在可送";
        }
        return null;
    }

    private static String secondsToHm(Integer seconds) {
        if (seconds == null) {
            return null;
        }
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        return String.format("%02d:%02d", h, m);
    }

    private static String formatRouteDateLabel(String routeDate, Date serverNow) {
        return DisRouteTemporalHelper.formatRouteDateLabel(routeDate, serverNow);
    }

    private static String formatTomorrowYyyyMmDd(Date serverNow) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(serverNow);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
}
