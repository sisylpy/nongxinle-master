package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.dto.*;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.entity.NxCommunityUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nongxinle.community.dispatch.constants.CommunityDispatchConstants.DRIVER_ROLE_ID;
import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class CommunityDispatchFacade {

    @Autowired
    private CommunityDispatchComputeService communityDispatchComputeService;
    @Autowired
    private CommunityDispatchPageAssembler communityDispatchPageAssembler;
    @Autowired
    private CommunityDispatchConfirmService communityDispatchConfirmService;
    @Autowired
    private CommunityDispatchExecutionService communityDispatchExecutionService;
    @Autowired
    private NxCommunityUserDao nxCommunityUserDao;

    public Map<String, Object> buildSandboxPage(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult result = communityDispatchComputeService.computeSandbox(communityId, routeDate);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(result));
        return data;
    }

    public Map<String, Object> buildLoadingPage(Integer communityId, String routeDate) {
        CommunityDispatchSandboxResult result = communityDispatchComputeService.computeLoading(communityId, routeDate);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(result));
        return data;
    }

    public Map<String, Object> buildDeliveryPage(Integer communityId, String routeDate, Integer driverUserId) {
        CommunityDispatchSandboxResult result = communityDispatchComputeService.computeDelivery(communityId, routeDate);
        if (driverUserId != null) {
            result = communityDispatchComputeService.computeDriverDelivery(communityId, routeDate, driverUserId);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(result));
        return data;
    }

    public Map<String, Object> buildDriverLoadingPage(Integer communityId, String routeDate, Integer driverUserId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeDriverLoading(communityId, routeDate, driverUserId)));
        return data;
    }

    public Map<String, Object> buildDriverDeliveryPage(Integer communityId, String routeDate, Integer driverUserId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeDriverDelivery(communityId, routeDate, driverUserId)));
        return data;
    }

    public List<Map<String, Object>> listAvailableDrivers(Integer communityId, String routeDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId);
        map.put("roleId", DRIVER_ROLE_ID);
        List<NxCommunityUserEntity> drivers = nxCommunityUserDao.queryCommunityRoleUsers(map);
        List<Map<String, Object>> list = new ArrayList<>();
        for (NxCommunityUserEntity driver : drivers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("driverUserId", driver.getNxCommunityUserId());
            item.put("driverName", driver.getNxCouWxNickName());
            item.put("driverPhone", driver.getNxCouWxPhone());
            list.add(item);
        }
        return list;
    }

    public Map<String, Object> confirmStop(CommunitySandboxStopConfirmRequest request) {
        return communityDispatchConfirmService.confirmStop(request);
    }

    public Map<String, Object> returnStopToSandbox(Integer stopId, CommunitySandboxStopReturnRequest request) {
        return communityDispatchConfirmService.returnStopToSandbox(stopId, request);
    }

    public Map<String, Object> departNow(Integer driverUserId, CommunityDriverDepartRequest request) {
        return communityDispatchExecutionService.departNow(driverUserId, request);
    }

    public Map<String, Object> completeStopNow(Integer stopId, CommunityDeliveryCompleteRequest request) {
        return communityDispatchExecutionService.completeStopNow(stopId, request);
    }
}
