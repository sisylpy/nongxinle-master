package com.nongxinle.dto.route;

import com.nongxinle.entity.NxDisShipmentTaskEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DisRouteDispatchIntegrityResult {
    private List<NxDisShipmentTaskEntity> displayTasks = new ArrayList<NxDisShipmentTaskEntity>();
    private List<InvalidDispatchStopDto> invalidStops = new ArrayList<InvalidDispatchStopDto>();
    private Set<Integer> excludedTaskIds = new HashSet<Integer>();
    private Set<Integer> excludedStopIds = new HashSet<Integer>();
    private int invalidStopCount;
}
