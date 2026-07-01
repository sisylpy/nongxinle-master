package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.entity.NxCommunityOrdersEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 商城订单 nx_CO_service_date / nx_CO_service_time 解析与展示（分钟制）。 */
public final class CommunityDispatchOrderServiceTimeHelper {

    private CommunityDispatchOrderServiceTimeHelper() {
    }

    public static Integer resolveServiceTimeMinutes(NxCommunityOrdersEntity order) {
        if (order == null) {
            return null;
        }
        Integer fromServiceTime = parseMinutes(order.getNxCoServiceTime());
        if (fromServiceTime != null) {
            return fromServiceTime;
        }
        return parseHourMinute(order.getNxCoServiceHour(), order.getNxCoServiceMinute());
    }

    /** 写入 stop 表时使用：优先 nx_CO_service_time，否则由 hour+minute 计算。 */
    public static String resolveServiceTimeRaw(NxCommunityOrdersEntity order) {
        if (order == null) {
            return null;
        }
        if (order.getNxCoServiceTime() != null && !order.getNxCoServiceTime().trim().isEmpty()) {
            return order.getNxCoServiceTime().trim();
        }
        Integer minutes = resolveServiceTimeMinutes(order);
        return minutes != null ? String.valueOf(minutes) : null;
    }

    public static Integer resolveServiceTimeMinutes(String serviceTime, String serviceHour, String serviceMinute) {
        Integer fromServiceTime = parseMinutes(serviceTime);
        if (fromServiceTime != null) {
            return fromServiceTime;
        }
        return parseHourMinute(serviceHour, serviceMinute);
    }

    public static String resolveServiceDate(NxCommunityOrdersEntity order, String fallbackRouteDate) {
        if (order != null && order.getNxCoServiceDate() != null
                && !order.getNxCoServiceDate().trim().isEmpty()) {
            return order.getNxCoServiceDate().trim();
        }
        return fallbackRouteDate;
    }

    public static String resolveServiceDate(String serviceDate, String fallbackRouteDate) {
        if (serviceDate != null && !serviceDate.trim().isEmpty()) {
            return serviceDate.trim();
        }
        return fallbackRouteDate;
    }

    /** 从同地址订单组取最严格的送达时刻（最早 service_date+time）。 */
    public static RequestedServiceTime resolveStrictestFromOrders(
            List<NxCommunityOrdersEntity> orders, String fallbackRouteDate) {
        if (orders == null || orders.isEmpty()) {
            return RequestedServiceTime.empty();
        }
        RequestedServiceTime strictest = null;
        for (NxCommunityOrdersEntity order : orders) {
            RequestedServiceTime candidate = resolveFromOrder(order, fallbackRouteDate);
            if (!candidate.isPresent()) {
                continue;
            }
            if (strictest == null || candidate.getRequestedAt().before(strictest.getRequestedAt())) {
                strictest = candidate;
            }
        }
        return strictest != null ? strictest : RequestedServiceTime.empty();
    }

    public static RequestedServiceTime resolveFromOrder(NxCommunityOrdersEntity order, String fallbackRouteDate) {
        if (order == null) {
            return RequestedServiceTime.empty();
        }
        Integer minutes = resolveServiceTimeMinutes(order);
        if (minutes == null) {
            return RequestedServiceTime.empty();
        }
        String serviceDate = resolveServiceDate(order, fallbackRouteDate);
        Date requestedAt = toDateOnServiceDate(serviceDate, minutes);
        if (requestedAt == null) {
            return RequestedServiceTime.empty();
        }
        return new RequestedServiceTime(serviceDate, minutes, requestedAt);
    }

    public static RequestedServiceTime resolveFromStopFields(
            String serviceDate, String serviceTime, String fallbackRouteDate) {
        Integer minutes = parseMinutes(serviceTime);
        if (minutes == null) {
            return RequestedServiceTime.empty();
        }
        String date = resolveServiceDate(serviceDate, fallbackRouteDate);
        Date requestedAt = toDateOnServiceDate(date, minutes);
        if (requestedAt == null) {
            return RequestedServiceTime.empty();
        }
        return new RequestedServiceTime(date, minutes, requestedAt);
    }

