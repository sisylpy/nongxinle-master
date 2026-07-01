package com.nongxinle.community.dispatch.controller;

import com.nongxinle.community.dispatch.dto.*;
import com.nongxinle.community.dispatch.service.CommunityDispatchFacade;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@RestController
@RequestMapping("api/nxcommunitydispatch")
public class CommunityDispatchController {

    @Autowired
    private CommunityDispatchFacade communityDispatchFacade;

    @RequestMapping(value = "/drivers/available", method = RequestMethod.GET)
    @ResponseBody
    public R listAvailableDrivers(@RequestParam Integer communityId,
                                  @RequestParam(required = false) String routeDate) {
        try {
            return R.ok().put("data", communityDispatchFacade.listAvailableDrivers(
                    communityId, normalizeRouteDate(routeDate)));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/dispatch/sandbox/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchSandboxToday(@RequestParam Integer communityId,
                                     @RequestParam(required = false) String routeDate) {
        try {
            return R.ok().put("data", communityDispatchFacade.buildSandboxPage(
                    communityId, normalizeRouteDate(routeDate)));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/dispatch/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchLoadingToday(@RequestParam Integer communityId,
                                     @RequestParam(required = false) String routeDate) {
        try {
            return R.ok().put("data", communityDispatchFacade.buildLoadingPage(
                    communityId, normalizeRouteDate(routeDate)));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDispatchDeliveryToday(@RequestParam Integer communityId,
                                      @RequestParam(required = false) String routeDate,
                                      @RequestParam(required = false) Integer driverUserId) {
        try {
            return R.ok().put("data", communityDispatchFacade.buildDeliveryPage(
                    communityId, normalizeRouteDate(routeDate), driverUserId));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/sandbox/stops/confirm", method = RequestMethod.POST)
    @ResponseBody
    public R confirmSandboxStop(@RequestBody CommunitySandboxStopConfirmRequest request) {
        try {
            return R.ok().put("data", communityDispatchFacade.confirmStop(request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/driver-terminal/loading/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalLoadingToday(@RequestParam Integer communityId,
                                           @RequestParam Integer driverUserId,
                                           @RequestParam(required = false) String routeDate) {
        try {
            return R.ok().put("data", communityDispatchFacade.buildDriverLoadingPage(
                    communityId, normalizeRouteDate(routeDate), driverUserId));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/driver-terminal/delivery/today", method = RequestMethod.GET)
    @ResponseBody
    public R getDriverTerminalDeliveryToday(@RequestParam Integer communityId,
                                            @RequestParam Integer driverUserId,
                                            @RequestParam(required = false) String routeDate) {
        try {
            return R.ok().put("data", communityDispatchFacade.buildDriverDeliveryPage(
                    communityId, normalizeRouteDate(routeDate), driverUserId));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/on", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckIn(@PathVariable Integer driverUserId,
                           @RequestBody CommunityDriverDutyRequest request) {
        try {
            return R.ok().put("data", communityDispatchFacade.driverCheckIn(driverUserId, request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/duty/off", method = RequestMethod.POST)
    @ResponseBody
    public R driverCheckOut(@PathVariable Integer driverUserId,
                            @RequestBody CommunityDriverDutyRequest request) {
        try {
            return R.ok().put("data", communityDispatchFacade.driverCheckOut(driverUserId, request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/drivers/{driverUserId}/depart-now", method = RequestMethod.POST)
    @ResponseBody
    public R departDriverNow(@PathVariable Integer driverUserId,
                             @RequestBody(required = false) CommunityDriverDepartRequest request) {
        try {
            if (request == null) {
                request = new CommunityDriverDepartRequest();
            }
            if (request.getDriverUserId() == null) {
                request.setDriverUserId(driverUserId);
            }
            return R.ok().put("data", communityDispatchFacade.departNow(driverUserId, request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/delivery/stops/{stopId}/complete-now", method = RequestMethod.POST)
    @ResponseBody
    public R completeDeliveryStopNow(@PathVariable Integer stopId,
                                     @RequestBody(required = false) CommunityDeliveryCompleteRequest request) {
        try {
            return R.ok().put("data", communityDispatchFacade.completeStopNow(stopId, request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/loading/stops/{stopId}/return-to-sandbox", method = RequestMethod.POST)
    @ResponseBody
    public R returnLoadingStopToSandbox(@PathVariable Integer stopId,
                                        @RequestBody CommunityLoadingStopRemoveRequest request) {
        try {
            return R.ok().put("data", communityDispatchFacade.returnLoadingStopToSandbox(stopId, request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/page", method = RequestMethod.POST)
    @ResponseBody
    public R buildDriverRouteEditPage(@RequestBody CommunityDriverRouteEditPageRequest request) {
        try {
            if (request != null && (request.getRouteDate() == null || request.getRouteDate().trim().isEmpty())) {
                request.setRouteDate(normalizeRouteDate(null));
            }
            return R.ok().put("data", communityDispatchFacade.buildDriverRouteEditPage(request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/preview", method = RequestMethod.POST)
    @ResponseBody
    public R previewDriverRouteEdit(@RequestBody CommunityDriverRouteEditPageRequest request) {
        try {
            if (request != null && (request.getRouteDate() == null || request.getRouteDate().trim().isEmpty())) {
                request.setRouteDate(normalizeRouteDate(null));
            }
            return R.ok().put("data", communityDispatchFacade.previewDriverRouteEdit(request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    @RequestMapping(value = "/sandbox/driver-route-edit/confirm", method = RequestMethod.POST)
    @ResponseBody
    public R confirmDriverRouteEdit(@RequestBody CommunityDriverRouteEditConfirmRequest request) {
        try {
            if (request != null && (request.getRouteDate() == null || request.getRouteDate().trim().isEmpty())) {
                request.setRouteDate(normalizeRouteDate(null));
            }
            return R.ok().put("data", communityDispatchFacade.confirmDriverRouteEdit(request));
        } catch (Exception e) {
            return R.error(formatError(e));
        }
    }

    private String normalizeRouteDate(String routeDate) {
        if (routeDate == null || routeDate.trim().isEmpty()) {
            return formatWhatDay(0);
        }
        return routeDate;
    }

    private String formatError(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
