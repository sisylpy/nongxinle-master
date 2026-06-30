package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.route.DisRouteDeliveryStopAdapter;
import com.nongxinle.route.DisRouteRouteExecutionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1.5c：driver_route 汇总与 stopSeq 展示连续化。
 * Phase 3a.1：task 驱动，route_stop 仅 legacy fallback。
 */
@Component
public class DisRoutePlanPresentationHelper {

    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;

    public void refreshPlanPresentation(Integer planId) {
        if (planId == null) {
            return;
        }
        normalizeRouteSeqForPlan(planId);
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
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        int stopCount = 0;
        long totalDistanceM = 0L;
        long totalDurationS = 0L;
        if (tasks != null) {
            for (NxDisShipmentTaskEntity task : tasks) {
                if (!isCountableTask(task)) {
                    continue;
                }
                stopCount++;
                if (task.getNxDstLegDistanceM() != null) {
                    totalDistanceM += task.getNxDstLegDistanceM();
                }
                if (task.getNxDstLegDurationS() != null) {
                    totalDurationS += task.getNxDstLegDurationS();
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

    private void normalizeRouteSeqForPlan(Integer planId) {
        List<NxDisDriverRouteEntity> routes = nxDisDriverRouteDao.queryByPlanId(planId);
        if (routes == null) {
            return;
        }
        for (NxDisDriverRouteEntity route : routes) {
            normalizeRouteSeqForDriverRoute(route.getNxDdrId());
        }
    }

    private void normalizeRouteSeqForDriverRoute(Integer driverRouteId) {
        List<NxDisShipmentTaskEntity> tasks = nxDisShipmentTaskDao.queryByDriverRouteId(driverRouteId);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        List<NxDisShipmentTaskEntity> activeTasks = new ArrayList<NxDisShipmentTaskEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (isCountableTask(task)) {
                activeTasks.add(task);
            }
        }
        if (activeTasks.isEmpty()) {
            return;
        }

        Set<Integer> fixedSeqs = new HashSet<Integer>();
        Map<Integer, Integer> targetSeqByTaskId = new HashMap<Integer, Integer>();
        List<NxDisShipmentTaskEntity> unlockedTasks = new ArrayList<NxDisShipmentTaskEntity>();

        for (NxDisShipmentTaskEntity task : activeTasks) {
            if (isManualLockedTask(task)) {
                Integer fixedSeq = resolveFixedRouteSeq(task);
                if (fixedSeq == null) {
                    unlockedTasks.add(task);
                    continue;
                }
                if (fixedSeqs.contains(fixedSeq)) {
                    throw new IllegalStateException(
                            "司机路线 stopSeq 冲突：driverRouteId=" + driverRouteId
                                    + " 存在多个 manualLocked 停靠点占用 stopSeq=" + fixedSeq);
                }
                fixedSeqs.add(fixedSeq);
                targetSeqByTaskId.put(task.getNxDstId(), fixedSeq);
            } else {
                unlockedTasks.add(task);
            }
        }

        Collections.sort(unlockedTasks, new Comparator<NxDisShipmentTaskEntity>() {
            @Override
            public int compare(NxDisShipmentTaskEntity a, NxDisShipmentTaskEntity b) {
                int seqA = a.getNxDstRouteSeq() != null ? a.getNxDstRouteSeq()
                        : (a.getNxDstManualStopSeq() != null ? a.getNxDstManualStopSeq() : Integer.MAX_VALUE);
                int seqB = b.getNxDstRouteSeq() != null ? b.getNxDstRouteSeq()
                        : (b.getNxDstManualStopSeq() != null ? b.getNxDstManualStopSeq() : Integer.MAX_VALUE);
                if (seqA != seqB) {
                    return Integer.compare(seqA, seqB);
                }
                return Integer.compare(a.getNxDstId(), b.getNxDstId());
            }
        });

        int nextSeq = 1;
        for (NxDisShipmentTaskEntity task : unlockedTasks) {
            while (fixedSeqs.contains(nextSeq)) {
                nextSeq++;
            }
            targetSeqByTaskId.put(task.getNxDstId(), nextSeq);
            nextSeq++;
        }

        for (Map.Entry<Integer, Integer> entry : targetSeqByTaskId.entrySet()) {
            NxDisShipmentTaskEntity task = findTask(activeTasks, entry.getKey());
            if (task == null) {
                continue;
            }
            Integer targetSeq = entry.getValue();
            Integer currentSeq = task.getNxDstRouteSeq();
            if (currentSeq == null || !currentSeq.equals(targetSeq)) {
                NxDisShipmentTaskEntity update = new NxDisShipmentTaskEntity();
                update.setNxDstId(task.getNxDstId());
                update.setNxDstRouteSeq(targetSeq);
                nxDisShipmentTaskDao.update(update);
            }
        }
    }

    private NxDisShipmentTaskEntity findTask(List<NxDisShipmentTaskEntity> tasks, Integer taskId) {
        for (NxDisShipmentTaskEntity task : tasks) {
            if (taskId.equals(task.getNxDstId())) {
                return task;
            }
        }
        return null;
    }

    private boolean isManualLockedTask(NxDisShipmentTaskEntity task) {
        return task != null && task.getNxDstManualLocked() != null && task.getNxDstManualLocked() == 1
                && resolveFixedRouteSeq(task) != null;
    }

    private Integer resolveFixedRouteSeq(NxDisShipmentTaskEntity task) {
        if (task.getNxDstManualStopSeq() != null) {
            return task.getNxDstManualStopSeq();
        }
        return task.getNxDstRouteSeq();
    }

    private boolean isCountableTask(NxDisShipmentTaskEntity task) {
        return DisRouteRouteExecutionHelper.isRouteSeqActiveTask(task);
    }

    /** 读模型：从 task 构建内存 stop 列表（无 nxDrsId）。 */
    public static List<NxDisRouteStopEntity> tasksToReadModelStops(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<NxDisRouteStopEntity> stops = new ArrayList<NxDisRouteStopEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            stops.add(DisRouteDeliveryStopAdapter.fromTask(task, null));
        }
        prepareStopsForReadModel(stops);
        return stops;
    }

    private static int resolveReadModelStopSeq(NxDisRouteStopEntity stop) {
        if (stop == null) {
            return Integer.MAX_VALUE;
        }
        NxDisShipmentTaskEntity task = stop.getShipmentTask();
        Integer seq = DisRouteDeliveryStopAdapter.resolveReadModelStopSeq(task);
        if (seq != null && seq > 0) {
            return seq;
        }
        if (stop.getNxDrsStopSeq() != null && stop.getNxDrsStopSeq() > 0) {
            return stop.getNxDrsStopSeq();
        }
        return Integer.MAX_VALUE;
    }

    /** 读模型：清空旧 stop_order 字段，按 stopSeq 排序。 */
    public static void prepareStopsForReadModel(List<NxDisRouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        for (NxDisRouteStopEntity stop : stops) {
            stop.setOrderIds(null);
        }
        Collections.sort(stops, new Comparator<NxDisRouteStopEntity>() {
            @Override
            public int compare(NxDisRouteStopEntity a, NxDisRouteStopEntity b) {
                int seqA = resolveReadModelStopSeq(a);
                int seqB = resolveReadModelStopSeq(b);
                if (seqA != seqB) {
                    return Integer.compare(seqA, seqB);
                }
                Integer idA = a.getNxDrsShipmentTaskId();
                Integer idB = b.getNxDrsShipmentTaskId();
                if (idA != null && idB != null) {
                    return Integer.compare(idA, idB);
                }
                return 0;
            }
        });
    }
}
