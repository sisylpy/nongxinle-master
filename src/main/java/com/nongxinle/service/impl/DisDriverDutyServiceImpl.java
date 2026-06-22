package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDisDriverDutyDao;
import com.nongxinle.dao.NxDisDriverRouteDao;
import com.nongxinle.dao.NxDisRoutePlanDao;
import com.nongxinle.dao.NxDisShipmentTaskDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.dto.route.DriverAvailableDto;
import com.nongxinle.dto.route.DriverDutyRequest;
import com.nongxinle.dto.route.RouteDispatchOperationDecision;
import com.nongxinle.entity.NxDisDriverDutyEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.route.DisDriverDutyStatus;
import com.nongxinle.route.DisRouteDriverDutyLockHelper;
import com.nongxinle.route.DisRouteDispatchBatch;
import com.nongxinle.route.DisRoutePlanStatus;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteFeasibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.nongxinle.route.DisDriverDutyStatus.ON_DUTY;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserDriver;

@Service("disDriverDutyService")
public class DisDriverDutyServiceImpl implements DisDriverDutyService {

    @Autowired
    private NxDisDriverDutyDao nxDisDriverDutyDao;
    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;
    @Autowired
    private NxDisRoutePlanDao nxDisRoutePlanDao;
    @Autowired
    private NxDisDriverRouteDao nxDisDriverRouteDao;
    @Autowired
    private NxDisShipmentTaskDao nxDisShipmentTaskDao;
    @Autowired
    private DisRouteFeasibilityService disRouteFeasibilityService;

    @Override
    @Transactional
    public NxDisDriverDutyEntity checkIn(DriverDutyRequest request) {
        validateDutyRequest(request);
        NxDistributerUserEntity driver = requireDriverAccount(request.getDisId(), request.getDriverUserId());
        String dutyDate = resolveDutyDate(request.getDutyDate());
        Date now = new Date();

        NxDisDriverDutyEntity existing = nxDisDriverDutyDao.queryByDisDriverDate(
                request.getDisId(), request.getDriverUserId(), dutyDate);
        if (existing == null) {
            NxDisDriverDutyEntity entity = new NxDisDriverDutyEntity();
            entity.setNxDddDistributerId(request.getDisId());
            entity.setNxDddDriverUserId(request.getDriverUserId());
            entity.setNxDddDutyDate(dutyDate);
            entity.setNxDddDutyStatus(ON_DUTY);
            entity.setNxDddCheckInAt(now);
            entity.setNxDddCheckOutAt(null);
            entity.setNxDddOperatorUserId(request.getOperatorUserId());
            entity.setNxDddUpdatedAt(now);
            nxDisDriverDutyDao.save(entity);
            refreshActivePlansAfterDutyChange(request.getDisId(), dutyDate);
            return entity;
        }

        NxDisDriverDutyEntity update = new NxDisDriverDutyEntity();
        update.setNxDddId(existing.getNxDddId());
        update.setNxDddDutyStatus(ON_DUTY);
        update.setNxDddCheckInAt(existing.getNxDddCheckInAt() != null ? existing.getNxDddCheckInAt() : now);
        update.setNxDddCheckOutAt(null);
        update.setNxDddOperatorUserId(request.getOperatorUserId());
        update.setNxDddUpdatedAt(now);
        nxDisDriverDutyDao.update(update);
        refreshActivePlansAfterDutyChange(request.getDisId(), dutyDate);
        return nxDisDriverDutyDao.queryByDisDriverDate(request.getDisId(), request.getDriverUserId(), dutyDate);
    }

    @Override
    @Transactional
    public NxDisDriverDutyEntity checkOut(DriverDutyRequest request) {
        validateDutyRequest(request);
        requireDriverAccount(request.getDisId(), request.getDriverUserId());
        String dutyDate = resolveDutyDate(request.getDutyDate());
        RouteDispatchOperationDecision lockDecision = DisRouteDriverDutyLockHelper.evaluateCheckOut(
                request.getDisId(),
                dutyDate,
                request.getDriverUserId(),
                nxDisDriverRouteDao,
                nxDisRoutePlanDao,
                nxDisShipmentTaskDao);
        if (!lockDecision.isAllowed()) {
            String reason = lockDecision.getBlockedReason();
            throw new IllegalArgumentException(reason != null ? reason : "司机当前不能关闭可派");
        }
        Date now = new Date();

        NxDisDriverDutyEntity existing = nxDisDriverDutyDao.queryByDisDriverDate(
                request.getDisId(), request.getDriverUserId(), dutyDate);
        if (existing == null) {
            NxDisDriverDutyEntity entity = new NxDisDriverDutyEntity();
            entity.setNxDddDistributerId(request.getDisId());
            entity.setNxDddDriverUserId(request.getDriverUserId());
            entity.setNxDddDutyDate(dutyDate);
            entity.setNxDddDutyStatus(DisDriverDutyStatus.OFF_DUTY);
            entity.setNxDddCheckOutAt(now);
            entity.setNxDddOperatorUserId(request.getOperatorUserId());
            entity.setNxDddUpdatedAt(now);
            nxDisDriverDutyDao.save(entity);
            refreshActivePlansAfterDutyChange(request.getDisId(), dutyDate);
            return entity;
        }

        NxDisDriverDutyEntity update = new NxDisDriverDutyEntity();
        update.setNxDddId(existing.getNxDddId());
        update.setNxDddDutyStatus(DisDriverDutyStatus.OFF_DUTY);
        update.setNxDddCheckOutAt(now);
        update.setNxDddOperatorUserId(request.getOperatorUserId());
        update.setNxDddUpdatedAt(now);
        nxDisDriverDutyDao.update(update);
        refreshActivePlansAfterDutyChange(request.getDisId(), dutyDate);
        return nxDisDriverDutyDao.queryByDisDriverDate(request.getDisId(), request.getDriverUserId(), dutyDate);
    }

