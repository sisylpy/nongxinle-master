package com.nongxinle.dispatch.controller;

import com.nongxinle.dto.route.DriverDeliveryNowRequest;
import com.nongxinle.dto.route.DriverDutyRequest;
import com.nongxinle.dto.route.DriverRouteDepartRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditConfirmRequest;
import com.nongxinle.dto.route.SandboxDriverRouteEditPageRequest;
import com.nongxinle.dto.route.SandboxStopConfirmRequest;
import com.nongxinle.dto.route.SandboxStopReturnToSandboxRequest;
import com.nongxinle.dto.route.SandboxStopTimeWindowRequest;
import com.nongxinle.service.DisDriverDutyService;
import com.nongxinle.service.DisRouteDispatchService;
import com.nongxinle.service.DisRouteSandboxDriverDeliveryNowService;
import com.nongxinle.service.DisRouteSandboxDriverDepartNowService;
import com.nongxinle.service.DisRouteSandboxStopTimeWindowService;
import com.nongxinle.dto.route.SandboxManualDispatchBaseRequest;
import com.nongxinle.todaydispatch.TodayDispatchDriverListService;
import com.nongxinle.todaydispatch.TodayDispatchDriverTerminalService;
import com.nongxinle.todaydispatch.TodayDispatchFacade;
import com.nongxinle.todaydispatch.TodayDispatchManualDispatchService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

/**
 * 今日派单 — 老板端 / 司机端小程序正式接口（15 个，与 apiRouteDispatch.js 一致）。
 */
@RestController
@RequestMapping("api/nxdisroutedispatch")
public class TodayDispatchController {

