package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityUserEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 社区派车页司机展示名解析：路线卡、地图图例、路线编辑页共用。
 */
public final class CommunityDispatchDriverNameHelper {

    private CommunityDispatchDriverNameHelper() {
    }

    public static String resolveDisplayName(NxCommunityUserEntity driver) {
        if (driver == null) {
            return null;
        }
        if (driver.getNxCouWxNickName() != null && !driver.getNxCouWxNickName().trim().isEmpty()) {
            return driver.getNxCouWxNickName().trim();
        }
        if (driver.getNxCouWxPhone() != null && !driver.getNxCouWxPhone().trim().isEmpty()) {
            return driver.getNxCouWxPhone().trim();
        }
        if (driver.getNxCommunityUserId() != null) {
            return "司机#" + driver.getNxCommunityUserId();
        }
        return null;
    }

    public static Map<Integer, String> buildDriverNameMap(
            CommunityDispatchSandboxResult result,
            NxCommunityUserDao nxCommunityUserDao) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        Set<Integer> driverUserIds = new HashSet<Integer>();
        if (result == null) {
            return map;
        }
        if (result.getAvailableDrivers() != null) {
            for (NxCommunityUserEntity driver : result.getAvailableDrivers()) {
                registerDriver(map, driverUserIds, driver);
            }
        }
        collectRouteDriverUserIds(result.getConfirmedRoutes(), driverUserIds);
        collectRouteDriverUserIds(result.getSuggestedRoutes(), driverUserIds);
        collectRouteDriverUserIds(result.resolveSandboxRoutes(), driverUserIds);

        for (Integer driverUserId : driverUserIds) {
            if (driverUserId == null) {
                continue;
            }
            String existing = map.get(driverUserId);
            if (existing != null && !existing.trim().isEmpty()) {
                continue;
            }
            if (nxCommunityUserDao != null) {
                registerDriver(map, driverUserIds, nxCommunityUserDao.queryObject(driverUserId));
            }
            if (!map.containsKey(driverUserId) || isBlank(map.get(driverUserId))) {
                map.put(driverUserId, "司机#" + driverUserId);
            }
        }
        return map;
    }

    private static void registerDriver(
            Map<Integer, String> map,
            Set<Integer> driverUserIds,
            NxCommunityUserEntity driver) {
        if (driver == null || driver.getNxCommunityUserId() == null) {
            return;
        }
        driverUserIds.add(driver.getNxCommunityUserId());
        String name = resolveDisplayName(driver);
        if (!isBlank(name)) {
            map.put(driver.getNxCommunityUserId(), name);
        }
    }

    private static void collectRouteDriverUserIds(
            List<NxCommunityDispatchDriverRouteEntity> routes,
            Set<Integer> driverUserIds) {
        if (routes == null) {
            return;
        }
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            if (route != null && route.getNxCddrDriverUserId() != null) {
                driverUserIds.add(route.getNxCddrDriverUserId());
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
