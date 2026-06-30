package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
public class DeliveryHistoryPreferenceBatchResult {
    private Integer disId;
    private Integer lookbackDays;
    private String resolvedAt;
    private Map<Integer, DeliveryHistoryPreferenceDto> preferencesByDepFatherId =
            new LinkedHashMap<Integer, DeliveryHistoryPreferenceDto>();
    private List<RouteDispatchWarning> warnings = new ArrayList<RouteDispatchWarning>();
}
