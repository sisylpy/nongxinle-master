package com.nongxinle.route;

import com.nongxinle.dto.route.DisRouteOrderSnapshotDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Phase 2b-6：从订单送达字段解析 routeDate（yyyy-MM-dd）。
 * 主权字段优先：nxDoArriveDate → nxDoArriveOnlyDate → nxDoApplyDate。
 */
public final class DisRouteOrderArriveDateHelper {

    private static final Pattern FULL_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern MONTH_DAY = Pattern.compile("^\\d{2}-\\d{2}$");

    private DisRouteOrderArriveDateHelper() {
    }

    public static String resolveOrderRouteDate(DisRouteOrderSnapshotDto order) {
        if (order == null) {
            return null;
        }
        if (isFullDate(order.getArriveDate())) {
            return order.getArriveDate().trim();
        }
        if (isMonthDay(order.getArriveOnlyDate())) {
            return composeYearMonthDay(inferYear(order), order.getArriveOnlyDate().trim());
        }
        if (isFullDate(order.getApplyDate())) {
            return order.getApplyDate().trim();
        }
        return null;
    }

    public static List<DisRouteOrderSnapshotDto> filterByRouteDate(List<DisRouteOrderSnapshotDto> orders,
                                                                   String routeDate) {
        List<DisRouteOrderSnapshotDto> filtered = new ArrayList<DisRouteOrderSnapshotDto>();
        if (orders == null || routeDate == null) {
            return filtered;
        }
        for (DisRouteOrderSnapshotDto order : orders) {
            if (routeDate.equals(resolveOrderRouteDate(order))) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    public static Set<String> collectDistinctRouteDates(List<DisRouteOrderSnapshotDto> orders) {
        Set<String> dates = new LinkedHashSet<String>();
        if (orders == null) {
            return dates;
        }
        for (DisRouteOrderSnapshotDto order : orders) {
            String routeDate = resolveOrderRouteDate(order);
            if (routeDate != null) {
                dates.add(routeDate);
            }
        }
        return dates;
    }

    public static String toRouteDateOnly(String routeDate) {
        if (routeDate == null || routeDate.length() < 10) {
            return null;
        }
        return routeDate.substring(5);
    }

    private static int inferYear(DisRouteOrderSnapshotDto order) {
        if (order.getApplyDate() != null && isFullDate(order.getApplyDate())) {
            return Integer.parseInt(order.getApplyDate().trim().substring(0, 4));
        }
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    private static String composeYearMonthDay(int year, String monthDay) {
        return year + "-" + monthDay;
    }

    private static boolean isFullDate(String value) {
        return value != null && FULL_DATE.matcher(value.trim()).matches();
    }

    private static boolean isMonthDay(String value) {
        return value != null && MONTH_DAY.matcher(value.trim()).matches();
    }

    static Date parseRouteDateStart(String routeDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            return format.parse(routeDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("路线日格式无效: " + routeDate);
        }
    }
}
