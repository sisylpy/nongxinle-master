package com.nongxinle.route;

import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

public class DisRouteSandboxDispatchEligibilityHelperTest {

    @Test
    public void resolveSandboxDispatchIneligibleDriverUserIds_scopesActiveTasksByRouteDate() {
        RecordingTaskDao taskDao = new RecordingTaskDao();
        taskDao.addTask(task(60, "2026-06-28", IN_DELIVERY, 307));
        taskDao.addTask(task(70, "2026-06-29", IN_DELIVERY, 294));

        Set<Integer> blocked = DisRouteSandboxDispatchEligibilityHelper.resolveSandboxDispatchIneligibleDriverUserIds(
                null,
                null,
                taskDao,
                160,
                "2026-06-29",
                null);

        Assert.assertFalse(blocked.contains(307));
        Assert.assertTrue(blocked.contains(294));
        Assert.assertEquals("2026-06-29", taskDao.lastRouteDateFilter);
    }

    private static NxDisShipmentTaskEntity task(Integer id,
                                                String routeDate,
                                                String status,
                                                Integer driverUserId) {
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstId(id);
        task.setNxDstRouteDate(routeDate);
        task.setNxDstStatus(status);
        task.setNxDstAssignedDriverUserId(driverUserId);
        return task;
    }

    private static final class RecordingTaskDao implements NxDisShipmentTaskDao {

        private final List<NxDisShipmentTaskEntity> tasks = new ArrayList<NxDisShipmentTaskEntity>();
        private String lastRouteDateFilter;

        void addTask(NxDisShipmentTaskEntity task) {
            tasks.add(task);
        }

        @Override
        public List<NxDisShipmentTaskEntity> queryList(Map<String, Object> map) {
            lastRouteDateFilter = map != null && map.get("routeDate") != null
                    ? String.valueOf(map.get("routeDate")) : null;
            String status = map != null && map.get("status") != null
                    ? String.valueOf(map.get("status")) : null;
            String routeDate = lastRouteDateFilter;
            List<NxDisShipmentTaskEntity> matched = new ArrayList<NxDisShipmentTaskEntity>();
            for (NxDisShipmentTaskEntity task : tasks) {
                if (task == null) {
                    continue;
                }
                if (status != null && !status.equals(task.getNxDstStatus())) {
                    continue;
                }
                if (routeDate != null && !routeDate.equals(task.getNxDstRouteDate())) {
                    continue;
                }
                matched.add(task);
            }
            return matched;
        }

        @Override
        public NxDisShipmentTaskEntity queryObject(Integer nxDstId) {
            return null;
        }

        @Override
        public NxDisShipmentTaskEntity queryByOpenKey(String openKey) {
            return null;
        }

        @Override
        public List<NxDisShipmentTaskEntity> queryByPlanId(Integer planId) {
            return Collections.emptyList();
        }

        @Override
        public List<NxDisShipmentTaskEntity> queryByDriverRouteId(Integer driverRouteId) {
            return Collections.emptyList();
        }

        @Override
        public void save(NxDisShipmentTaskEntity task) {
        }

        @Override
        public void update(NxDisShipmentTaskEntity task) {
        }

        @Override
        public List<NxDisShipmentTaskEntity> queryByDisRouteDateStatus(Map<String, Object> map) {
            return Collections.emptyList();
        }

        @Override
        public void clearOpenKey(Integer taskId) {
        }

        @Override
        public void updateSchedule(NxDisShipmentTaskEntity entity) {
        }

        @Override
        public List<com.nongxinle.dto.route.DeliveryHistoryPreferenceAggRow> queryDeliveryHistoryAggByDepAndDriver(
                Integer disId,
                List<Integer> depFatherIds,
                Integer lookbackDays,
                double manualLockedWeight) {
            return Collections.emptyList();
        }
    }
}
