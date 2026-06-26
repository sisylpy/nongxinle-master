package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDistanceTypes;
import com.nongxinle.route.DisRouteExecutionRouteSnapshotHelper;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import com.nongxinle.route.RouteCostProvider;
import com.nongxinle.route.cost.ResilientTencentRouteCostProvider;
import com.nongxinle.route.cost.TencentMatrixRouteCostProvider;
import com.nongxinle.route.model.CostMatrix;
import com.nongxinle.route.model.GeoPoint;
import com.nongxinle.route.model.RouteStopInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.nongxinle.route.RouteCoordinateUtils.isValidCoordinate;
import static com.nongxinle.route.RouteCoordinateUtils.toPoint;

/**
 * Phase 3a：合并路线后按当前站点顺序重算 leg / 返程 / 路线总计（含 confirmed stop，不沿用入库 0）。
 */
@Component
public class DisRouteSandboxLegMetricsHelper {

  private static final long STRAIGHT_SPEED_M_PER_S = 8L;

  @Autowired
  private TencentMatrixRouteCostProvider tencentMatrixRouteCostProvider;

  /** 已出发路线：DB 快照优先；缺失时仍做坐标 leg 重算（只读展示，非沙盘分配）。 */
  public void applyToPlan(GeoPoint depot, NxDisRoutePlanEntity plan) throws IOException {
    if (plan == null || plan.getDriverRoutes() == null) {
      return;
    }
    GeoPoint effectiveDepot = depot != null ? depot : planDepot(plan);
    RouteCostProvider costProvider = new ResilientTencentRouteCostProvider(tencentMatrixRouteCostProvider);
    for (NxDisDriverRouteEntity route : plan.getDriverRoutes()) {
      if (DisRouteRouteExecutionHelper.isExecutionRoute(route)
          && !DisRouteExecutionRouteSnapshotHelper.routeSnapshotIncomplete(route)) {
        continue;
      }
      applyToRoute(effectiveDepot, route, costProvider);
    }
  }

  /** 未分配客户：从市场到各站的 leg 距离/耗时（单次矩阵，不写 DB）。 */
  public void applyDepotLegToStops(GeoPoint depot, List<NxDisRouteStopEntity> stops) throws IOException {
    if (depot == null || stops == null || stops.isEmpty()) {
      return;
    }
    List<NxDisRouteStopEntity> pending = new ArrayList<NxDisRouteStopEntity>();
    List<RouteStopInput> stopInputs = new ArrayList<RouteStopInput>();
    for (NxDisRouteStopEntity stop : stops) {
      if (stop == null || hasLegMetrics(stop)) {
        continue;
      }
      GeoPoint location = resolveStopLocation(stop);
      if (location == null) {
        continue;
      }
      RouteStopInput input = new RouteStopInput();
      input.setDepartmentId(stop.getNxDrsDepartmentId());
      input.setDepartmentName(stop.getNxDrsDepartmentName());
      input.setLocation(location);
      input.setAddress(stop.getNxDrsAddress());
      pending.add(stop);
      stopInputs.add(input);
    }
    if (stopInputs.isEmpty()) {
      return;
    }
    RouteCostProvider costProvider = new ResilientTencentRouteCostProvider(tencentMatrixRouteCostProvider);
    CostMatrix matrix = costProvider.buildMatrix(depot, stopInputs);
    for (int i = 0; i < pending.size(); i++) {
      int matrixIdx = i + 1;
      applyLegToStop(pending.get(i), matrix.distance(0, matrixIdx), matrix.duration(0, matrixIdx),
          matrix.getDistanceProvider(), matrix.distanceType(0, matrixIdx));
    }
  }