    private void refreshActivePlansAfterDutyChange(Integer disId, String dutyDate) {
        if (disId == null || dutyDate == null || dutyDate.trim().isEmpty()) {
            return;
        }
        String date = dutyDate.trim();
        String[] batches = {
                DisRouteDispatchBatch.MORNING,
                DisRouteDispatchBatch.AFTERNOON,
                DisRouteDispatchBatch.ADHOC
        };
        String[] statuses = {
                DisRoutePlanStatus.SIMULATED,
                DisRoutePlanStatus.ASSIGNED,
                DisRoutePlanStatus.READY
        };
        for (String batch : batches) {
            for (String status : statuses) {
                NxDisRoutePlanEntity plan = nxDisRoutePlanDao.queryByDisRouteDateBatchStatus(
                        disId, date, batch, status);
                if (plan != null && plan.getNxDrpId() != null) {
                    disRouteFeasibilityService.assess(plan.getNxDrpId());
                }
            }
        }
    }

    @Override
    public List<DriverAvailableDto> listAvailableDrivers(Integer disId, String routeDate) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        String dutyDate = resolveDutyDate(routeDate);
        List<DriverAvailableDto> drivers = nxDisDriverDutyDao.queryOnDutyDrivers(disId, dutyDate);
        return drivers != null ? drivers : Collections.<DriverAvailableDto>emptyList();
    }

    @Override
    public List<NxDistributerUserEntity> listOnDutyDriverUsers(Integer disId, String routeDate) {
        List<DriverAvailableDto> available = listAvailableDrivers(disId, routeDate);
        if (available.isEmpty()) {
            return new ArrayList<NxDistributerUserEntity>();
        }
        List<NxDistributerUserEntity> users = new ArrayList<NxDistributerUserEntity>();
        for (DriverAvailableDto dto : available) {
            NxDistributerUserEntity user = nxDistributerUserDao.queryObject(dto.getDriverUserId());
            if (user != null) {
                users.add(user);
            }
        }
        Collections.sort(users, new Comparator<NxDistributerUserEntity>() {
            @Override
            public int compare(NxDistributerUserEntity a, NxDistributerUserEntity b) {
                return Integer.compare(a.getNxDistributerUserId(), b.getNxDistributerUserId());
            }
        });
        return users;
    }

    @Override
    public void requireDriverOnDuty(Integer disId, Integer driverUserId, String routeDate, String driverName) {
        String dutyDate = resolveDutyDate(routeDate);
        NxDisDriverDutyEntity duty = nxDisDriverDutyDao.queryByDisDriverDate(disId, driverUserId, dutyDate);
        if (duty == null || !ON_DUTY.equals(duty.getNxDddDutyStatus())) {
            String label = driverName != null && !driverName.trim().isEmpty()
                    ? driverName.trim() : String.valueOf(driverUserId);
            throw new IllegalArgumentException("司机当前不可派，不能参与派车：" + label);
        }
    }

    private void validateDutyRequest(DriverDutyRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private NxDistributerUserEntity requireDriverAccount(Integer disId, Integer driverUserId) {
        NxDistributerUserEntity driver = nxDistributerUserDao.queryObject(driverUserId);
        if (driver == null) {
            throw new IllegalArgumentException("司机不存在: " + driverUserId);
        }
        if (!disId.equals(driver.getNxDiuDistributerId())) {
            throw new IllegalArgumentException("司机不属于该配送商: " + driverUserId);
        }
        if (!getNxDisUserDriver().equals(driver.getNxDiuAdmin())) {
            throw new IllegalArgumentException("用户不是司机角色: " + driverUserId);
        }
        return driver;
    }

    private String resolveDutyDate(String dutyDate) {
        if (dutyDate != null && !dutyDate.trim().isEmpty()) {
            return dutyDate.trim();
        }
        return formatWhatDay(0);
    }
}
