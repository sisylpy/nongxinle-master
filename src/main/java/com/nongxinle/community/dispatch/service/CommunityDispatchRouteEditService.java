package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.dto.CommunityDriverRouteEditConfirmRequest;
import com.nongxinle.community.dispatch.dto.CommunityDriverRouteEditPageRequest;
import com.nongxinle.community.dispatch.dto.CommunitySandboxStopConfirmRequest;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCommunityDispatchStopDao;
import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchMapOverviewBuilder;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchPageViewAdapter;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchRouteEditActionHelper;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchRouteMetricsHelper;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchRouteTimelineBuilder;
import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.view.DispatchRouteCard;
import com.nongxinle.dispatch.core.view.DispatchTimelineItem;
import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.entity.NxCommunityDispatchStopItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 社区派单 M1 路线编辑：page / preview / confirm（调整站点顺序）。 */
@Service
public class CommunityDispatchRouteEditService {

    @Autowired
    private CommunityDispatchComputeService communityDispatchComputeService;
    @Autowired
    private CommunityDispatchPageAssembler communityDispatchPageAssembler;
    @Autowired
    private CommunityDispatchRouteTimelineBuilder communityDispatchRouteTimelineBuilder;
    @Autowired
    private CommunityDispatchMapOverviewBuilder communityDispatchMapOverviewBuilder;
    @Autowired
    private NxCommunityDispatchStopDao nxCommunityDispatchStopDao;
    @Autowired
    private NxCustomerUserAddressDao nxCustomerUserAddressDao;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;
    @Autowired
    private CommunityDispatchConfirmService communityDispatchConfirmService;

    public Map<String, Object> buildPage(CommunityDriverRouteEditPageRequest request) {
        validatePageRequest(request);
        String sourcePage = normalizeSourcePage(request.getSourcePage());
        CommunityDispatchSandboxResult result = computeForRouteEdit(
                request.getCommunityId(), request.getRouteDate(), sourcePage);
        NxCommunityDispatchDriverRouteEntity route = requireRoute(result, request.getDriverUserId(), sourcePage);
        List<String> stopKeys = resolveStopKeys(request.getStopKeys(), result, route);
        return wrapResponse(result, route, stopKeys, sourcePage);
    }

    public Map<String, Object> preview(CommunityDriverRouteEditPageRequest request) {
        validatePageRequest(request);
        if (request.getStopKeys() == null || request.getStopKeys().isEmpty()) {
            throw new IllegalArgumentException("stopKeys 不能为空");
        }
        String sourcePage = normalizeSourcePage(request.getSourcePage());
        CommunityDispatchSandboxResult result = computeForRouteEdit(
                request.getCommunityId(), request.getRouteDate(), sourcePage);
        NxCommunityDispatchDriverRouteEntity route = requireRoute(result, request.getDriverUserId(), sourcePage);
        List<String> stopKeys = resolveStopKeys(request.getStopKeys(), result, route);
        NxCommunityDispatchDriverRouteEntity editedRoute = CommunityDispatchRouteEditStopPool.buildRouteFromStopKeys(
                result, route, stopKeys, nxCustomerUserAddressDao);
        return wrapResponse(result, editedRoute, stopKeys, sourcePage);
    }

