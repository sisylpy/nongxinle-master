package com.nongxinle.dispatch.adapter.community;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.community.dispatch.service.CommunityDispatchDriverNameHelper;
import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.dispatch.core.domain.DispatchPageMode;
import com.nongxinle.dispatch.core.domain.DispatchTenantRef;
import com.nongxinle.dispatch.core.domain.DispatchTenantType;
import com.nongxinle.dispatch.core.view.*;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * nxCommunity M1 → dispatch-core {@link DispatchPageViewModel} adapter。
 * 输出结构与 boss-mini / Nx 三页契约对齐（sections.cards + mapOverview）。
 */
@Component
public class CommunityDispatchPageViewAdapter {

    private static final String SECTION_SUGGESTED = "SUGGESTED_DRIVER_ROUTES";
    private static final String SECTION_UNASSIGNED = "UNASSIGNED";
    @Autowired
    private CommunityDispatchMapOverviewBuilder communityDispatchMapOverviewBuilder;
    @Autowired
    private CommunityDispatchRouteTimelineBuilder communityDispatchRouteTimelineBuilder;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;
    @Autowired
    private NxCustomerUserAddressDao nxCustomerUserAddressDao;

    public DispatchPageViewModel assemble(CommunityDispatchSandboxResult result) {
        return assemble(result, AdapterOptions.defaults());
    }

    public DispatchPageViewModel assemble(CommunityDispatchSandboxResult result, AdapterOptions options) {
        if (result == null) {
            return new DispatchPageViewModel();
        }
        AdapterOptions opts = options != null ? options : AdapterOptions.defaults();
        DispatchPageMode pageMode = resolvePageMode(result.getPageMode());
        Map<Integer, String> driverNames = CommunityDispatchDriverNameHelper.buildDriverNameMap(
                result, nxCommunityUserDao);
        Map<Integer, Integer> assignedStopCountByDriver = buildAssignedStopCount(result, pageMode);

        DispatchPageViewModel vm = new DispatchPageViewModel();
        vm.setPageMode(pageMode);
        vm.setTenant(DispatchTenantRef.of(DispatchTenantType.NX_COMMUNITY, result.getCommunityId()));
        vm.setRouteDate(result.getRouteDate());
        vm.setPageHeader(buildHeader(result, pageMode));
        vm.setTopMetrics(buildTopMetrics(result));
        vm.setAvailableDrivers(pageMode == DispatchPageMode.LOADING
                ? Collections.<DispatchPageViewModel.DispatchAvailableDriver>emptyList()
                : buildAvailableDrivers(result, assignedStopCountByDriver));
        vm.setMapOverview(communityDispatchMapOverviewBuilder.build(result, pageMode, driverNames));
        vm.setSections(buildSections(result, pageMode, driverNames, opts));
        applyDriverTerminalDepartBar(vm, result, pageMode, opts);
        return vm;
    }

    private DispatchPageMode resolvePageMode(String pageMode) {
        if (CommunityDispatchConstants.PAGE_MODE_LOADING.equals(pageMode)) {
            return DispatchPageMode.LOADING;
        }
        if (CommunityDispatchConstants.PAGE_MODE_DELIVERY.equals(pageMode)) {
            return DispatchPageMode.DELIVERY;
        }
        return DispatchPageMode.DISPATCH_SANDBOX;
    }

    private DispatchPageViewModel.DispatchPageHeader buildHeader(
            CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        DispatchPageViewModel.DispatchPageHeader header = new DispatchPageViewModel.DispatchPageHeader();
        header.setTitle(resolveTitle(pageMode));
        header.setRouteDateLabel(result.getRouteDate());
        DispatchPageViewModel.DispatchPageHeaderProgress progress =
                new DispatchPageViewModel.DispatchPageHeaderProgress();
        progress.setMainLine(buildStatusLabel(result, pageMode));
        int stopCount = countSandboxCustomerStops(result, pageMode);
        progress.setHighlightText(stopCount > 0 ? String.valueOf(stopCount) : "0");
        header.setProgress(progress);
        return header;
    }

