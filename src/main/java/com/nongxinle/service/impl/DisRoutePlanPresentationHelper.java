package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRouteStopDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1.5c：driver_route 汇总与 stopSeq 展示连续化（尊重 manualLocked 固定位置）。
 */
@Component
public class DisRoutePlanPresentationHelper {

    private static final String STOP_CANCELLED = "CANCELLED";

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisRouteStopDao nxDisRouteStopDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    public void refreshPlanPresentation(Integer planId) {
        if (planId == null) {
            return;
        }
        normalizeStopSeqForPlan(planId);
        refreshDriverRouteSummaries(planId);
    }

    public void refreshDriverRouteSummaries(Integer planId) {
        if (planId == null) {
            return;
        }
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            refreshDriverRouteSummary(route.getNxDdrId());
        }
    }

    private void refreshDriverRouteSummary(Integer driverRouteId) {
        List<NxDisRouteStopEntity> stops = nxDisRouteStopDao.queryByDriverRouteId(driverRouteId);
        int stopCount = 0;
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        if (stops != null) {
            for (NxDisRouteStopEntity stop : stops) {
                if (!isCountableStop(stop)) {
                    continue;
                }
                stopCount++;
                if (stop.getNxDrsLegDistanceM() != null) {
                    totalDistanceM += stop.getNxDrsLegDistanceM();
                }
                if (stop.getNxDrsLegDurationS() != null) {
                    totalDurationS += stop.getNxDrsLegDurationS();
                }
            }
        }
        NxDisDriverRouteEntity update = new NxDisDriverRouteEntity();
        update.setNxDdrId(driverRouteId);
        update.setNxDdrStopCount(stopCount);
        update.setNxDdrTotalDistanceM(totalDistanceM);
        update.setNxDdrTotalDurationS(totalDurationS);
        nxDisDriverRouteDao.update(update);
    }

    private void normalizeStopSeqForPlan(Integer planId) {
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            normalizeStopSeqForDriverRoute(route.getNxDdrId());
        }
    }

    private void normalizeStopSeqForDriverRoute(Integer driverRouteId) {
        List<NxDisRouteStopEntity> stops = nxDisRouteStopDao.queryByDriverRouteId(driverRouteId);
        if (stops == null || stops.isEmpty()) {
            return;
        }

        List<NxDisRouteStopEntity> activeStops = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisRouteStopEntity stop : stops) {
            if (isCountableStop(stop)) {
                activeStops.add(stop);
            }
        }
        if (activeStops.isEmpty()) {
            return;
        }

        Set<Integer> fixedSeqs = new HashSet<Integer>();
        Map<Integer, Integer> targetSeqByStopId = new HashMap<Integer, Integer>();
        List<NxDisRouteStopEntity> unlockedStops = new ArrayList<NxDisRouteStopEntity>();

        for (NxDisRouteStopEntity stop : activeStops) {
            NxDisShipmentTaskEntity task = loadTask(stop.getNxDrsShipmentTaskId());
            if (isManualLockedStop(task, stop)) {
                Integer fixedSeq = resolveFixedStopSeq(task, stop);
                if (fixedSeq == null) {
                    unlockedStops.add(stop);
                    continue;
                }
                if (fixedSeqs.contains(fixedSeq)) {
                    throw new IllegalStateException(
                            "司机路线 stopSeq 冲突：driverRouteId=" + driverRouteId
                                    + " 存在多个 manualLocked 停靠点占用 stopSeq=" + fixedSeq);
                }
                fixedSeqs.add(fixedSeq);
                targetSeqByStopId.put(stop.getNxDrsId(), fixedSeq);
            } else {
                unlockedStops.add(stop);
            }
        }

        Collections.sort(unlockedStops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : Integer.MAX_VALUE;
                int seqB = b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : Integer.MAX_VALUE;
                if (seqA != seqB) {
                    return Integer.compare(seqA, seqB);
                }
                return Integer.compare(a.getNxDrsId(), b.getNxDrsId());
            }
        });

        int nextSeq = 1;
        for (NxDisRouteStopEntity stop : unlockedStops) {
            while (fixedSeqs.contains(nextSeq)) {
                nextSeq++;
            }
            targetSeqByStopId.put(stop.getNxDrsId(), nextSeq);
            nextSeq++;
        }

        for (Map.Entry<Integer, Integer> entry : targetSeqByStopId.entrySet()) {
            NxDisRouteStopEntity stop = findStop(activeStops, entry.getKey());
            if (stop == null) {
                continue;
            }
            Integer targetSeq = entry.getValue();
            if (stop.getNxDrsStopSeq() == null || !stop.getNxDrsStopSeq().equals(targetSeq)) {
                NxDisRouteStopEntity update = new NxDisRouteStopEntity();
                update.setNxDrsId(stop.getNxDrsId());
                update.setNxDrsStopSeq(targetSeq);
                nxDisRouteStopDao.update(update);
            }
        }
    }

    private NxDisRouteStopEntity findStop(List<NxDisRouteStopEntity> stops, Integer stopId) {
        for (NxDisRouteStopEntity stop : stops) {
            if (stopId.equals(stop.getNxDrsId())) {
                return stop;
            }
        }
        return null;
    }

    private NxDisShipmentTaskEntity loadTask(Integer taskId) {
        if (taskId == null) {
            return null;
        }
        return nxDisShipmentTaskDao.queryObject(taskId);
    }

    private boolean isManualLockedStop(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        if (task == null) {
            return false;
        }
        if (task.getNxDstManualLocked() == null || task.getNxDstManualLocked() != 1) {
            return false;
        }
        return resolveFixedStopSeq(task, stop) != null;
    }

    private Integer resolveFixedStopSeq(NxDisShipmentTaskEntity task, NxDisRouteStopEntity stop) {
        if (task != null && task.getNxDstManualStopSeq() != null) {
            return task.getNxDstManualStopSeq();
        }
        if (stop != null && stop.getNxDrsStopSeq() != null) {
            return stop.getNxDrsStopSeq();
        }
        return null;
    }

    private boolean isCountableStop(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return false;
        }
        if (stop.getNxDrsShipmentTaskId() == null) {
            return false;
        }
        String status = stop.getNxDrsStopStatus();
        return status == null || !STOP_CANCELLED.equalsIgnoreCase(status);
    }

    /** 读模型：清空旧 stop_order 字段，按 stopSeq 排序。 */
    public static void prepareStopsForReadModel(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            stop.setOrders(null);
            stop.setOrderIds(null);
        }
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = a.getNxDrsStopSeq() != null ? a.getNxDrsStopSeq() : Integer.MAX_VALUE;
                int seqB = b.getNxDrsStopSeq() != null ? b.getNxDrsStopSeq() : Integer.MAX_VALUE;
                if (seqA != seqB) {
                    return Integer.compare(seqA, seqB);
                }
                return Integer.compare(a.getNxDrsId(), b.getNxDrsId());
            }
        });
    }
}
