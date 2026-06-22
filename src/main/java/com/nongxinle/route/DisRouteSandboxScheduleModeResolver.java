package com.nongxinle.route;

import java.util.Calendar;
import java.util.Date;

/**
 * 判定沙盘站点使用正式预排还是即时补单排程。
 */
public final class DisRouteSandboxScheduleModeResolver {

    private DisRouteSandboxScheduleModeResolver() {
    }

    public static String resolveMode(String deliveryRouteDate,
                                       Date serverNow,
                                       Integer earliestSeconds,
                                       Integer latestSeconds) {
        if (serverNow == null) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }
        String today = DisRouteTemporalHelper.formatTodayYyyyMmDd(serverNow);
        if (deliveryRouteDate == null || deliveryRouteDate.trim().isEmpty()) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }
        String deliveryDate = deliveryRouteDate.trim();

        Date dayStart = DisRouteOrderArriveDateHelper.parseRouteDateStart(deliveryDate);
        Date earliestAt = earliestSeconds != null ? addSeconds(dayStart, earliestSeconds) : null;
        Date latestAt = latestSeconds != null ? addSeconds(dayStart, latestSeconds) : null;

        // 送达日在未来（如今晚下单、明早送达）→ 老板当前打开沙盘按补单最快送
        if (deliveryDate.compareTo(today) > 0) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }

        // 送达日已过
        if (deliveryDate.compareTo(today) < 0) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }

        // 送达日 = 今天
        if (latestAt != null && serverNow.after(latestAt)) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }
        if (earliestAt != null && serverNow.before(earliestAt)) {
            return DisRouteSandboxScheduleMode.SCHEDULED_BATCH;
        }
        if (earliestAt != null && latestAt != null
                && !serverNow.before(earliestAt) && !serverNow.after(latestAt)) {
            return DisRouteSandboxScheduleMode.SCHEDULED_BATCH;
        }
        if (earliestAt == null && latestAt == null) {
            return DisRouteSandboxScheduleMode.ADHOC_NOW;
        }
        return DisRouteSandboxScheduleMode.SCHEDULED_BATCH;
    }

    public static boolean isAdhocNow(String mode) {
        return DisRouteSandboxScheduleMode.ADHOC_NOW.equals(mode);
    }

    private static Date addSeconds(Date base, int seconds) {
        return new Date(base.getTime() + seconds * 1000L);
    }
}
