package com.nongxinle.todaydispatch;

import com.nongxinle.entity.NxDisDriverRouteEntity;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static com.nongxinle.route.DisRouteSandboxDriverDispatchPhase.LOADING;
import static com.nongxinle.route.DisShipmentTaskStatus.EXCEPTION;
import static com.nongxinle.route.DisShipmentTaskStatus.IN_DELIVERY;

public class DriverDispatchStateServiceTest {

    private static final Integer LOADING_DRIVER = 1;

    private final DriverDispatchStateService service = new DriverDispatchStateService();

    @Test
    public void canReceiveNewStops_nullDriverReturnsFalse() {
        Assert.assertFalse(service.canReceiveNewStops(null, query()));
    }

    @Test
    public void resolveRoutePhase_loadingEnteredAtIsLoading() {
        NxDisDriverRouteEntity route = loadingRoute(LOADING_DRIVER);
        Assert.assertEquals(LOADING, service.resolveRoutePhase(route, query()));
        Assert.assertFalse(service.canRouteAcceptEphemeralStops(route, query()));
        Assert.assertTrue(service.blocksAvailableIdleSlot(route, query()));
    }

    @Test
    public void pageProjector_loadingRouteBlocksEphemeralAcceptance() {
        NxDisDriverRouteEntity route = loadingRoute(LOADING_DRIVER);
        Assert.assertFalse(service.canRouteAcceptEphemeralStops(route, query()));
    }

    @Test
    public void blocksAvailableIdleSlotByTaskStop_inDeliveryOrExceptionBlocks() {
        NxDisShipmentTaskEntity inDelivery = taskWithStatus(IN_DELIVERY);
        NxDisRouteStopEntity stop = stopWithTask(inDelivery);
        Assert.assertTrue(service.blocksAvailableIdleSlotByTaskStop(stop, query()));

        NxDisShipmentTaskEntity exception = taskWithStatus(EXCEPTION);
        Assert.assertTrue(service.blocksAvailableIdleSlotByTaskStop(stopWithTask(exception), query()));

        NxDisShipmentTaskEntity delivered = taskWithStatus(
                com.nongxinle.route.DisShipmentTaskStatus.DELIVERED);
        Assert.assertFalse(service.blocksAvailableIdleSlotByTaskStop(stopWithTask(delivered), query()));
    }

    private static NxDisShipmentTaskEntity taskWithStatus(String status) {
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstStatus(status);
        return task;
    }

    private static NxDisRouteStopEntity stopWithTask(NxDisShipmentTaskEntity task) {
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setShipmentTask(task);
        return stop;
    }

    private static DriverDispatchStateService.StateQuery query() {
        return DriverDispatchStateService.StateQuery.of(null, null, null, null, new Date());
    }

    private static NxDisDriverRouteEntity loadingRoute(Integer driverUserId) {
        NxDisDriverRouteEntity route = new NxDisDriverRouteEntity();
        route.setNxDdrDriverUserId(driverUserId);
        route.setNxDdrLoadingEnteredAt(new Date());
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstStatus(com.nongxinle.route.DisShipmentTaskStatus.ASSIGNED);
        stop.setShipmentTask(task);
        route.setStops(Collections.singletonList(stop));
        return route;
    }
}
