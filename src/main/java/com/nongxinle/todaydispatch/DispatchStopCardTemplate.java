package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.DispatchStoreCardDto;
import com.nongxinle.route.DisRouteSandboxTodayTimelineBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/** 客户站点统一展示模板（timeline / 未分配 / 人工调度 storeCard 共用）。 */
public final class DispatchStopCardTemplate {

    private DispatchStopCardTemplate() {
    }

    /** 门店卡统一 Map 契约（与 timeline stop 节点字段对齐）。 */
    public static Map<String, Object> buildStoreStopMap(CustomerStopPlan stop) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (stop == null) {
            return map;
        }
        put(map, "cardKey", stop.getCardKey());
        put(map, "customerName", stop.getCustomerName());
        put(map, "customerShortName", stop.getCustomerShortName());
        put(map, "departmentId", stop.getDepFatherId());
        put(map, "depFatherId", stop.getDepFatherId());
        put(map, "depId", stop.getDepFatherId());
        put(map, "customerId", stop.getCustomerId());
        put(map, "sandboxStopKey", stop.getSandboxStopKey());
        put(map, "goodsSummary", stop.getGoodsSummary());
        put(map, "distanceText", stop.getDistanceText());
        put(map, "durationText", stop.getDurationText());
        put(map, "plannedArrivalLabel", stop.getPlannedArrivalLabel());
        put(map, "plannedDepartureLabel", stop.getPlannedDepartureLabel());
        put(map, "arrivalLabel", stop.getPlannedArrivalLabel());
        put(map, "departureLabel", stop.getPlannedDepartureLabel());
        put(map, "customerWindowLabel", stop.getCustomerWindowLabel());
        put(map, "windowLabel", stop.getWindowLabel());
        put(map, "deliveryWindowLabel", stop.getCustomerWindowLabel());
        put(map, "windowRequirementLabel", stop.getWindowRequirementLabel());
        if (Boolean.TRUE.equals(stop.getWindowRequirementModified())) {
            map.put("windowRequirementModified", Boolean.TRUE);
        }
        put(map, "windowStatusLabel", stop.getWindowStatusLabel());
        put(map, "windowTone", resolveWindowTone(stop));
        put(map, "serviceDurationLabel", stop.getServiceDurationLabel());
        put(map, "unloadDurationText", stop.getServiceDurationLabel());
        put(map, "timeLabel", stop.getTimeLabel());
        put(map, "arrivalStatusLabel", stop.getArrivalStatusLabel());
        put(map, "arrivalStatusTone", stop.getArrivalStatusTone());
        put(map, "arrivalStatusType", stop.getArrivalStatusTone());
        put(map, "statusLabel", stop.getStatusLabel());
        put(map, "taskId", stop.getTaskId());
        put(map, "deliveryStopId", stop.getDeliveryStopId());
        String legText = DisRouteSandboxTodayTimelineBuilder.joinLegText(
                stop.getDistanceText(), stop.getDurationText());
        put(map, "legText", legText);
        put(map, "legDistanceLabel", legText);
        if (stop.getPrimaryAction() != null) {
            map.put("primaryAction", stop.getPrimaryAction());
        }
        return map;
    }

    public static void applyToCard(Map<String, Object> card, CustomerStopPlan stop) {
        if (card == null || stop == null) {
            return;
        }
        card.putAll(buildStoreStopMap(stop));
    }

    public static DispatchStoreCardDto toStoreCard(CustomerStopPlan stop) {
        DispatchStoreCardDto card = new DispatchStoreCardDto();
        if (stop == null) {
            return card;
        }
        Map<String, Object> map = buildStoreStopMap(stop);
        card.setCardKey(asString(map.get("cardKey")));
        card.setDepartmentId(asInteger(map.get("departmentId")));
        card.setDepFatherId(asInteger(map.get("depFatherId")));
        card.setSandboxStopKey(asString(map.get("sandboxStopKey")));
        card.setCustomerName(asString(map.get("customerName")));
        card.setGoodsSummary(asString(map.get("goodsSummary")));
        card.setDistanceText(asString(map.get("distanceText")));
        card.setDurationText(asString(map.get("durationText")));
        card.setLegText(asString(map.get("legText")));
        card.setPlannedArrivalLabel(asString(map.get("plannedArrivalLabel")));
        card.setPlannedDepartureLabel(asString(map.get("plannedDepartureLabel")));
        card.setCustomerWindowLabel(asString(map.get("customerWindowLabel")));
        card.setWindowRequirementLabel(asString(map.get("windowRequirementLabel")));
        card.setWindowRequirementModified(stop.getWindowRequirementModified());
        card.setServiceDurationLabel(asString(map.get("serviceDurationLabel")));
        card.setTimeLabel(asString(map.get("timeLabel")));
        card.setArrivalStatusLabel(asString(map.get("arrivalStatusLabel")));
        card.setArrivalStatusTone(asString(map.get("arrivalStatusTone")));
        card.setDispatchStatusLabel("未分配");
        return card;
    }

    private static String resolveWindowTone(CustomerStopPlan stop) {
        if (stop == null || stop.getArrivalStatusTone() == null) {
            return null;
        }
        String tone = stop.getArrivalStatusTone().trim();
        if ("warn".equalsIgnoreCase(tone) || "late".equalsIgnoreCase(tone)) {
            return "warn";
        }
        return tone;
    }

    private static void put(Map<String, Object> card, String key, Object value) {
        if (value != null) {
            card.put(key, value);
        }
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        return null;
    }
}
