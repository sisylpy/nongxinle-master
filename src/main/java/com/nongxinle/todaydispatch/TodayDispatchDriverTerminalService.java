package com.nongxinle.todaydispatch;

import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.entity.NxDistributerUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/** 司机终端读 — 走 todaydispatch 简单 DB 读，不经老 terminal/compute 链。 */
@Service("todayDispatchDriverTerminalService")
public class TodayDispatchDriverTerminalService {

    @Autowired
    private TodayDispatchComputeService todayDispatchComputeService;

    @Autowired
    private DriverTerminalPageAssembler driverTerminalPageAssembler;

    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;

    public Map<String, Object> buildDriverLoadingToday(Integer disId,
                                                       String routeDate,
                                                       String batchCode,
                                                       Integer driverUserId) throws Exception {
        validate(disId, driverUserId);
        TodayDispatchResult result = todayDispatchComputeService.computeLoading(
                disId, routeDate, batchCode, null);
        String driverName = resolveDriverName(driverUserId);
        Map<String, Object> pageViewModel = driverTerminalPageAssembler.assembleLoading(
                result, driverUserId, driverName);
        return DriverTerminalPageAssembler.wrapResponse(
                disId, result.getRouteDate(), result.getBatchCode(), driverUserId, driverName, pageViewModel);
    }

    public Map<String, Object> buildDriverDeliveryToday(Integer disId,
                                                        String routeDate,
                                                        String batchCode,
                                                        Integer driverUserId) throws Exception {
        validate(disId, driverUserId);
        TodayDispatchResult result = todayDispatchComputeService.computeDelivery(
                disId, routeDate, batchCode, null);
        String driverName = resolveDriverName(driverUserId);
        Map<String, Object> pageViewModel = driverTerminalPageAssembler.assembleDelivery(
                result, driverUserId, driverName);
        return DriverTerminalPageAssembler.wrapResponse(
                disId, result.getRouteDate(), result.getBatchCode(), driverUserId, driverName, pageViewModel);
    }

    private String resolveDriverName(Integer driverUserId) {
        NxDistributerUserEntity user = nxDistributerUserDao.queryObject(driverUserId);
        if (user != null && user.getNxDiuWxNickName() != null && !user.getNxDiuWxNickName().trim().isEmpty()) {
            return user.getNxDiuWxNickName().trim();
        }
        return String.valueOf(driverUserId);
    }

    private static void validate(Integer disId, Integer driverUserId) {
        if (disId == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        if (driverUserId == null) {
            throw new IllegalArgumentException("driverUserId 不能为空");
        }
    }
}