    @Transactional
    public Map<String, Object> confirm(CommunityDriverRouteEditConfirmRequest request) {
        if (request == null || request.getCommunityId() == null || request.getDriverUserId() == null) {
            throw new IllegalArgumentException("communityId / driverUserId 不能为空");
        }
        if (request.getStopKeys() == null) {
            throw new IllegalArgumentException("stopKeys 不能为空");
        }
        String sourcePage = normalizeSourcePage(request.getSourcePage());
        CommunityDispatchSandboxResult result = computeForRouteEdit(
                request.getCommunityId(), request.getRouteDate(), sourcePage);
        NxCommunityDispatchDriverRouteEntity route = requireRoute(result, request.getDriverUserId(), sourcePage);
        List<String> stopKeys = new ArrayList<String>(request.getStopKeys());
        Map<String, NxCommunityDispatchStopEntity> stopByKey =
                CommunityDispatchRouteEditStopPool.indexByStopKey(result, nxCustomerUserAddressDao);

        applyStopRemovals(route, stopKeys);
        confirmRemainingSandboxStops(request, stopByKey, stopKeys);

        result = computeForRouteEdit(request.getCommunityId(), request.getRouteDate(), sourcePage);
        NxCommunityDispatchDriverRouteEntity updatedRoute = findRoute(
                result, request.getDriverUserId(), sourcePage);
        int activeDbStopCount = 0;
        if (updatedRoute != null && updatedRoute.getNxCommunityDispatchDriverRouteId() != null) {
            if (!stopKeys.isEmpty()) {
                persistStopOrder(updatedRoute, resolvePersistedStopKeys(updatedRoute, stopKeys));
            }
            activeDbStopCount = nxCommunityDispatchStopDao.countActiveByDriverRouteId(
                    updatedRoute.getNxCommunityDispatchDriverRouteId());
        }

        result = computeForRouteEdit(request.getCommunityId(), request.getRouteDate(), sourcePage);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(sourcePage)) {
            data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                    result, CommunityDispatchPageViewAdapter.AdapterOptions.defaults()));
        } else {
            data.put("pageViewModel", communityDispatchPageAssembler.assemble(result));
        }
        data.put("exitedLoading", stopKeys.isEmpty() || activeDbStopCount <= 0);
        return data;
    }

    private Map<String, Object> wrapResponse(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity route,
            List<String> stopKeys,
            String sourcePage) {
        Map<Integer, String> driverNames = CommunityDispatchDriverNameHelper.buildDriverNameMap(
                result, nxCommunityUserDao);
        DispatchRouteCard card = new DispatchRouteCard();
        card.setDriverUserId(route.getNxCddrDriverUserId());
        card.setDriverName(driverNames.get(route.getNxCddrDriverUserId()));
        card.setDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
        DispatchPageMode pageMode = resolveDispatchPageMode(sourcePage);
        List<DispatchTimelineItem> timeline = communityDispatchRouteTimelineBuilder.build(
                result, route, pageMode,
                CommunityDispatchPageViewAdapter.AdapterOptions.defaults());
        card.setTimeline(timeline);
        card.setStopCount(route.getStops() != null ? route.getStops().size() : 0);
        CommunityDispatchRouteMetricsHelper.applyRouteMetrics(card, result, route);

        Map<String, Object> vm = new LinkedHashMap<String, Object>();
        boolean loadingPage = CommunityDispatchConstants.PAGE_MODE_LOADING.equals(
                normalizeSourcePage(sourcePage));
        vm.put("pageTitle", "编辑司机路线");
        vm.put("manualDispatchMode", Boolean.FALSE);
        vm.put("removeStopMode", loadingPage ? "REMOTE" : "LOCAL");
        vm.put("routeDate", result.getRouteDate());
        vm.put("communityId", result.getCommunityId());
        vm.put("driver", buildDriverSummary(card));
        vm.put("routeStops", buildRouteStopDtos(route));
        vm.put("stopKeys", new ArrayList<String>(stopKeys));
        List<NxCommunityDispatchStopEntity> addableStopEntities = CommunityDispatchRouteEditStopPool.listAddableStops(
                result, route, nxCustomerUserAddressDao);
        List<Map<String, Object>> addableStops = buildAddableStopDtos(result, route, addableStopEntities);
        vm.put("availableCustomers", addableStops);
        vm.put("addableStops", addableStops);
        vm.put("timeline", timelineToMaps(timeline));
        vm.put("mapOverview", communityDispatchMapOverviewBuilder.buildRouteEdit(
                result, route, addableStopEntities, driverNames));
        vm.put("actions", buildActions(route, stopKeys, sourcePage));
        vm.put("bottomHint", loadingPage
                ? "装车中移除将立即生效；调整顺序请先「重新试算」"
                : "调整顺序后请点击「重新试算」更新预计时间");

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", vm);
        return data;
    }

    private Map<String, Object> buildDriverSummary(DispatchRouteCard card) {
        Map<String, Object> driver = new LinkedHashMap<String, Object>();
        driver.put("driverUserId", card.getDriverUserId());
        driver.put("driverName", card.getDriverName());
        driver.put("customerStopCount", card.getStopCount());
        driver.put("plannedDepartLabel", card.getPlannedDepartLabel());
        driver.put("firstStopPlannedArrivalTimeLabel", card.getFirstStopPlannedArrivalTimeLabel());
        driver.put("firstStopArrivalStatusLabel", card.getFirstStopArrivalStatusLabel());
        driver.put("firstStopArrivalStatusTone", card.getFirstStopArrivalStatusTone());
        driver.put("plannedReturnLabel", card.getPlannedReturnLabel());
        driver.put("totalRoundTripDistanceText", card.getTotalRoundTripDistanceText());
        driver.put("totalRoundTripDurationText", card.getTotalRoundTripDurationText());
        return driver;
    }

    private List<Map<String, Object>> buildAddableStopDtos(
            CommunityDispatchSandboxResult result,
            NxCommunityDispatchDriverRouteEntity currentRoute,
            List<NxCommunityDispatchStopEntity> addable) {
        List<Map<String, Object>> dtos = new ArrayList<Map<String, Object>>();
        Map<Integer, String> driverNames = CommunityDispatchDriverNameHelper.buildDriverNameMap(
                result, nxCommunityUserDao);
        if (addable == null) {
            addable = Collections.emptyList();
        }
        for (NxCommunityDispatchStopEntity stop : addable) {
            if (stop == null) {
                continue;
            }
            String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
            if (stopKey == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("stopKey", stopKey);
            item.put("addressId", stop.getNxCdsAddressId());
            item.put("customerName", resolveCustomerName(stop));
            item.put("goodsSummary", buildGoodsSummary(stop.getItems()));
            if (stop.getNxCdsAssignedDriverUserId() != null
                    && currentRoute != null
                    && !stop.getNxCdsAssignedDriverUserId().equals(currentRoute.getNxCddrDriverUserId())) {
                String otherName = driverNames.get(stop.getNxCdsAssignedDriverUserId());
                item.put("constraintHint", "当前建议司机："
                        + (otherName != null ? otherName : ("#" + stop.getNxCdsAssignedDriverUserId())));
            }
            dtos.add(item);
        }
        return dtos;
    }

    private List<Map<String, Object>> buildRouteStopDtos(NxCommunityDispatchDriverRouteEntity route) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (route.getStops() == null) {
            return list;
        }
        int seq = 1;
        for (NxCommunityDispatchStopEntity stop : route.getStops()) {
            if (stop == null) {
                continue;
            }
            String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
            if (stopKey == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("stopKey", stopKey);
            item.put("seq", seq++);
            if (stop.getNxCommunityDispatchStopId() != null) {
                item.put("deliveryStopId", stop.getNxCommunityDispatchStopId());
            }
            if (CommunityDispatchConstants.isSandboxSimulatedStop(stop)) {
                item.put("simulated", Boolean.TRUE);
                item.put("sandboxStopKey", CommunityDispatchConstants.sandboxKeyForAddress(stop.getNxCdsAddressId()));
                item.put("addressId", stop.getNxCdsAddressId());
            }
            item.put("customerName", resolveCustomerName(stop));
            item.put("goodsSummary", buildGoodsSummary(stop.getItems()));
            item.put("removeConfirmTitle", "移除门店");
            item.put("removeConfirmMessage", "确认从路线中移除该门店？移除后将回到待分派池。");
            list.add(item);
        }
        return list;
    }

    private Map<String, Object> buildActions(
            NxCommunityDispatchDriverRouteEntity route,
            List<String> stopKeys,
            String sourcePage) {
        Map<String, Object> actions = new LinkedHashMap<String, Object>();
        actions.put("editPagePath", CommunityDispatchRouteEditActionHelper.EDIT_PAGE_PATH);
        actions.put("previewPath", CommunityDispatchRouteEditActionHelper.PREVIEW_PATH);
        actions.put("confirmPath", CommunityDispatchRouteEditActionHelper.CONFIRM_PATH);
        boolean editable = isRouteEditable(route);
        int stopCount = stopKeys != null ? stopKeys.size() : 0;
        boolean loadingPage = CommunityDispatchConstants.PAGE_MODE_LOADING.equals(
                normalizeSourcePage(sourcePage));
        boolean confirmEnabled = editable && stopCount > 0;
        actions.put("confirmEnabled", confirmEnabled);
        if (!confirmEnabled) {
            if (stopCount <= 0 && loadingPage) {
                actions.put("confirmDisabledReason", "装车中请使用「移除」退回待分派");
            } else if (stopCount <= 0) {
                actions.put("confirmDisabledReason", "请至少保留一个客户");
            } else if (!editable) {
                actions.put("confirmDisabledReason", "当前路线不可编辑");
            }
        } else {
            actions.put("confirmDisabledReason", "");
        }
        return actions;
    }

    private boolean isRouteEditable(NxCommunityDispatchDriverRouteEntity route) {
        String status = route.getNxCddrRouteStatus();
        return !CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(status)
                && !CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(status);
    }

    private void persistStopOrder(NxCommunityDispatchDriverRouteEntity route, List<String> stopKeys) {
        if (!isRouteEditable(route)) {
            throw new IllegalStateException("当前路线不可编辑");
        }
        Map<Integer, NxCommunityDispatchStopEntity> stopById = indexStops(route.getStops());
        int seq = 1;
        for (String stopKey : stopKeys) {
            Integer stopId = CommunityDispatchRouteEditActionHelper.parseStopId(stopKey);
            if (stopId == null || !stopById.containsKey(stopId)) {
                throw new IllegalArgumentException("无效 stopKey: " + stopKey);
            }
            NxCommunityDispatchStopEntity update = new NxCommunityDispatchStopEntity();
            update.setNxCommunityDispatchStopId(stopId);
            update.setNxCdsRouteSeq(seq++);
            nxCommunityDispatchStopDao.update(update);
        }
    }

    private List<String> resolveStopKeys(List<String> requestedKeys,
                                         CommunityDispatchSandboxResult result,
                                         NxCommunityDispatchDriverRouteEntity route) {
        Map<String, NxCommunityDispatchStopEntity> stopByKey =
                CommunityDispatchRouteEditStopPool.indexByStopKey(result, nxCustomerUserAddressDao);
        if (requestedKeys != null && !requestedKeys.isEmpty()) {
            List<String> keys = new ArrayList<String>();
            for (String stopKey : requestedKeys) {
                if (stopKey == null || !stopByKey.containsKey(stopKey)) {
                    throw new IllegalArgumentException("无效 stopKey: " + stopKey);
                }
                keys.add(stopKey);
            }
            return keys;
        }
        List<String> keys = new ArrayList<String>();
        if (route.getStops() != null) {
            for (NxCommunityDispatchStopEntity stop : route.getStops()) {
                String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
                if (stopKey != null) {
                    keys.add(stopKey);
                }
            }
        }
        return keys;
    }

    private void confirmRemainingSandboxStops(CommunityDriverRouteEditConfirmRequest request,
                                              Map<String, NxCommunityDispatchStopEntity> stopByKey,
                                              List<String> stopKeys) {
        if (stopKeys == null || stopKeys.isEmpty()) {
            return;
        }
        for (String stopKey : stopKeys) {
            if (!CommunityDispatchRouteEditActionHelper.isSandboxStopKey(stopKey)) {
                continue;
            }
            NxCommunityDispatchStopEntity simulated = stopByKey.get(stopKey);
            if (simulated == null || !CommunityDispatchConstants.isSandboxSimulatedStop(simulated)) {
                continue;
            }
            CommunitySandboxStopConfirmRequest confirmRequest = new CommunitySandboxStopConfirmRequest();
            confirmRequest.setCommunityId(request.getCommunityId());
            confirmRequest.setRouteDate(request.getRouteDate());
            confirmRequest.setDriverUserId(request.getDriverUserId());
            confirmRequest.setOperatorUserId(request.getOperatorUserId());
            confirmRequest.setSandboxStopKey(stopKey);
            confirmRequest.setAddressId(simulated.getNxCdsAddressId());
            confirmRequest.setOrderIds(collectOrderIds(simulated.getItems()));
            communityDispatchConfirmService.confirmStop(confirmRequest);
        }
    }

    private List<String> resolvePersistedStopKeys(NxCommunityDispatchDriverRouteEntity route,
                                                   List<String> stopKeys) {
        List<String> persisted = new ArrayList<String>();
        if (stopKeys == null || route == null || route.getStops() == null) {
            return persisted;
        }
        Map<Integer, NxCommunityDispatchStopEntity> byAddress = indexStopsByAddress(route.getStops());
        for (String stopKey : stopKeys) {
            if (CommunityDispatchRouteEditActionHelper.isDbStopKey(stopKey)) {
                persisted.add(stopKey);
                continue;
            }
            Integer addressId = CommunityDispatchRouteEditActionHelper.parseAddressId(stopKey);
            NxCommunityDispatchStopEntity stop = addressId != null ? byAddress.get(addressId) : null;
            if (stop != null && stop.getNxCommunityDispatchStopId() != null) {
                persisted.add(CommunityDispatchRouteEditActionHelper.stopKey(stop.getNxCommunityDispatchStopId()));
            }
        }
        return persisted;
    }

    private List<Integer> collectOrderIds(List<NxCommunityDispatchStopItemEntity> items) {
        List<Integer> ids = new ArrayList<Integer>();
        if (items == null) {
            return ids;
        }
        for (NxCommunityDispatchStopItemEntity item : items) {
            if (item != null && item.getNxCdsiCommunityOrderId() != null) {
                ids.add(item.getNxCdsiCommunityOrderId());
            }
        }
        return ids;
    }

    private void applyStopRemovals(NxCommunityDispatchDriverRouteEntity route, List<String> stopKeys) {
        if (route == null || route.getStops() == null) {
            return;
        }
        Set<String> keptStopKeys = new HashSet<String>();
        if (stopKeys != null) {
            keptStopKeys.addAll(stopKeys);
        }
        for (NxCommunityDispatchStopEntity stop : route.getStops()) {
            if (stop == null || stop.getNxCommunityDispatchStopId() == null) {
                continue;
            }
            String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
            if (stopKey != null && !keptStopKeys.contains(stopKey)) {
                communityDispatchConfirmService.removeStopFromRoute(stop.getNxCommunityDispatchStopId());
            }
        }
    }

    private NxCommunityDispatchDriverRouteEntity requireRoute(
            CommunityDispatchSandboxResult result,
            Integer driverUserId,
            String sourcePage) {
        NxCommunityDispatchDriverRouteEntity route = findRoute(result, driverUserId, sourcePage);
        if (route == null) {
            throw new IllegalArgumentException("司机暂无路线");
        }
        return route;
    }

    private NxCommunityDispatchDriverRouteEntity findRoute(
            CommunityDispatchSandboxResult result,
            Integer driverUserId,
            String sourcePage) {
        List<NxCommunityDispatchDriverRouteEntity> routes = resolveRoutesForEdit(result, sourcePage);
        if (routes != null) {
            for (NxCommunityDispatchDriverRouteEntity route : routes) {
                if (route != null && driverUserId.equals(route.getNxCddrDriverUserId())) {
                    return route;
                }
            }
        }
        if (CommunityDispatchConstants.PAGE_MODE_SANDBOX.equals(normalizeSourcePage(sourcePage))
                && isOnDutyDriver(result, driverUserId)) {
            return createVirtualRoute(result, driverUserId);
        }
        return null;
    }

    private List<NxCommunityDispatchDriverRouteEntity> resolveRoutesForEdit(
            CommunityDispatchSandboxResult result,
            String sourcePage) {
        String mode = normalizeSourcePage(sourcePage);
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(mode)
                || CommunityDispatchConstants.PAGE_MODE_DELIVERY.equals(mode)) {
            return result.getConfirmedRoutes() != null
                    ? result.getConfirmedRoutes()
                    : Collections.<NxCommunityDispatchDriverRouteEntity>emptyList();
        }
        return result.resolveSandboxRoutes();
    }

    private CommunityDispatchSandboxResult computeForRouteEdit(
            Integer communityId,
            String routeDate,
            String sourcePage) {
        String mode = normalizeSourcePage(sourcePage);
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(mode)) {
            return communityDispatchComputeService.computeLoading(communityId, routeDate);
        }
        if (CommunityDispatchConstants.PAGE_MODE_DELIVERY.equals(mode)) {
            return communityDispatchComputeService.computeDelivery(communityId, routeDate);
        }
        return communityDispatchComputeService.computeSandbox(communityId, routeDate);
    }

    private String normalizeSourcePage(String sourcePage) {
        if (sourcePage == null || sourcePage.trim().isEmpty()) {
            return CommunityDispatchConstants.PAGE_MODE_SANDBOX;
        }
        return sourcePage.trim();
    }

    private DispatchPageMode resolveDispatchPageMode(String sourcePage) {
        String mode = normalizeSourcePage(sourcePage);
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(mode)) {
            return DispatchPageMode.LOADING;
        }
        if (CommunityDispatchConstants.PAGE_MODE_DELIVERY.equals(mode)) {
            return DispatchPageMode.DELIVERY;
        }
        return DispatchPageMode.DISPATCH_SANDBOX;
    }

    private boolean isOnDutyDriver(CommunityDispatchSandboxResult result, Integer driverUserId) {
        if (result == null || result.getAvailableDrivers() == null || driverUserId == null) {
            return false;
        }
        for (NxCommunityUserEntity driver : result.getAvailableDrivers()) {
            if (driver != null && driverUserId.equals(driver.getNxCommunityUserId())) {
                return true;
            }
        }
        return false;
    }

    private NxCommunityDispatchDriverRouteEntity createVirtualRoute(
            CommunityDispatchSandboxResult result,
            Integer driverUserId) {
        NxCommunityDispatchDriverRouteEntity route = new NxCommunityDispatchDriverRouteEntity();
        route.setNxCddrCommunityId(result.getCommunityId());
        route.setNxCddrDriverUserId(driverUserId);
        route.setNxCddrRouteStatus(CommunityDispatchConstants.ROUTE_STATUS_DRAFT);
        route.setStops(new ArrayList<NxCommunityDispatchStopEntity>());
        return route;
    }

    private void validatePageRequest(CommunityDriverRouteEditPageRequest request) {
        if (request == null || request.getCommunityId() == null || request.getDriverUserId() == null) {
            throw new IllegalArgumentException("communityId / driverUserId 不能为空");
        }
    }

    private Map<Integer, NxCommunityDispatchStopEntity> indexStops(List<NxCommunityDispatchStopEntity> stops) {
        Map<Integer, NxCommunityDispatchStopEntity> map = new HashMap<Integer, NxCommunityDispatchStopEntity>();
        if (stops == null) {
            return map;
        }
        for (NxCommunityDispatchStopEntity stop : stops) {
            if (stop != null && stop.getNxCommunityDispatchStopId() != null) {
                map.put(stop.getNxCommunityDispatchStopId(), stop);
            }
        }
        return map;
    }

    private Map<String, NxCommunityDispatchStopEntity> indexStopsByKey(List<NxCommunityDispatchStopEntity> stops) {
        Map<String, NxCommunityDispatchStopEntity> map = new LinkedHashMap<String, NxCommunityDispatchStopEntity>();
        if (stops == null) {
            return map;
        }
        for (NxCommunityDispatchStopEntity stop : stops) {
            String stopKey = CommunityDispatchRouteEditActionHelper.resolveStopKey(stop);
            if (stopKey != null) {
                map.put(stopKey, stop);
            }
        }
        return map;
    }

    private Map<Integer, NxCommunityDispatchStopEntity> indexStopsByAddress(List<NxCommunityDispatchStopEntity> stops) {
        Map<Integer, NxCommunityDispatchStopEntity> map = new HashMap<Integer, NxCommunityDispatchStopEntity>();
        if (stops == null) {
            return map;
        }
        for (NxCommunityDispatchStopEntity stop : stops) {
            if (stop != null && stop.getNxCdsAddressId() != null
                    && stop.getNxCommunityDispatchStopId() != null) {
                map.put(stop.getNxCdsAddressId(), stop);
            }
        }
        return map;
    }

    private NxCommunityDispatchDriverRouteEntity cloneRoute(NxCommunityDispatchDriverRouteEntity route) {
        NxCommunityDispatchDriverRouteEntity copy = new NxCommunityDispatchDriverRouteEntity();
        copy.setNxCommunityDispatchDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
        copy.setNxCddrDriverUserId(route.getNxCddrDriverUserId());
        copy.setNxCddrRouteStatus(route.getNxCddrRouteStatus());
        copy.setStops(route.getStops() != null
                ? new ArrayList<NxCommunityDispatchStopEntity>(route.getStops()) : new ArrayList<NxCommunityDispatchStopEntity>());
        return copy;
    }

    private NxCommunityDispatchStopEntity cloneStop(NxCommunityDispatchStopEntity stop) {
        NxCommunityDispatchStopEntity copy = new NxCommunityDispatchStopEntity();
        copy.setNxCommunityDispatchStopId(stop.getNxCommunityDispatchStopId());
        copy.setNxCdsAddressId(stop.getNxCdsAddressId());
        copy.setNxCdsCustomerName(stop.getNxCdsCustomerName());
        copy.setNxCdsAddressText(stop.getNxCdsAddressText());
        copy.setNxCdsLat(stop.getNxCdsLat());
        copy.setNxCdsLng(stop.getNxCdsLng());
        copy.setNxCdsRouteSeq(stop.getNxCdsRouteSeq());
        copy.setNxCdsStopStatus(stop.getNxCdsStopStatus());
        copy.setItems(stop.getItems());
        return copy;
    }

    private String resolveCustomerName(NxCommunityDispatchStopEntity stop) {
        if (stop.getNxCdsCustomerName() != null && !stop.getNxCdsCustomerName().trim().isEmpty()) {
            return stop.getNxCdsCustomerName();
        }
        if (stop.getNxCdsAddressText() != null && !stop.getNxCdsAddressText().trim().isEmpty()) {
            return stop.getNxCdsAddressText();
        }
        return "站点#" + stop.getNxCdsAddressId();
    }

    private String buildGoodsSummary(List<NxCommunityDispatchStopItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        if (items.size() == 1) {
            NxCommunityDispatchStopItemEntity item = items.get(0);
            if (item.getNxCdsiGoodsSummary() != null && !item.getNxCdsiGoodsSummary().trim().isEmpty()) {
                return item.getNxCdsiGoodsSummary();
            }
            return "订单#" + item.getNxCdsiCommunityOrderId();
        }
        NxCommunityDispatchStopItemEntity first = items.get(0);
        String firstSummary = first.getNxCdsiGoodsSummary() != null && !first.getNxCdsiGoodsSummary().trim().isEmpty()
                ? first.getNxCdsiGoodsSummary()
                : ("订单#" + first.getNxCdsiCommunityOrderId());
        return items.size() + " 单 · " + firstSummary;
    }

    private List<Map<String, Object>> timelineToMaps(List<DispatchTimelineItem> timeline) {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (DispatchTimelineItem item : timeline) {
            if (item == null) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<String, Object>();
            node.put("type", item.getType());
            node.put("seq", item.getRouteSeq());
            node.put("legText", item.getLegText());
            node.put("legRole", item.getLegRole());
            node.put("marker", item.getMarker());
            node.put("name", item.getName());
            node.put("timeRight", item.getTimeRight());
            node.put("customerName", item.getTitle());
            node.put("goodsSummary", item.getSubtitle());
            node.put("plannedArrivalLabel", item.getPlannedArrivalLabel());
            node.put("plannedDepartureLabel", item.getPlannedDepartureLabel());
            node.put("serviceDurationLabel", item.getServiceDurationLabel());
            node.put("deliveryStopId", item.getDeliveryStopId());
            putIfNotNull(node, "stopKey", item.getStopKey());
            putIfNotNull(node, "addressId", item.getAddressId());
            list.add(node);
        }
        return list;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
