package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dispatch.core.view.DispatchPrimaryAction;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

/** 社区派单路线编辑 routeEditAction 契约（对齐 boss-mini EDIT_DRIVER_ROUTE）。 */
public final class CommunityDispatchRouteEditActionHelper {

    public static final String ACTION_TYPE_EDIT_DRIVER_ROUTE = "EDIT_DRIVER_ROUTE";
    public static final String EDIT_PAGE_PATH = "api/nxcommunitydispatch/sandbox/driver-route-edit/page";
    public static final String PREVIEW_PATH = "api/nxcommunitydispatch/sandbox/driver-route-edit/preview";
    public static final String CONFIRM_PATH = "api/nxcommunitydispatch/sandbox/driver-route-edit/confirm";

    private CommunityDispatchRouteEditActionHelper() {
    }

    static DispatchPrimaryAction buildRouteEditAction(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route) {
        if (route == null || route.getNxCddrDriverUserId() == null) {
            return null;
        }
        String routeStatus = route.getNxCddrRouteStatus();
        if (CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(routeStatus)
                || CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(routeStatus)) {
            return disabledEditRoute("编辑路线", "配送中不可编辑路线");
        }
        DispatchPrimaryAction action = new DispatchPrimaryAction();
        action.setActionType(ACTION_TYPE_EDIT_DRIVER_ROUTE);
        action.setLabel("编辑路线");
        action.setEnabled(true);
        action.setPayload(buildPayload(result, route));
        return action;
    }

    static DispatchPrimaryAction disabledEditRoute(String label, String reason) {
        DispatchPrimaryAction action = new DispatchPrimaryAction();
        action.setActionType(ACTION_TYPE_EDIT_DRIVER_ROUTE);
        action.setLabel(label != null ? label : "编辑路线");
        action.setEnabled(false);
        action.setDisabledReason(reason != null ? reason : "当前不可编辑");
        action.setPayload(new LinkedHashMap<String, Object>());
        return action;
    }

    static DispatchPrimaryAction buildIdleDriverRouteEditAction(
            CommunityDispatchSandboxResult result,
            Integer driverUserId) {
        if (result == null || driverUserId == null) {
            return null;
        }
        NxCommunityDispatchDriverRouteEntity virtual = new NxCommunityDispatchDriverRouteEntity();
        virtual.setNxCddrCommunityId(result.getCommunityId());
        virtual.setNxCddrDriverUserId(driverUserId);
        virtual.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_DRAFT);
        virtual.setStops(new ArrayList<NxCommunityDispatchStopEntity>());
        return buildRouteEditAction(result, virtual);
    }

    static Map<String, Object> buildPayload(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("communityId", result.getCommunityId());
        payload.put("routeDate", result.getRouteDate());
        payload.put("driverUserId", route.getNxCddrDriverUserId());
        payload.put("driverRouteId", route.getNxCommunityDispatchDriverRouteId());
        String sourcePage = result.getPageMode();
        if (sourcePage == null || sourcePage.trim().isEmpty()) {
            sourcePage = CommunityDispatchConstants.PAGE_MODE_SANDBOX;
        }
        payload.put("sourcePage", sourcePage);
        payload.put("editPagePath", EDIT_PAGE_PATH);
        payload.put("previewPath", PREVIEW_PATH);
        payload.put("confirmPath", CONFIRM_PATH);
        return payload;
    }

    public static String stopKey(Integer stopId) {
        return stopId != null ? ("STOP-" + stopId) : null;
    }

    public static String resolveStopKey(com.nongxinle.entity.NxCommunityDispatchStopEntity stop) {
        if (stop == null) {
            return null;
        }
        if (stop.getNxCommunityDispatchStopId() != null) {
            return stopKey(stop.getNxCommunityDispatchStopId());
        }
        if (CommunityDispatchConstants.isSandboxSimulatedStop(stop)
                && stop.getNxCdsAddressId() != null) {
            return CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId());
        }
        return null;
    }

    public static boolean isDbStopKey(String stopKey) {
        return stopKey != null && stopKey.startsWith("STOP-");
    }

    public static boolean isSandboxStopKey(String stopKey) {
        return stopKey != null && stopKey.startsWith(CommunityDispatchConstants.SANDBOX_KEY_PREFIX);
    }

    public static Integer parseStopId(String stopKey) {
        if (stopKey == null || !stopKey.startsWith("STOP-")) {
            return null;
        }
        try {
            return Integer.valueOf(stopKey.substring("STOP-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Integer parseAddressId(String stopKey) {
        if (!isSandboxStopKey(stopKey)) {
            return null;
        }
        try {
            return Integer.valueOf(stopKey.substring(CommunityDispatchConstants.SANDBOX_KEY_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
