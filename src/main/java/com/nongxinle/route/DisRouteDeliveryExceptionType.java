package com.nongxinle.route;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Phase 3E：司机端配送异常类型 */
public final class DisRouteDeliveryExceptionType {

    public static final String CUSTOMER_NOT_AVAILABLE = "CUSTOMER_NOT_AVAILABLE";
    public static final String SHORTAGE = "SHORTAGE";
    public static final String RETURN_GOODS = "RETURN_GOODS";
    public static final String ADDRESS_ERROR = "ADDRESS_ERROR";
    public static final String TEMP_CHANGE = "TEMP_CHANGE";
    public static final String OTHER = "OTHER";

    private static final Set<String> KNOWN = new HashSet<String>();
    private static final Map<String, String> LABELS;

    static {
        KNOWN.add(CUSTOMER_NOT_AVAILABLE);
        KNOWN.add(SHORTAGE);
        KNOWN.add(RETURN_GOODS);
        KNOWN.add(ADDRESS_ERROR);
        KNOWN.add(TEMP_CHANGE);
        KNOWN.add(OTHER);

        Map<String, String> map = new HashMap<String, String>();
        map.put(CUSTOMER_NOT_AVAILABLE, "客户不在");
        map.put(SHORTAGE, "少货");
        map.put(RETURN_GOODS, "退货");
        map.put(ADDRESS_ERROR, "地址异常");
        map.put(TEMP_CHANGE, "临时改送");
        map.put(OTHER, "其他");
        LABELS = Collections.unmodifiableMap(map);
    }

    private DisRouteDeliveryExceptionType() {
    }

    public static String normalize(String exceptionType) {
        if (exceptionType == null || exceptionType.trim().isEmpty()) {
            return OTHER;
        }
        String normalized = exceptionType.trim().toUpperCase();
        return KNOWN.contains(normalized) ? normalized : OTHER;
    }

    public static String label(String exceptionType) {
        String normalized = normalize(exceptionType);
        return LABELS.get(normalized);
    }
}
