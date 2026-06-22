package com.nongxinle.route;

import com.nongxinle.dto.route.DriverDispatchCandidateDto;
import com.nongxinle.dto.route.DriverDispatchListResponse;
import com.nongxinle.entity.NxDisDriverRouteEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 派单读模型：稳定解析司机展示名，避免用 driverUserId 冒充姓名。 */
public final class DisRouteDispatchDriverNameHelper {

    public static final String MISSING_DRIVER_NAME = "司机资料缺失";
    public static final String MISSING_REASON = "未找到司机账号昵称，请检查司机资料";

    private DisRouteDispatchDriverNameHelper() {
    }

    public static void enrichRoutesDriverNames(List<NxDisDriverRouteEntity> routes,
                                               DriverDispatchListResponse drivers) {
        if (routes == null || routes.isEmpty()) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            if (route == null) {
                continue;
            }
            DriverDispatchCandidateDto meta = findDriver(drivers, route.getNxDdrDriverUserId());
            route.setDriverName(resolveRouteDriverName(route.getDriverName(), route.getNxDdrDriverUserId(), meta));
        }
    }

    public static DriverDispatchCandidateDto findDriver(DriverDispatchListResponse drivers,
                                                        Integer driverUserId) {
        if (drivers == null || drivers.getDrivers() == null || driverUserId == null) {
            return null;
        }
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver != null && driverUserId.equals(driver.getDriverUserId())) {
                return driver;
            }
        }
        return null;
    }

    public static String resolveRouteDriverName(NxDisDriverRouteEntity route,
                                                DriverDispatchCandidateDto driverMeta) {
        if (route == null) {
            return MISSING_DRIVER_NAME;
        }
        return resolveRouteDriverName(route.getDriverName(), route.getNxDdrDriverUserId(), driverMeta);
    }

    public static String resolveRouteDriverName(String existingName,
                                                Integer driverUserId,
                                                DriverDispatchCandidateDto driverMeta) {
        if (!isPlaceholderDriverName(existingName, driverUserId)) {
            return existingName.trim();
        }
        if (driverMeta != null) {
            if (!isPlaceholderDriverName(driverMeta.getDriverName(), driverUserId)) {
                return driverMeta.getDriverName().trim();
            }
            if (driverMeta.getDriverPhone() != null && !driverMeta.getDriverPhone().trim().isEmpty()) {
                return driverMeta.getDriverPhone().trim();
            }
        }
        return MISSING_DRIVER_NAME;
    }

    public static boolean isResolvedName(String driverName) {
        return driverName != null
                && !driverName.trim().isEmpty()
                && !MISSING_DRIVER_NAME.equals(driverName.trim());
    }

    public static boolean isPlaceholderDriverName(String name, Integer driverUserId) {
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        String trimmed = name.trim();
        if (MISSING_DRIVER_NAME.equals(trimmed)) {
            return true;
        }
        if (driverUserId != null) {
            if (String.valueOf(driverUserId).equals(trimmed)) {
                return true;
            }
            if (("司机 " + driverUserId).equals(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, String> indexDriverNames(DriverDispatchListResponse drivers) {
        Map<Integer, String> index = new HashMap<Integer, String>();
        if (drivers == null || drivers.getDrivers() == null) {
            return index;
        }
        for (DriverDispatchCandidateDto driver : drivers.getDrivers()) {
            if (driver == null || driver.getDriverUserId() == null) {
                continue;
            }
            index.put(driver.getDriverUserId(),
                    resolveRouteDriverName(driver.getDriverName(), driver.getDriverUserId(), driver));
        }
        return index;
    }
}