    public static String formatTimeLabel(Integer minutesFromMidnight) {
        if (minutesFromMidnight == null || minutesFromMidnight < 0 || minutesFromMidnight >= 24 * 60) {
            return null;
        }
        int hour = minutesFromMidnight / 60;
        int minute = minutesFromMidnight % 60;
        return String.format(Locale.CHINA, "%02d:%02d", hour, minute);
    }

    /** 卡片角标：要求 08:00 送达 */
    public static String formatWindowRequirementLabel(RequestedServiceTime requested) {
        if (requested == null || !requested.isPresent()) {
            return null;
        }
        String time = formatTimeLabel(requested.getMinutesFromMidnight());
        if (time == null) {
            return null;
        }
        return "要求 " + time + " 送达";
    }

    /** 副标题：2026-07-01 08:00 送达 */
    public static String formatCustomerWindowLabel(RequestedServiceTime requested) {
        if (requested == null || !requested.isPresent()) {
            return null;
        }
        String time = formatTimeLabel(requested.getMinutesFromMidnight());
        if (time == null) {
            return null;
        }
        if (requested.getServiceDate() != null && !requested.getServiceDate().trim().isEmpty()) {
            return requested.getServiceDate().trim() + " " + time + " 送达";
        }
        return time + " 送达";
    }

    public static ArrivalWindowStatus resolveArrivalWindowStatus(Date plannedArrivalAt,
                                                               RequestedServiceTime requested) {
        if (plannedArrivalAt == null || requested == null || !requested.isPresent()) {
            return ArrivalWindowStatus.empty();
        }
        Date target = requested.getRequestedAt();
        long diffMs = plannedArrivalAt.getTime() - target.getTime();
        long diffMinutes = diffMs / 60000L;
        if (diffMinutes > 0L) {
            long minutes = Math.max(1L, diffMinutes);
            return new ArrivalWindowStatus("迟到" + minutes + "分钟", "warn");
        }
        if (diffMinutes < -15L) {
            long minutes = Math.max(1L, Math.abs(diffMinutes));
            return new ArrivalWindowStatus("早到 " + minutes + " 分钟", "early");
        }
        return new ArrivalWindowStatus("准时", "ok");
    }

    private static Integer parseMinutes(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int minutes = Integer.parseInt(raw.trim());
            return minutes >= 0 && minutes < 24 * 60 ? minutes : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseHourMinute(String hour, String minute) {
        if (hour == null || hour.trim().isEmpty() || minute == null || minute.trim().isEmpty()) {
            return null;
        }
        try {
            int h = Integer.parseInt(hour.trim());
            int m = Integer.parseInt(minute.trim());
            if (h < 0 || h >= 24 || m < 0 || m >= 60) {
                return null;
            }
            return h * 60 + m;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Date toDateOnServiceDate(String serviceDate, int minutesFromMidnight) {
        if (serviceDate == null || serviceDate.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            sdf.setLenient(false);
            Date day = sdf.parse(serviceDate.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60);
            cal.set(Calendar.MINUTE, minutesFromMidnight % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception ex) {
            return null;
        }
    }

    public static final class RequestedServiceTime {
        private final String serviceDate;
        private final Integer minutesFromMidnight;
        private final Date requestedAt;

        private RequestedServiceTime(String serviceDate, Integer minutesFromMidnight, Date requestedAt) {
            this.serviceDate = serviceDate;
            this.minutesFromMidnight = minutesFromMidnight;
            this.requestedAt = requestedAt;
        }

        static RequestedServiceTime empty() {
            return new RequestedServiceTime(null, null, null);
        }

        public boolean isPresent() {
            return requestedAt != null && minutesFromMidnight != null;
        }

        public String getServiceDate() {
            return serviceDate;
        }

        public Integer getMinutesFromMidnight() {
            return minutesFromMidnight;
        }

        public Date getRequestedAt() {
            return requestedAt;
        }
    }

    public static final class ArrivalWindowStatus {
        private final String label;
        private final String tone;

        ArrivalWindowStatus(String label, String tone) {
            this.label = label;
            this.tone = tone;
        }

        static ArrivalWindowStatus empty() {
            return new ArrivalWindowStatus(null, null);
        }

        public String getLabel() {
            return label;
        }

        public String getTone() {
            return tone;
        }

        public boolean isPresent() {
            return label != null;
        }
    }
}
