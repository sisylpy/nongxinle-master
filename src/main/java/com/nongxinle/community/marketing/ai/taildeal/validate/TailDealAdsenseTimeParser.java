package com.nongxinle.community.marketing.ai.taildeal.validate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TailDealAdsenseTimeParser {

    private static final Pattern HH_MM = Pattern.compile("(\\d{1,2})[:：点](\\d{0,2})");
    private static final Pattern HOUR_ONLY = Pattern.compile("(\\d{1,2})\\s*点");

    public static class ParsedTime {
        private final String hhmm;
        private final LocalDateTime dateTime;

        public ParsedTime(String hhmm, LocalDateTime dateTime) {
            this.hhmm = hhmm;
            this.dateTime = dateTime;
        }

        public String getHhmm() {
            return hhmm;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
    }

    public ParsedTime parseEndTime(String endTimeText, String defaultMarketCloseTime) {
        if (endTimeText == null || endTimeText.trim().isEmpty()) {
            return null;
        }
        String text = endTimeText.trim();
        if (text.contains("闭市前")) {
            return parseHhmm(defaultMarketCloseTime != null ? defaultMarketCloseTime : "12:00");
        }
        if (text.contains("中午前") || text.contains("中午")) {
            return parseHhmm("12:00");
        }
        Matcher m = HH_MM.matcher(text);
        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            String minutePart = m.group(2);
            int minute = (minutePart == null || minutePart.isEmpty()) ? 0 : Integer.parseInt(minutePart);
            return build(hour, minute);
        }
        Matcher hourOnly = HOUR_ONLY.matcher(text);
        if (hourOnly.find()) {
            return build(Integer.parseInt(hourOnly.group(1)), 0);
        }
        return null;
    }

    public ParsedTime parseStartTime(String startTimeText) {
        if (startTimeText == null || startTimeText.trim().isEmpty()) {
            LocalTime now = LocalTime.now();
            return build(now.getHour(), now.getMinute());
        }
        Matcher m = HH_MM.matcher(startTimeText.trim());
        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            String minutePart = m.group(2);
            int minute = (minutePart == null || minutePart.isEmpty()) ? 0 : Integer.parseInt(minutePart);
            return build(hour, minute);
        }
        LocalTime now = LocalTime.now();
        return build(now.getHour(), now.getMinute());
    }

    public ParsedTime resolveEndTime(String endTimeText, String defaultMarketCloseTime) {
        if (endTimeText != null && !endTimeText.trim().isEmpty()) {
            ParsedTime parsed = parseEndTime(endTimeText, defaultMarketCloseTime);
            if (parsed != null) {
                return parsed;
            }
        }
        String fallback = defaultMarketCloseTime != null && !defaultMarketCloseTime.isEmpty()
                ? defaultMarketCloseTime : "23:59";
        ParsedTime parsed = parseEndTime("闭市前", fallback);
        return parsed != null ? parsed : build(23, 59);
    }

    public boolean isEndTimeExpired(String endTimeText, String defaultMarketCloseTime) {
        ParsedTime end = resolveEndTime(endTimeText, defaultMarketCloseTime);
        return end != null && !end.getDateTime().isAfter(LocalDateTime.now());
    }

    private ParsedTime parseHhmm(String hhmm) {
        String[] parts = hhmm.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return build(hour, minute);
    }

    private ParsedTime build(int hour, int minute) {
        if (hour > 23) {
            hour = 23;
        }
        if (minute > 59) {
            minute = 59;
        }
        String hhmm = String.format("%02d:%02d", hour, minute);
        LocalDateTime dt = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
        return new ParsedTime(hhmm, dt);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