    private String resolveTitle(DispatchPageMode pageMode) {
        if (pageMode == DispatchPageMode.LOADING) {
            return "装车中";
        }
        if (pageMode == DispatchPageMode.DELIVERY) {
            return "配送中";
        }
        return "今日派单";
    }

    private String buildStatusLabel(CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX) {
            int stopCount = countSandboxCustomerStops(result, pageMode);
            if (stopCount <= 0) {
                return "暂无待确认站点";
            }
            return "待确认 " + stopCount + " 个客户站点";
        }
        if (pageMode == DispatchPageMode.LOADING) {
            return "装车中";
        }
        return "配送中";
    }

    private int countSandboxCustomerStops(CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        int count = 0;
        List<NxCommunityDispatchDriverRouteEntity> routes = resolveDisplayRoutes(result, pageMode);
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            if (route != null && route.getStops() != null) {
                count += route.getStops().size();
            }
        }
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX && result.getUnassignedStopGroups() != null) {
            count += result.getUnassignedStopGroups().size();
        }
        return count;
    }

    private List<NxCommunityDispatchDriverRouteEntity> resolveDisplayRoutes(
            CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX) {
            return result.resolveSandboxRoutes();
        }
        return result.getConfirmedRoutes() != null
                ? result.getConfirmedRoutes() : Collections.<NxCommunityDispatchDriverRouteEntity>emptyList();
    }

    private DispatchPageViewModel.DispatchTopMetrics buildTopMetrics(CommunityDispatchSandboxResult result) {
        DispatchPageViewModel.DispatchTopMetrics metrics = new DispatchPageViewModel.DispatchTopMetrics();
        int unassigned = result.getUnassignedStopGroups() != null
                ? result.getUnassignedStopGroups().size() : 0;
        List<NxCommunityDispatchDriverRouteEntity> routes = result.resolveSandboxRoutes();
        int routeStopCount = 0;
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            if (route != null && route.getStops() != null) {
                routeStopCount += route.getStops().size();
            }
        }
        metrics.setUnassignedStopCount(unassigned);
        metrics.setAssignedStopCount(routeStopCount);
        metrics.setActiveRouteCount(routes.size());
        metrics.setAvailableDriverCount(result.getAvailableDrivers() != null
                ? result.getAvailableDrivers().size() : 0);
        Map<String, Object> extra = new LinkedHashMap<String, Object>();
        extra.put("eligibleOrderCount", result.getEligibleOrders() != null ? result.getEligibleOrders().size() : 0);
        extra.put("confirmedStopCount", result.getConfirmedStops() != null ? result.getConfirmedStops().size() : 0);
        extra.put("customerStopCount", routeStopCount + unassigned);
        metrics.setExtra(extra);
        return metrics;
    }

    private List<DispatchPageViewModel.DispatchPageSection> buildSections(
            CommunityDispatchSandboxResult result,
            DispatchPageMode pageMode,
            Map<Integer, String> driverNames,
            AdapterOptions opts) {
        List<DispatchPageViewModel.DispatchPageSection> sections = new ArrayList<DispatchPageViewModel.DispatchPageSection>();
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX) {
            appendSection(sections, SECTION_SUGGESTED, "建议派车路线", null,
                    buildRouteCards(result, pageMode, driverNames, opts));
            appendSection(sections, SECTION_UNASSIGNED, "待分配客户",
                    "以下客户暂未分配司机，可人工调度",
                    buildUnassignedCards(result));
        } else if (pageMode == DispatchPageMode.LOADING) {
            appendSection(sections, "loading", null, null,
                    buildRouteCards(result, pageMode, driverNames, opts));
        } else {
            appendSection(sections, "delivery", null, null,
                    buildRouteCards(result, pageMode, driverNames, opts));
        }
        return sections;
    }

    private void appendSection(
            List<DispatchPageViewModel.DispatchPageSection> sections,
            String sectionKey,
            String title,
            String description,
            List<DispatchSectionCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        DispatchPageViewModel.DispatchPageSection section = new DispatchPageViewModel.DispatchPageSection();
        section.setSectionKey(sectionKey);
        section.setTitle(title);
        section.setDescription(description);
        section.setCards(new ArrayList<DispatchSectionCard>(cards));
        sections.add(section);
    }

    private List<DispatchSectionCard> buildUnassignedCards(CommunityDispatchSandboxResult result) {
        List<DispatchSectionCard> cards = new ArrayList<DispatchSectionCard>();
        if (result.getUnassignedStopGroups() == null) {
            return cards;
        }
        for (Map.Entry<Integer, List<NxCommunityOrdersEntity>> entry : result.getUnassignedStopGroups().entrySet()) {
            Integer addressId = entry.getKey();
            List<NxCommunityOrdersEntity> orders = entry.getValue();
            if (orders == null || orders.isEmpty()) {
                continue;
            }
            String sandboxStopKey = CommunityDispatchConstants.sandboxKeyForAddress(addressId);
            NxCustomerUserAddressEntity address = nxCustomerUserAddressDao.queryObject(addressId);
            DispatchStopCard card = new DispatchStopCard();
            card.setCardType("UNASSIGNED_CUSTOMER");
            card.setCardKey("unassigned_" + sandboxStopKey);
            card.setBadgeLabel("未分配");
            card.setDriverLabel("无司机");
            card.setSandboxStopKey(sandboxStopKey);
            card.setAddressId(addressId);
            card.setOrderCount(orders.size());
            card.setOrderIds(collectOrderIds(orders));
            card.setSimulated(true);
            CommunityDispatchStopPresentationHelper.applyUnassignedStopPresentation(
                    card, address, orders, result);
            cards.add(card);
        }
        return cards;
    }

    private List<DispatchSectionCard> buildRouteCards(
            CommunityDispatchSandboxResult result,
            DispatchPageMode pageMode,
            Map<Integer, String> driverNames,
            AdapterOptions opts) {
        List<DispatchSectionCard> cards = new ArrayList<DispatchSectionCard>();
        List<NxCommunityDispatchDriverRouteEntity> routes = resolveDisplayRoutes(result, pageMode);
        if (routes.isEmpty()) {
            return cards;
        }
        int idx = 0;
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            if ((pageMode == DispatchPageMode.LOADING || pageMode == DispatchPageMode.DELIVERY)
                    && (route.getStops() == null || route.getStops().isEmpty())) {
                continue;
            }
            DispatchRouteCard card = new DispatchRouteCard();
            card.setCardType("DRIVER_ROUTE");
            card.setCardKey("route_" + (route.getNxCommunityDispatchDriverRouteId() != null
                    ? route.getNxCommunityDispatchDriverRouteId() : route.getNxCddrDriverUserId()));
            card.setDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
            card.setDriverUserId(route.getNxCddrDriverUserId());
            card.setDriverName(driverNames.get(route.getNxCddrDriverUserId()));
            if (pageMode == DispatchPageMode.DISPATCH_SANDBOX) {
                card.setRouteStatusLabel("建议路线");
                card.setDriverStatusTone("suggested");
                card.setSimulated(route.getNxCommunityDispatchDriverRouteId() == null);
            } else {
                card.setRouteStatusLabel(routeStatusLabel(route.getNxCddrRouteStatus()));
                card.setSimulated(false);
            }
            card.setRouteStatus(route.getNxCddrRouteStatus());

            List<DispatchTimelineItem> timeline = communityDispatchRouteTimelineBuilder.build(
                    result, route, pageMode, opts);
            card.setTimeline(timeline);
            card.setStopCount(countTimelineStops(timeline));
            CommunityDispatchRouteMetricsHelper.applyRouteMetrics(card, result, route);
            if (pageMode == DispatchPageMode.DELIVERY) {
                CommunityDispatchRouteMetricsHelper.applyDeliveryExecutionMetrics(card, result, route);
            }
            if (pageMode == DispatchPageMode.DISPATCH_SANDBOX || pageMode == DispatchPageMode.LOADING) {
                card.setRouteEditAction(CommunityDispatchRouteEditActionHelper.buildRouteEditAction(result, route));
            }

            if (pageMode == DispatchPageMode.LOADING
                    && route.getNxCddrActualDepartAt() == null
                    && !opts.isDriverDeliveryMode()) {
                card.setShowDepartAction(true);
                card.setPrimaryAction(buildDepartAction(result, route.getNxCddrDriverUserId()));
            }

            if (pageMode == DispatchPageMode.DELIVERY) {
                card.setRouteStatusLabel("配送中");
                card.setDriverStatusTone("COMPLETED".equals(route.getNxCddrRouteStatus()) ? "muted" : "ok");
                int done = 0;
                int total = route.getStops() != null ? route.getStops().size() : 0;
                if (route.getStops() != null) {
                    for (NxCommunityDispatchStopEntity stop : route.getStops()) {
                        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(stop.getNxCdsStopStatus())) {
                            done++;
                        }
                    }
                }
                card.setDeliveredCount(done);
                card.setDeliveryProgressPercent(total > 0 ? Math.round(done * 100f / total) : 0);
                card.setDeliveryProgressLine(done + "/" + total + " 站已送达");
            }

            cards.add(card);
            idx++;
        }
        return cards;
    }

    private int countTimelineStops(List<DispatchTimelineItem> timeline) {
        if (timeline == null) {
            return 0;
        }
        int count = 0;
        for (DispatchTimelineItem item : timeline) {
            if (item != null && "stop".equals(item.getType())) {
                count++;
            }
        }
        return count;
    }

    private void applyDriverTerminalDepartBar(
            DispatchPageViewModel vm,
            CommunityDispatchSandboxResult result,
            DispatchPageMode pageMode,
            AdapterOptions opts) {
        if (pageMode != DispatchPageMode.LOADING || !opts.isDriverLoadingMode()) {
            return;
        }
        if (result.getConfirmedRoutes() == null || result.getConfirmedRoutes().isEmpty()) {
            return;
        }
        NxCommunityDispatchDriverRouteEntity route = result.getConfirmedRoutes().get(0);
        boolean showDepart = route.getNxCddrActualDepartAt() == null;
        vm.setShowDepartAction(showDepart);
        vm.setDepartActionEnabled(showDepart);
        vm.setDepartActionLabel("现在出发");
        vm.setDriverRouteId(route.getNxCommunityDispatchDriverRouteId());
    }

    private List<DispatchPageViewModel.DispatchAvailableDriver> buildAvailableDrivers(
            CommunityDispatchSandboxResult result,
            Map<Integer, Integer> assignedStopCountByDriver) {
        List<DispatchPageViewModel.DispatchAvailableDriver> list =
                new ArrayList<DispatchPageViewModel.DispatchAvailableDriver>();
        List<NxCommunityUserEntity> drivers = result != null ? result.getAvailableDrivers() : null;
        if (drivers == null) {
            return list;
        }
        for (NxCommunityUserEntity driver : drivers) {
            int stopCount = assignedStopCountByDriver.getOrDefault(driver.getNxCommunityUserId(), 0);
            // 与 Nx 分派中一致：已有路线卡的司机只在 sections 展示，不出现在 availableDrivers。
            if (stopCount > 0) {
                continue;
            }
            DispatchPageViewModel.DispatchAvailableDriver item =
                    new DispatchPageViewModel.DispatchAvailableDriver();
            item.setDriverUserId(driver.getNxCommunityUserId());
            item.setDriverName(CommunityDispatchDriverNameHelper.resolveDisplayName(driver));
            item.setDriverPhone(driver.getNxCouWxPhone());
            item.setSelectable(true);
            item.setStatusLabel("空闲可派");
            item.setBadgeLabel("");
            item.setRouteEditAction(CommunityDispatchRouteEditActionHelper.buildIdleDriverRouteEditAction(
                    result, driver.getNxCommunityUserId()));
            list.add(item);
        }
        return list;
    }

    private Map<Integer, Integer> buildAssignedStopCount(
            CommunityDispatchSandboxResult result, DispatchPageMode pageMode) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        List<NxCommunityDispatchDriverRouteEntity> routes = resolveDisplayRoutes(result, pageMode);
        for (NxCommunityDispatchDriverRouteEntity route : routes) {
            int count = route.getStops() != null ? route.getStops().size() : 0;
            map.put(route.getNxCddrDriverUserId(), count);
        }
        if (pageMode == DispatchPageMode.DISPATCH_SANDBOX && result.getConfirmedRoutes() != null) {
            for (NxCommunityDispatchDriverRouteEntity route : result.getConfirmedRoutes()) {
                if (route == null || route.getNxCddrDriverUserId() == null) {
                    continue;
                }
                if (CommunityDispatchConstants.isActiveLoadingOrDeliveryRoute(route)) {
                    map.put(route.getNxCddrDriverUserId(),
                            Math.max(map.getOrDefault(route.getNxCddrDriverUserId(), 0), 1));
                }
            }
        }
        return map;
    }

    private DispatchPrimaryAction buildDepartAction(CommunityDispatchSandboxResult result, Integer driverUserId) {
        DispatchPrimaryAction action = new DispatchPrimaryAction();
        action.setActionType("DEPART_NOW");
        action.setLabel("发车");
        action.setEnabled(true);
        action.setToneClass("stop-state-action");
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("communityId", result.getCommunityId());
        payload.put("routeDate", result.getRouteDate());
        payload.put("driverUserId", driverUserId);
        action.setPayload(payload);
        return action;
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

    private List<Integer> collectOrderIds(List<NxCommunityOrdersEntity> orders) {
        List<Integer> ids = new ArrayList<Integer>();
        for (NxCommunityOrdersEntity order : orders) {
            ids.add(order.getNxCommunityOrdersId());
        }
        return ids;
    }

    private String buildCustomerLabel(NxCommunityOrdersEntity order) {
        return "订单#" + order.getNxCommunityOrdersId();
    }

    private String routeStatusLabel(String status) {
        if (CommunityDispatchConstants.ROUTE_STATUS_LOADING.equals(status)) {
            return "装车中";
        }
        if (CommunityDispatchConstants.ROUTE_STATUS_IN_DELIVERY.equals(status)) {
            return "配送中";
        }
        if (CommunityDispatchConstants.ROUTE_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        return status != null ? status : "";
    }

    private String stopStatusLabel(String status) {
        if (CommunityDispatchConstants.STOP_STATUS_ASSIGNED.equals(status)) {
            return "已分配";
        }
        if (CommunityDispatchConstants.STOP_STATUS_LOADING.equals(status)) {
            return "装车中";
        }
        if (CommunityDispatchConstants.STOP_STATUS_IN_DELIVERY.equals(status)) {
            return "配送中";
        }
        if (CommunityDispatchConstants.STOP_STATUS_DELIVERED.equals(status)) {
            return "已送达";
        }
        return status != null ? status : "";
    }

    private Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static final class AdapterOptions {
        private boolean driverLoadingMode;
        private boolean driverDeliveryMode;

        public static AdapterOptions defaults() {
            return new AdapterOptions();
        }

        public static AdapterOptions driverLoading() {
            AdapterOptions options = new AdapterOptions();
            options.driverLoadingMode = true;
            return options;
        }

        public static AdapterOptions driverDelivery() {
            AdapterOptions options = new AdapterOptions();
            options.driverDeliveryMode = true;
            return options;
        }

        public boolean isDriverLoadingMode() {
            return driverLoadingMode;
        }

        public boolean isDriverDeliveryMode() {
            return driverDeliveryMode;
        }
    }
}
