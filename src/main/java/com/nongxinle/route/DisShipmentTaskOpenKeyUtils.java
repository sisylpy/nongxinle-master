package com.nongxinle.route;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskStatus.*;

/**
 * open_key 规则：同一 disId + routeDate + depFatherId 仅允许一个 open task。
 * UNASSIGNED 仍占 open_key；关闭态释放后可新建补单 task。
 */
public final class DisShipmentTaskOpenKeyUtils {

    private static final Set<String> OPEN_STATUSES = new HashSet<String>(Arrays.asList(
            SIMULATED, ASSIGNED, UNASSIGNED
    ));

    private static final Set<String> CLOSED_STATUSES = new HashSet<String>(Arrays.asList(
            READY_TO_GO, CANCELLED, CLOSED, IN_DELIVERY, DELIVERED
    ));

    private DisShipmentTaskOpenKeyUtils() {
    }

    public static String buildOpenKey(Integer disId, String routeDate, Integer depFatherId) {
        return disId + "-" + routeDate + "-" + depFatherId;
    }

    public static boolean isOpenStatus(String status) {
        return status != null && OPEN_STATUSES.contains(status);
    }

    public static boolean isClosedStatus(String status) {
        return status != null && CLOSED_STATUSES.contains(status);
    }
}
