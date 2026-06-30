package com.nongxinle.route;

import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import org.junit.Assert;
import org.junit.Test;

import static com.nongxinle.route.DisRouteSandboxStopSource.SANDBOX_SUGGESTED;
import static com.nongxinle.route.DisShipmentTaskStatus.SIMULATED;

/** 非 eligibility 工具单测。 */
public class DisRouteSandboxDriverDispatchStateHelperTest {

    @Test
    public void resolveStopDriverUserId_readsSuggestedThenTaskFields() {
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstSuggestedDriverUserId(294);
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setShipmentTask(task);
        stop.setSuggestedDriverUserId(306);
        Assert.assertEquals(Integer.valueOf(306), DisRouteSandboxDriverDispatchStateHelper.resolveStopDriverUserId(stop));
    }

    @Test
    public void orphanStopHasNoDriverAfterBlockedAssignment() {
        NxDisShipmentTaskEntity task = new NxDisShipmentTaskEntity();
        task.setNxDstDepFatherId(1517);
        task.setNxDstStatus(SIMULATED);
        NxDisRouteStopEntity stop = new NxDisRouteStopEntity();
        stop.setShipmentTask(task);
        stop.setStopSource(SANDBOX_SUGGESTED);
        stop.setSuggestedDriverUserId(null);
        task.setNxDstSuggestedDriverUserId(null);
        Assert.assertNull(stop.getSuggestedDriverUserId());
    }
}
