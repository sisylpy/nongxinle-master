package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import com.nongxinle.community.dispatch.dto.CommunityDriverDutyRequest;
import com.nongxinle.dao.NxCommunityDispatchDriverDutyDao;
import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.entity.NxCommunityDispatchDriverDutyEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class CommunityDriverDutyService {

    @Autowired
    private NxCommunityDispatchDriverDutyDao nxCommunityDispatchDriverDutyDao;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;
    @Autowired
    private CommunityDispatchDutyLockHelper communityDispatchDutyLockHelper;

    public NxCommunityDispatchDriverDutyEntity checkIn(CommunityDriverDutyRequest request) {
        validateDutyRequest(request);
        requireDriverAccount(request.getCommunityId(), request.getDriverUserId());
        String dutyDate = resolveDutyDate(request.getDutyDate());
        Date now = new Date();

        NxCommunityDispatchDriverDutyEntity entity = new NxCommunityDispatchDriverDutyEntity();
        entity.setNxCddCommunityId(request.getCommunityId());
        entity.setNxCddDriverUserId(request.getDriverUserId());
        entity.setNxCddRouteDate(dutyDate);
        entity.setNxCddStatus(CommunityDispatchConstants.DUTY_ON);
        entity.setNxCddCheckedInAt(now);
        entity.setNxCddCheckedOutAt(null);
        nxCommunityDispatchDriverDutyDao.upsertCheckIn(entity);
        return entity;
    }

    public NxCommunityDispatchDriverDutyEntity checkOut(CommunityDriverDutyRequest request) {
        validateDutyRequest(request);
        requireDriverAccount(request.getCommunityId(), request.getDriverUserId());
        String dutyDate = resolveDutyDate(request.getDutyDate());
        String lockReason = communityDispatchDutyLockHelper.resolveDutyLockReason(
                request.getCommunityId(), dutyDate, request.getDriverUserId());
        if (lockReason != null) {
            throw new IllegalArgumentException(lockReason);
        }
        Date now = new Date();

        NxCommunityDispatchDriverDutyEntity entity = new NxCommunityDispatchDriverDutyEntity();
        entity.setNxCddCommunityId(request.getCommunityId());
        entity.setNxCddDriverUserId(request.getDriverUserId());
        entity.setNxCddRouteDate(dutyDate);
        entity.setNxCddStatus(CommunityDispatchConstants.DUTY_OFF);
        entity.setNxCddCheckedOutAt(now);
        nxCommunityDispatchDriverDutyDao.upsertCheckOut(entity);
        return entity;
    }

    public List<NxCommunityUserEntity> listOnDutyDriverUsers(Integer communityId, String routeDate) {
        if (communityId == null) {
            return Collections.emptyList();
        }
        String dutyDate = resolveDutyDate(routeDate);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("communityId", communityId);
        map.put("routeDate", dutyDate);
        List<NxCommunityUserEntity> drivers = nxCommunityDispatchDriverDutyDao.queryOnDutyDriverUsers(map);
        return drivers != null ? drivers : Collections.<NxCommunityUserEntity>emptyList();
    }

    public List<NxCommunityUserEntity> listAllDriverUsers(Integer communityId) {
        if (communityId == null) {
            return Collections.emptyList();
        }
        List<NxCommunityUserEntity> drivers = nxCommunityUserDao.getDriverUsersByComId(communityId);
        return drivers != null ? drivers : Collections.<NxCommunityUserEntity>emptyList();
    }

    public Map<Integer, NxCommunityDispatchDriverDutyEntity> loadDutyByDriver(
            Integer communityId, String routeDate) {
        Map<Integer, NxCommunityDispatchDriverDutyEntity> map =
                new HashMap<Integer, NxCommunityDispatchDriverDutyEntity>();
        if (communityId == null) {
            return map;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("communityId", communityId);
        params.put("routeDate", resolveDutyDate(routeDate));
        List<NxCommunityDispatchDriverDutyEntity> duties =
                nxCommunityDispatchDriverDutyDao.queryByCommunityDate(params);
        if (duties == null) {
            return map;
        }
        for (NxCommunityDispatchDriverDutyEntity duty : duties) {
            if (duty != null && duty.getNxCddDriverUserId() != null) {
                map.put(duty.getNxCddDriverUserId(), duty);
            }
        }
        return map;
    }

    public List<Map<String, Object>> buildDriverDutyCards(Integer communityId, String routeDate) {
        List<NxCommunityUserEntity> drivers = listAllDriverUsers(communityId);
        Map<Integer, NxCommunityDispatchDriverDutyEntity> dutyByDriver =
                loadDutyByDriver(communityId, routeDate);
        String resolvedDate = resolveDutyDate(routeDate);
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        for (NxCommunityUserEntity driver : drivers) {
            if (driver == null || driver.getNxCommunityUserId() == null) {
                continue;
            }
            NxCommunityDispatchDriverDutyEntity duty =
                    dutyByDriver.get(driver.getNxCommunityUserId());
            boolean onDuty = duty != null
                    && CommunityDispatchConstants.DUTY_ON.equals(duty.getNxCddStatus());
            String toggleDisabledReason = null;
            boolean canToggleDuty = true;
            if (onDuty) {
                toggleDisabledReason = communityDispatchDutyLockHelper.resolveDutyLockReason(
                        communityId, resolvedDate, driver.getNxCommunityUserId());
                if (toggleDisabledReason != null) {
                    canToggleDuty = false;
                }
            }
            Map<String, Object> card = new LinkedHashMap<String, Object>();
            card.put("driverUserId", driver.getNxCommunityUserId());
            card.put("driverName", driver.getNxCouWxNickName());
            card.put("driverPhone", driver.getNxCouWxPhone());
            card.put("dutyStatus", onDuty ? CommunityDispatchConstants.DUTY_ON
                    : CommunityDispatchConstants.DUTY_OFF);
            card.put("dutyStatusLabel", onDuty ? "可派" : "不可派");
            card.put("canToggleDuty", canToggleDuty);
            card.put("toggleDisabledReason", toggleDisabledReason);
            cards.add(card);
        }
        return cards;
    }

    public void requireDriverOnDuty(Integer communityId, Integer driverUserId,
                                    String routeDate, String driverName) {
        String dutyDate = resolveDutyDate(routeDate);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("communityId", communityId);
        map.put("driverUserId", driverUserId);
        map.put("routeDate", dutyDate);
        NxCommunityDispatchDriverDutyEntity duty =
                nxCommunityDispatchDriverDutyDao.queryByCommunityDriverDate(map);
        if (duty == null || !CommunityDispatchConstants.DUTY_ON.equals(duty.getNxCddStatus())) {
            String label = driverName != null && !driverName.trim().isEmpty()
                    ? driverName.trim() : String.valueOf(driverUserId);
            throw new IllegalArgumentException("司机当前不可派，不能参与派车：" + label);
        }
    }

    private void validateDutyRequest(CommunityDriverDutyRequest request) {
        if (request == null || request.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (request.getDriverUserId() == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
        if (request.getOperatorUserId() == null) {
            throw new IllegalArgumentException("operatorUserId 不能为空");
        }
    }

    private NxCommunityUserEntity requireDriverAccount(Integer communityId, Integer driverUserId) {
        NxCommunityUserEntity driver = nxCommunityUserDao.queryObject(driverUserId);
        if (driver == null) {
            throw new IllegalArgumentException("司机不存在: " + driverUserId);
        }
        if (!communityId.equals(driver.getNxCouCommunityId())) {
            throw new IllegalArgumentException("司机不属于该门店: " + driverUserId);
        }
        if (!CommunityDispatchConstants.isCommunityDriverUser(driver)) {
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
