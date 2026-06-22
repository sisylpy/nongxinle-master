package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 今日派车页展示文案格式化（距离/时长/商品摘要）。 */
public final class DisRouteSandboxDisplayFormatHelper {

    private DisRouteSandboxDisplayFormatHelper() {
    }

    public static String formatDistanceText(Long distanceM) {
        if (distanceM == null || distanceM <= 0L) {
            return null;
        }
        if (distanceM >= 1000L) {
            double km = distanceM / 1000.0;
            if (Math.abs(km - Math.round(km)) < 0.05) {
                return Math.round(km) + " 公里";
            }
            return String.format("%.1f 公里", km);
        }
        return distanceM + " 米";
    }

    public static String formatDurationText(Long durationS) {
        if (durationS == null || durationS <= 0L) {
            return null;
        }
        if (durationS < 60L) {
            return "1 分钟";
        }
        long minutes = Math.max(1L, Math.round(durationS / 60.0));
        if (minutes < 60L) {
            return minutes + " 分钟";
        }
        long hours = minutes / 60L;
        long remainMinutes = minutes % 60L;
        if (remainMinutes <= 0L) {
            return hours + " 小时";
        }
        return hours + " 小时 " + remainMinutes + " 分钟";
    }

    public static Long resolveLegDistanceM(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsLegDistanceM() != null) {
            return stop.getNxDrsLegDistanceM();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstLegDistanceM() : null;
    }

    public static Long resolveLegDurationS(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxDrsLegDurationS() != null) {
            return stop.getNxDrsLegDurationS();
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        return task != null ? task.getNxDstLegDurationS() : null;
    }

    public static String buildGoodsSummary(NxDisShipmentTaskEntity task) {
        if (task == null || task.getItems() == null || task.getItems().isEmpty()) {
            return null;
        }
        Map<String, BigDecimal> quantityByKey = new LinkedHashMap<String, BigDecimal>();
        Map<String, String> standardByKey = new LinkedHashMap<String, String>();
        for (NxDisShipmentTaskItemEntity item : task.getItems()) {
            if (item == null) {
                continue;
            }
            String goodsName = item.getNxDstiGoodsName();
            if (goodsName == null || goodsName.trim().isEmpty()) {
                continue;
            }
            String key = goodsName.trim();
            BigDecimal qty = parseQuantity(item.getNxDstiQuantity());
            if (qty != null) {
                BigDecimal existing = quantityByKey.get(key);
                quantityByKey.put(key, existing != null ? existing.add(qty) : qty);
            }
            if (item.getNxDstiStandard() != null && !item.getNxDstiStandard().trim().isEmpty()) {
                standardByKey.put(key, item.getNxDstiStandard().trim());
            }
        }
        if (quantityByKey.isEmpty()) {
            return null;
        }
        List<String> parts = new ArrayList<String>();
        for (Map.Entry<String, BigDecimal> entry : quantityByKey.entrySet()) {
            String goodsName = entry.getKey();
            String standard = standardByKey.get(goodsName);
            String qtyText = formatQuantity(entry.getValue());
            if (standard != null && !standard.isEmpty()) {
                parts.add(goodsName + " " + qtyText + standard);
            } else {
                parts.add(goodsName + " " + qtyText);
            }
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return parts.get(0) + " 等" + parts.size() + "品";
    }

    private static BigDecimal parseQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(quantity.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String formatQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return "";
        }
        BigDecimal normalized = quantity.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            return normalized.toPlainString();
        }
        return normalized.toPlainString();
    }
}
