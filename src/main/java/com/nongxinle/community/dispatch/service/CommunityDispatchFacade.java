package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.dto.*;
import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchPageViewAdapter;
import com.nongxinle.entity.NxCommunityDispatchDriverDutyEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.community.dispatch.constants.CommunityDispatchConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private CommunityDriverDutyService communityDriverDutyService;
    @Autowired
    private CommunityDispatchRouteEditService communityDispatchRouteEditService;

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
                communityDispatchComputeService.computeDriverLoading(communityId, routeDate, driverUserId),
                CommunityDispatchPageViewAdapter.AdapterOptions.driverLoading()));
        return data;
    }

    public Map<String, Object> buildDriverDeliveryPage(Integer communityId, String routeDate, Integer driverUserId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageViewModel", communityDispatchPageAssembler.assemble(
                communityDispatchComputeService.computeDriverDelivery(communityId, routeDate, driverUserId),
                CommunityDispatchPageViewAdapter.AdapterOptions.driverDelivery()));
        return data;
    }

    public Map<String, Object> listAvailableDrivers(Integer communityId, String routeDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            routeDate = formatWhatDay(0);
        }
        List<NxCommunityUserEntity> onDuty = communityDriverDutyService.listOnDutyDriverUsers(
                communityId, routeDate);
        List<Map<String, Object>> drivers = new ArrayList<Map<String, Object>>();
        for (NxCommunityUserEntity driver : onDuty) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("driverUserId", driver.getNxCommunityUserId());
            item.put("driverName", driver.getNxCouWxNickName());
            item.put("driverPhone", driver.getNxCouWxPhone());
            item.put("dutyStatus", CommunityDispatchConstants.DUTY_ON);
            drivers.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("routeDate", routeDate);
        data.put("drivers", drivers);
        data.put("driverCards", communityDriverDutyService.buildDriverDutyCards(communityId, routeDate));
        return data;
    }

    public NxCommunityDispatchDriverDutyEntity driverCheckIn(Integer driverUserId,
                                                             CommunityDriverDutyRequest request) {
        if (request == null) {
            request = new CommunityDriverDutyRequest();
        }
        if (request.getDriverUserId() == null) {
            request.setDriverUserId(driverUserId);
        }
        return communityDriverDutyService.checkIn(request);
    }

    public NxCommunityDispatchDriverDutyEntity driverCheckOut(Integer driverUserId,
                                                              CommunityDriverDutyRequest request) {
        if (request == null) {
            request = new CommunityDriverDutyRequest();
        }
        if (request.getDriverUserId() == null) {
            request.setDriverUserId(driverUserId);
        }
        return communityDriverDutyService.checkOut(request);
    }

    public Map<String, Object> confirmStop(CommunitySandboxStopConfirmRequest request) {
        return communityDispatchConfirmService.confirmStop(request);
    }

    public Map<String, Object> departNow(Integer driverUserId, CommunityDriverDepartRequest request) {
        return communityDispatchExecutionService.departNow(driverUserId, request);
    }

    public Map<String, Object> completeStopNow(Integer stopId, CommunityDeliveryCompleteRequest request) {
        return communityDispatchExecutionService.completeStopNow(stopId, request);
    }

    public Map<String, Object> buildDriverRouteEditPage(CommunityDriverRouteEditPageRequest request) {
        return communityDispatchRouteEditService.buildPage(request);
    }

    public Map<String, Object> previewDriverRouteEdit(CommunityDriverRouteEditPageRequest request) {
        return communityDispatchRouteEditService.preview(request);
    }

    public Map<String, Object> confirmDriverRouteEdit(CommunityDriverRouteEditConfirmRequest request) {
        return communityDispatchRouteEditService.confirm(request);
    }

    public Map<String, Object> returnLoadingStopToSandbox(Integer stopId,
                                                        CommunityLoadingStopRemoveRequest request) {
        return communityDispatchConfirmService.returnStopToSandbox(stopId, request);
    }
}