  private static boolean hasLegMetrics(NxDisRouteStopEntity stop) {
    if (stop.getNxDrsLegDistanceM() != null && stop.getNxDrsLegDistanceM() > 0L) {
      return true;
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    return task != null && task.getNxDstLegDistanceM() != null && task.getNxDstLegDistanceM() > 0L;
  }

  /** 单条司机路线 leg / 返程重算（内存模拟用）。 */
  public void applyToDriverRoute(GeoPoint depot, NxDisDriverRouteEntity route) throws IOException {
    if (route == null) {
      return;
    }
    RouteCostProvider costProvider = new ResilientTencentRouteCostProvider(tencentMatrixRouteCostProvider);
    applyToRoute(depot != null ? depot : planDepot(null), route, costProvider);
  }

  /**
   * 最后一站 → 市场：请求腾讯驾车路线，写入 returnLeg（与站间 leg 同 API）。
   */
  public void applyReturnLegFromLastStopToDepot(GeoPoint depot, NxDisDriverRouteEntity route) throws IOException {
    if (route == null || route.getStops() == null || route.getStops().isEmpty() || depot == null) {
      return;
    }
    List<NxDisRouteStopEntity> orderedStops = orderedStops(route.getStops());
    NxDisRouteStopEntity lastStop = orderedStops.get(orderedStops.size() - 1);
    GeoPoint lastPoint = resolveStopLocation(lastStop);
    if (lastPoint == null) {
      applyReturnLegStraightLineFallback(depot, lastStop, route);
      return;
    }
    RouteStopInput input = new RouteStopInput();
    input.setDepartmentId(lastStop.getNxDrsDepartmentId());
    input.setDepartmentName(lastStop.getNxDrsDepartmentName());
    input.setLocation(lastPoint);
    input.setAddress(lastStop.getNxDrsAddress());
    RouteCostProvider costProvider = new ResilientTencentRouteCostProvider(tencentMatrixRouteCostProvider);
    CostMatrix matrix = costProvider.buildMatrix(depot, Collections.singletonList(input));
    long returnDist = matrix.distance(1, 0);
    long returnDur = matrix.duration(1, 0);
    String returnType = matrix.distanceType(1, 0);
    route.setReturnLegDistanceM(returnDist);
    route.setReturnLegDurationS(returnDur);
    route.setReturnLegDistanceType(returnType);
  }

  private void applyToRoute(GeoPoint depot,
                            NxDisDriverRouteEntity route,
                            RouteCostProvider costProvider) throws IOException {
    if (route == null || route.getStops() == null || route.getStops().isEmpty()) {
      return;
    }
    List<NxDisRouteStopEntity> orderedStops = orderedStops(route.getStops());
    List<NxDisRouteStopEntity> routableStops = new ArrayList<NxDisRouteStopEntity>();
    List<RouteStopInput> stopInputs = new ArrayList<RouteStopInput>();
    for (NxDisRouteStopEntity stop : orderedStops) {
      GeoPoint location = resolveStopLocation(stop);
      if (stop == null || location == null) {
        continue;
      }
      RouteStopInput input = new RouteStopInput();
      input.setDepartmentId(stop.getNxDrsDepartmentId());
      input.setDepartmentName(stop.getNxDrsDepartmentName());
      input.setLocation(location);
      input.setAddress(stop.getNxDrsAddress());
      routableStops.add(stop);
      stopInputs.add(input);
    }
    if (stopInputs.isEmpty()) {
      return;
    }
    GeoPoint effectiveDepot = depot;
    if (effectiveDepot == null) {
      return;
    }

    if (allStopsHaveLegMetrics(routableStops)) {
      applyRouteTotalsFromExistingLegs(route, routableStops, effectiveDepot);
      return;
    }

    CostMatrix matrix = costProvider.buildMatrix(effectiveDepot, stopInputs);
    int prevIdx = 0;
    long routeDistanceM = 0L;
    long routeDriveDurationS = 0L;
    boolean hasStraightLeg = false;
    String routeProvider = matrix.getDistanceProvider();

    for (int i = 0; i < routableStops.size(); i++) {
      NxDisRouteStopEntity stop = routableStops.get(i);
      int matrixIdx = i + 1;
      long legDist = matrix.distance(prevIdx, matrixIdx);
      long legDur = matrix.duration(prevIdx, matrixIdx);
      String legType = matrix.distanceType(prevIdx, matrixIdx);
      if (DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE.equals(legType)) {
        hasStraightLeg = true;
      }

      applyLegToStop(stop, legDist, legDur, routeProvider, legType);
      routeDistanceM += legDist;
      routeDriveDurationS += legDur;
      prevIdx = matrixIdx;
    }

    long returnDist = matrix.distance(prevIdx, 0);
    long returnDur = matrix.duration(prevIdx, 0);
    String returnType = matrix.distanceType(prevIdx, 0);
    if (DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE.equals(returnType)) {
      hasStraightLeg = true;
    }
    routeDistanceM += returnDist;
    routeDriveDurationS += returnDur;

    route.setReturnLegDistanceM(returnDist);
    route.setReturnLegDurationS(returnDur);
    route.setReturnLegDistanceType(returnType);
    route.setNxDdrTotalDistanceM(routeDistanceM);
    route.setNxDdrTotalDurationS(routeDriveDurationS);
    route.setDistanceProvider(routeProvider);
    route.setRouteDistanceType(hasStraightLeg
        ? DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE
        : DisRouteDistanceTypes.ROUTE_DISTANCE);
    route.setNxDdrStopCount(route.getStops() != null ? route.getStops().size() : routableStops.size());
  }

  private static boolean allStopsHaveLegMetrics(List<NxDisRouteStopEntity> routableStops) {
    if (routableStops == null || routableStops.isEmpty()) {
      return false;
    }
    for (NxDisRouteStopEntity stop : routableStops) {
      if (!hasLegMetrics(stop)) {
        return false;
      }
    }
    return true;
  }

  private void applyRouteTotalsFromExistingLegs(NxDisDriverRouteEntity route,
                                                     List<NxDisRouteStopEntity> routableStops,
                                                     GeoPoint depot) {
    long routeDistanceM = 0L;
    long routeDriveDurationS = 0L;
    boolean hasStraightLeg = false;
    String routeProvider = null;
    for (NxDisRouteStopEntity stop : routableStops) {
      long legDist = resolveLegDistanceM(stop);
      long legDur = resolveLegDurationS(stop);
      routeDistanceM += legDist;
      routeDriveDurationS += legDur;
      String legType = resolveLegDistanceType(stop);
      if (DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE.equals(legType)) {
        hasStraightLeg = true;
      }
      if (routeProvider == null) {
        routeProvider = resolveLegProvider(stop);
      }
    }
    try {
      applyReturnLegFromLastStopToDepot(depot, route);
    } catch (IOException ex) {
      NxDisRouteStopEntity lastStop = routableStops.get(routableStops.size() - 1);
      applyReturnLegStraightLineFallback(depot, lastStop, route);
    }
    long returnDist = route.getReturnLegDistanceM() != null ? route.getReturnLegDistanceM() : 0L;
    long returnDur = route.getReturnLegDurationS() != null ? route.getReturnLegDurationS() : 0L;
    String returnType = route.getReturnLegDistanceType();
    if (DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE.equals(returnType)) {
      hasStraightLeg = true;
    }
    routeDistanceM += returnDist;
    routeDriveDurationS += returnDur;
    route.setNxDdrTotalDistanceM(routeDistanceM);
    route.setNxDdrTotalDurationS(routeDriveDurationS);
    route.setDistanceProvider(routeProvider != null ? routeProvider : "STRAIGHT");
    route.setRouteDistanceType(hasStraightLeg
        ? DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE
        : DisRouteDistanceTypes.ROUTE_DISTANCE);
    route.setNxDdrStopCount(route.getStops() != null ? route.getStops().size() : routableStops.size());
  }

  private static long resolveLegDistanceM(NxDisRouteStopEntity stop) {
    if (stop.getNxDrsLegDistanceM() != null && stop.getNxDrsLegDistanceM() > 0L) {
      return stop.getNxDrsLegDistanceM();
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    if (task != null && task.getNxDstLegDistanceM() != null && task.getNxDstLegDistanceM() > 0L) {
      return task.getNxDstLegDistanceM();
    }
    return 0L;
  }

  private static long resolveLegDurationS(NxDisRouteStopEntity stop) {
    if (stop.getNxDrsLegDurationS() != null && stop.getNxDrsLegDurationS() > 0L) {
      return stop.getNxDrsLegDurationS();
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    if (task != null && task.getNxDstLegDurationS() != null && task.getNxDstLegDurationS() > 0L) {
      return task.getNxDstLegDurationS();
    }
    return 0L;
  }

  private static String resolveLegDistanceType(NxDisRouteStopEntity stop) {
    if (stop.getLegDistanceType() != null) {
      return stop.getLegDistanceType();
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    return task != null ? task.getLegDistanceType() : null;
  }

  private static String resolveLegProvider(NxDisRouteStopEntity stop) {
    if (stop.getLegDistanceProvider() != null) {
      return stop.getLegDistanceProvider();
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    return task != null ? task.getLegDistanceProvider() : null;
  }

  private static GeoPoint planDepot(NxDisRoutePlanEntity plan) {
    if (plan != null && isValidCoordinate(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng())) {
      return toPoint(plan.getNxDrpDepotLat(), plan.getNxDrpDepotLng());
    }
    return null;
  }

  private static List<NxDisRouteStopEntity> orderedStops(List<NxDisRouteStopEntity> stops) {
    List<NxDisRouteStopEntity> ordered = new ArrayList<NxDisRouteStopEntity>(stops);
    Collections.sort(ordered, new Comparator<NxDisRouteStopEntity>() {
      @Override
      public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
        int seqA = a != null && a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : 0;
        int seqB = b != null && b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : 0;
        return Integer.compare(seqA, seqB);
      }
    });
    return ordered;
  }

  private static GeoPoint resolveStopLocation(NxDisRouteStopEntity stop) {
    if (isValidCoordinate(stop.getNxDrsLat(), stop.getNxDrsLng())) {
      return toPoint(stop.getNxDrsLat(), stop.getNxDrsLng());
    }
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    if (task != null && isValidCoordinate(task.getNxDstLat(), task.getNxDstLng())) {
      return toPoint(task.getNxDstLat(), task.getNxDstLng());
    }
    return null;
  }

  private static void applyReturnLegStraightLineFallback(GeoPoint depot,
                                                         NxDisRouteStopEntity lastStop,
                                                         NxDisDriverRouteEntity route) {
    if (route == null || depot == null || lastStop == null) {
      return;
    }
    GeoPoint lastPoint = resolveStopLocation(lastStop);
    long returnDist = 0L;
    long returnDur = 0L;
    if (lastPoint != null) {
      returnDist = straightLineMeters(lastPoint, depot);
      returnDur = Math.max(1L, returnDist / STRAIGHT_SPEED_M_PER_S);
    }
    route.setReturnLegDistanceM(returnDist);
    route.setReturnLegDurationS(returnDur);
    route.setReturnLegDistanceType(DisRouteDistanceTypes.ESTIMATED_STRAIGHT_DISTANCE);
  }

  private static long straightLineMeters(GeoPoint a, GeoPoint b) {
    if (a == null || b == null || a.getLat() == null || a.getLng() == null
        || b.getLat() == null || b.getLng() == null) {
      return 0L;
    }
    double lat1 = Math.toRadians(Double.parseDouble(a.getLat().trim()));
    double lat2 = Math.toRadians(Double.parseDouble(b.getLat().trim()));
    double dLat = lat2 - lat1;
    double dLng = Math.toRadians(Double.parseDouble(b.getLng().trim())
        - Double.parseDouble(a.getLng().trim()));
    double sinLat = Math.sin(dLat / 2.0);
    double sinLng = Math.sin(dLng / 2.0);
    double h = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLng * sinLng;
    return Math.round(6371000.0 * 2.0 * Math.asin(Math.min(1.0, Math.sqrt(h))));
  }

  private static void applyLegToStop(NxDisRouteStopEntity stop,
                                     long legDist,
                                     long legDur,
                                     String provider,
                                     String distanceType) {
    stop.setNxDrsLegDistanceM(legDist);
    stop.setNxDrsLegDurationS(legDur);
    stop.setLegDistanceProvider(provider);
    stop.setLegDistanceType(distanceType);
    NxDisShipmentTaskEntity task = stop.getShipmentTask();
    if (task != null) {
      task.setNxDstLegDistanceM(legDist);
      task.setNxDstLegDurationS(legDur);
      task.setLegDistanceProvider(provider);
      task.setLegDistanceType(distanceType);
    }
  }
}
