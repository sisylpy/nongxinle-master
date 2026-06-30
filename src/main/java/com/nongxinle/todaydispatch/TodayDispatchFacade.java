package com.nongxinle.todaydispatch;

import com.nongxinle.dto.route.SandboxDriverRouteEditConfirmRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditPageRequest;
import com.nongxinle.dto.route.SandboxStopConfirmRequest;
import com.nongxinle.dto.route.SandboxStopReturnToSandboxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** 分派中页唯一对外入口：compute → assemble。 */
@Service
public class TodayDispatchFacade {

    @Autowired
    private TodayDispatchComputeService todayDispatchComputeService;

    @Autowired
    private DispatchPageAssembler dispatchPageAssembler;

    @Autowired
    private TodayDispatchRouteConfirmService todayDispatchRouteConfirmService;

    @Autowired
    private TodayDispatchRouteEditService todayDispatchRouteEditService;

    public Map<String, Object> buildDispatchPage(Integer disId,
                                                 String routeDate,
                                                 String batchCode,
                                                 Integer operatorUserId) throws Exception {
        TodayDispatchResult result = todayDispatchComputeService.compute(
                disId, routeDate, batchCode, operatorUserId);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", dispatchPageAssembler.assemble(result));
        return data;
    }

    public Map<String, Object> buildLoadingPage(Integer disId,
                                                String routeDate,
                                                String batchCode,
                                                Integer operatorUserId) throws Exception {
        TodayDispatchResult result = todayDispatchComputeService.computeLoading(
                disId, routeDate, batchCode, operatorUserId);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", dispatchPageAssembler.assemble(result));
        return data;
    }

    public Map<String, Object> buildDeliveryPage(Integer disId,
                                                 String routeDate,
                                                 String batchCode,
                                                 Integer driverUserId) throws Exception {
        TodayDispatchResult result = todayDispatchComputeService.computeDelivery(
                disId, routeDate, batchCode, null);
        Map<String, Object> pageViewModel = dispatchPageAssembler.assemble(result);
        if (driverUserId != null) {
            pageViewModel = dispatchPageAssembler.filterDeliveryPageViewModelByDriver(pageViewModel, driverUserId);
        }
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pageViewModel", pageViewModel);
        return data;
    }

    public Map<String, Object> confirmStop(SandboxStopConfirmRequest request) throws Exception {
        return todayDispatchRouteConfirmService.confirmStop(request);
    }

    public Map<String, Object> confirmDriverRouteEdit(SandboxDriverRouteEditConfirmRequest request)
            throws Exception {
        return todayDispatchRouteConfirmService.confirmDriverRouteEdit(request);
    }

    public Map<String, Object> returnStopToSandbox(Integer deliveryStopId,
                                                   SandboxStopReturnToSandboxRequest request)
            throws Exception {
        return todayDispatchRouteConfirmService.returnStopToSandbox(deliveryStopId, request);
    }

    public Map<String, Object> buildDriverRouteEditPage(SandboxDriverRouteEditPageRequest request)
            throws Exception {
        return todayDispatchRouteEditService.buildPage(request);
    }

    public Map<String, Object> previewDriverRouteEdit(SandboxDriverRouteEditPageRequest request)
            throws Exception {
        return todayDispatchRouteEditService.preview(request);
    }
}