    @Autowired
    private DisRouteDispatchService disRouteDispatchService;
    @Autowired
    private DisDriverDutyService disDriverDutyService;
    @Autowired
    private TodayDispatchDriverListService todayDispatchDriverListService;
    @Autowired
    private TodayDispatchFacade todayDispatchFacade;
    @Autowired
    private DisRouteSandboxDriverDepartNowService disRouteSandboxDriverDepartNowService;
    @Autowired
    private DisRouteSandboxDriverDeliveryNowService disRouteSandboxDriverDeliveryNowService;
    @Autowired
    private TodayDispatchDriverTerminalService todayDispatchDriverTerminalService;
    @Autowired
    private DisRouteSandboxStopTimeWindowService disRouteSandboxStopTimeWindowService;
    @Autowired
    private TodayDispatchManualDispatchService todayDispatchManualDispatchService;

    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    @ResponseBody
    public R listDrivers(@RequestParam Integer disId) {
        try {
            return R.ok().put("data", disRouteDispatchService.listDrivers(disId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/drivers/available", method = RequestMethod.GET)
    @ResponseBody
    public R listAvailableDrivers(@RequestParam Integer disId,
                                  @RequestParam(required = false) String routeDate,
                                  @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {

            return R.ok().put("data", todayDispatchDriverListService.listDriversForBatch(
                    disId, routeDate, batchCode));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/on", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckIn(@PathVariable Integer driverUserId, @RequestBody DriverDutyRequest request) {
        try {
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disDriverDutyService.checkIn(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/off", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckOut(@PathVariable Integer driverUserId, @RequestBody DriverDutyRequest request) {
        try {
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disDriverDutyService.checkOut(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/dispatch/sandbox/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchSandboxToday(@RequestParam Integer disId,
                                     @RequestParam(required = false) String routeDate,
                                     @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                     @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = effectiveRouteDate(routeDate);
            return R.ok().put("data", todayDispatchFacade.buildDispatchPage(
                    disId, effectiveRouteDate, batchCode, operatorUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/dispatch/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchLoadingToday(@RequestParam Integer disId,
                                     @RequestParam(required = false) String routeDate,
                                     @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                     @RequestParam(required = false) Integer operatorUserId) {
        try {
            String effectiveRouteDate = effectiveRouteDate(routeDate);
            return R.ok().put("data", todayDispatchFacade.buildLoadingPage(
                    disId, effectiveRouteDate, batchCode, operatorUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchDeliveryToday(@RequestParam Integer disId,
                                      @RequestParam(required = false) String routeDate,
                                      @RequestParam(required = false, defaultValue = "MORNING") String batchCode,
                                      @RequestParam(required = false) Integer driverUserId) {
        try {
            String effectiveRouteDate = effectiveRouteDate(routeDate);
            return R.ok().put("data", todayDispatchFacade.buildDeliveryPage(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/dispatch/sandbox/stops/time-window", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R updateSandboxStopTimeWindow(@RequestBody SandboxStopTimeWindowRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxStopTimeWindowService.updateStopTimeWindow(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/stops/confirm", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmSandboxStop(@RequestBody SandboxStopConfirmRequest request) {
        try {
            return R.ok().put("data", todayDispatchFacade.confirmStop(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/stops/{deliveryStopId}/return-to-sandbox", method = RequestMethod.POST)
    @ResponseBody
    public R returnSandboxStopToSandbox(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                        @RequestBody SandboxStopReturnToSandboxRequest request) {
        try {
            return R.ok().put("data", todayDispatchFacade.returnStopToSandbox(deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/driver-terminal/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalLoadingToday(@RequestParam Integer disId,
                                           @RequestParam Integer driverUserId,
                                           @RequestParam(required = false) String routeDate,
                                           @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            String effectiveRouteDate = effectiveRouteDate(routeDate);
            return R.ok().put("data", todayDispatchDriverTerminalService.buildDriverLoadingToday(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/driver-terminal/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalDeliveryToday(@RequestParam Integer disId,
                                            @RequestParam Integer driverUserId,
                                            @RequestParam(required = false) String routeDate,
                                            @RequestParam(required = false, defaultValue = "MORNING") String batchCode) {
        try {
            String effectiveRouteDate = effectiveRouteDate(routeDate);
            return R.ok().put("data", todayDispatchDriverTerminalService.buildDriverDeliveryToday(
                    disId, effectiveRouteDate, batchCode, driverUserId));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/depart-now", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R departDriverNow(@PathVariable("driverUserId") Integer driverUserId,
                             @RequestBody DriverRouteDepartRequest request) {
        try {
            if (request == null) {
                request = new DriverRouteDepartRequest();
            }
            request.setDriverUserId(driverUserId);
            return R.ok().put("data", disRouteSandboxDriverDepartNowService.departNow(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/delivery/stops/{deliveryStopId}/complete-now", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R completeDeliveryStopNow(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                     @RequestBody DriverDeliveryNowRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverDeliveryNowService.completeNow(deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/stops/{deliveryStopId}/return-to-sandbox-now", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R returnSandboxStopNow(@PathVariable("deliveryStopId") Integer deliveryStopId,
                                  @RequestBody DriverDeliveryNowRequest request) {
        try {
            return R.ok().put("data", disRouteSandboxDriverDeliveryNowService.returnToSandboxNow(
                    deliveryStopId, request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/manual-dispatch/driver-panorama", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R listManualDispatchDriverPanorama(@RequestBody SandboxManualDispatchBaseRequest request) {
        try {
            return R.ok().put("data", todayDispatchManualDispatchService.listDriverPanorama(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/page", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R buildDriverRouteEditPage(@RequestBody SandboxDriverRouteEditPageRequest request) {
        try {
            return R.ok().put("data", todayDispatchFacade.buildDriverRouteEditPage(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/preview", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R previewDriverRouteEdit(@RequestBody SandboxDriverRouteEditPageRequest request) {
        try {
            return R.ok().put("data", todayDispatchFacade.previewDriverRouteEdit(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/confirm", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public R confirmDriverRouteEdit(@RequestBody SandboxDriverRouteEditConfirmRequest request) {
        try {
            return R.ok().put("data", todayDispatchFacade.confirmDriverRouteEdit(request));
        } catch (Exception e) {
            return R.error(formatDispatchError(e));
        }
    }

    private static String effectiveRouteDate(String routeDate) {
        return routeDate != null && !routeDate.trim().isEmpty()
                ? routeDate.trim() : formatWhatDay(0);
    }

    private static String formatDispatchError(Exception e) {
        if (e == null) {
            return "未知错误";
        }
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return e.getClass().getSimpleName();
    }
}
