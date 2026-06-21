package com.nongxinle.route.optimizer;

import com.nongxinle.route.RouteOptimizer;
import com.nongxinle.route.RouteOptimizerType;
import com.nongxinle.route.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 多司机均衡插入 + 每车 2-opt 局部优化。
 */
@Component
public class BalancedInsertion2OptRouteOptimizer implements RouteOptimizer {

    private static final double BALANCE_WEIGHT = 0.35;

    @Override
    public RouteOptimizerType optimizerType() {
        return RouteOptimizerType.BALANCED_INSERTION_2OPT;
    }

    @Override
    public RouteOptimizeResult optimize(RouteOptimizeRequest request) {
        CostMatrix matrix = request.getCostMatrix();
        List<DriverInput> drivers = request.getDrivers();
        List<RouteStopInput> remaining = new ArrayList<>(request.getStops());

        List<List<Integer>> routes = new ArrayList<>();
        for (int i = 0; i < drivers.size(); i++) {
            routes.add(new ArrayList<Integer>());
        }

        remaining.sort(new Comparator<RouteStopInput>() {
            @Override
            public int compare(RouteStopInput a, RouteStopInput b) {
                int idxA = request.getStops().indexOf(a) + 1;
                int idxB = request.getStops().indexOf(b) + 1;
                long da = matrix.distance(0, idxA);
                long db = matrix.distance(0, idxB);
                if (da != db) {
                    return Long.compare(db, da);
                }
                return Integer.compare(b.getOrderCount(), a.getOrderCount());
            }
        });

        while (!remaining.isEmpty()) {
            RouteStopInput stop = remaining.remove(0);
            int stopIdx = stopIndex(request.getStops(), stop);
            insertBestBalanced(routes, stopIdx, matrix, BALANCE_WEIGHT);
        }

        for (int d = 0; d < routes.size(); d++) {
            routes.set(d, twoOptImprove(routes.get(d), matrix));
        }

        return buildResult(request, routes, matrix);
    }

    private void insertBestBalanced(List<List<Integer>> routes, int stopIdx, CostMatrix matrix, double balanceWeight) {
        double avgLoad = averageLoad(routes);
        int bestDriver = 0;
        int bestPos = 0;
        double bestScore = Double.MAX_VALUE;

        for (int d = 0; d < routes.size(); d++) {
            List<Integer> route = routes.get(d);
            for (int pos = 0; pos <= route.size(); pos++) {
                long delta = insertionCost(route, stopIdx, pos, matrix);
                double balancePenalty = balanceWeight * Math.abs((route.size() + 1) - avgLoad);
                double score = delta + balancePenalty * matrix.duration(0, stopIdx);
                if (score < bestScore) {
                    bestScore = score;
                    bestDriver = d;
                    bestPos = pos;
                }
            }
        }
        routes.get(bestDriver).add(bestPos, stopIdx);
    }

    private double averageLoad(List<List<Integer>> routes) {
        int total = 0;
        for (List<Integer> route : routes) {
            total += route.size();
        }
        return routes.isEmpty() ? 0 : (double) total / routes.size();
    }

    private long insertionCost(List<Integer> route, int stopIdx, int pos, CostMatrix matrix) {
        if (route.isEmpty()) {
            return matrix.duration(0, stopIdx);
        }
        if (pos == 0) {
            int first = route.get(0);
            return matrix.duration(0, stopIdx) + matrix.duration(stopIdx, first) - matrix.duration(0, first);
        }
        if (pos == route.size()) {
            int last = route.get(route.size() - 1);
            return matrix.duration(last, stopIdx);
        }
        int prev = route.get(pos - 1);
        int next = route.get(pos);
        return matrix.duration(prev, stopIdx) + matrix.duration(stopIdx, next) - matrix.duration(prev, next);
    }

    private List<Integer> twoOptImprove(List<Integer> route, CostMatrix matrix) {
        if (route.size() < 3) {
            return route;
        }
        List<Integer> best = new ArrayList<Integer>(route);
        long bestCost = routeDuration(best, matrix);
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int k = i + 1; k < best.size(); k++) {
                    List<Integer> candidate = twoOptSwap(best, i, k);
                    long candidateCost = routeDuration(candidate, matrix);
                    if (candidateCost < bestCost) {
                        best = candidate;
                        bestCost = candidateCost;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private long routeDuration(List<Integer> route, CostMatrix matrix) {
        if (route.isEmpty()) {
            return 0;
        }
        long total = matrix.duration(0, route.get(0));
        for (int i = 0; i < route.size() - 1; i++) {
            total += matrix.duration(route.get(i), route.get(i + 1));
        }
        return total;
    }

    private List<Integer> twoOptSwap(List<Integer> route, int i, int k) {
        List<Integer> copy = new ArrayList<>(route);
        int left = i + 1;
        int right = k;
        while (left < right) {
            Integer tmp = copy.get(left);
            copy.set(left, copy.get(right));
            copy.set(right, tmp);
            left++;
            right--;
        }
        return copy;
    }

    private RouteOptimizeResult buildResult(RouteOptimizeRequest request, List<List<Integer>> routes, CostMatrix matrix) {
        RouteOptimizeResult result = new RouteOptimizeResult();
        long totalDistance = 0;
        long totalDuration = 0;

        for (int d = 0; d < routes.size(); d++) {
            List<Integer> stopIdxList = routes.get(d);
            DriverInput driver = request.getDrivers().get(d);
            OptimizedDriverRouteResult driverRoute = new OptimizedDriverRouteResult();
            driverRoute.setDriverUserId(driver.getDriverUserId());
            driverRoute.setDriverName(driver.getDriverName());
            driverRoute.setRouteSeq(d + 1);

            long routeDistance = 0;
            long routeDuration = 0;
            int prevIdx = 0;
            int seq = 1;
            for (Integer stopMatrixIdx : stopIdxList) {
                RouteStopInput stop = request.getStops().get(stopMatrixIdx - 1);
                long legDist = matrix.distance(prevIdx, stopMatrixIdx);
                long legDur = matrix.duration(prevIdx, stopMatrixIdx);
                routeDistance += legDist;
                routeDuration += legDur;

                OptimizedStopResult stopResult = new OptimizedStopResult();
                stopResult.setDepartmentId(stop.getDepartmentId());
                stopResult.setDepartmentName(stop.getDepartmentName());
                stopResult.setLocation(stop.getLocation());
                stopResult.setAddress(stop.getAddress());
                stopResult.setOrderCount(stop.getOrderCount());
                stopResult.setStopSeq(seq++);
                stopResult.setLegDistanceM(legDist);
                stopResult.setLegDurationS(legDur);
                driverRoute.getStops().add(stopResult);
                prevIdx = stopMatrixIdx;
            }

            driverRoute.setTotalDistanceM(routeDistance);
            driverRoute.setTotalDurationS(routeDuration);
            result.getDriverRoutes().add(driverRoute);
            totalDistance += routeDistance;
            totalDuration += routeDuration;
        }

        result.setTotalDistanceM(totalDistance);
        result.setTotalDurationS(totalDuration);
        return result;
    }

    private int stopIndex(List<RouteStopInput> stops, RouteStopInput target) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getDepartmentId().equals(target.getDepartmentId())) {
                return i + 1;
            }
        }
        return -1;
    }
}
