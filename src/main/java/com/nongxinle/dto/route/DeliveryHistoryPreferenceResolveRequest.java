package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@ToString
public class DeliveryHistoryPreferenceResolveRequest {
    private Integer disId;
    private List<Integer> depFatherIds = new ArrayList<Integer>();
    private List<Integer> eligibleDriverUserIds = new ArrayList<Integer>();
    private Integer lookbackDays;
    private Integer minDeliveredTimes;
    /** 仅日志/审计；不参与 SQL 过滤。 */
    private String routeDate;

    public static DeliveryHistoryPreferenceResolveRequest of(Integer disId,
                                                             List<Integer> depFatherIds,
                                                             List<Integer> eligibleDriverUserIds,
                                                             String routeDate) {
        DeliveryHistoryPreferenceResolveRequest request = new DeliveryHistoryPreferenceResolveRequest();
        request.setDisId(disId);
        request.setDepFatherIds(dedupe(depFatherIds));
        request.setEligibleDriverUserIds(dedupe(eligibleDriverUserIds));
        request.setRouteDate(routeDate);
        return request;
    }

    private static List<Integer> dedupe(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<Integer>();
        }
        Set<Integer> seen = new LinkedHashSet<Integer>();
        for (Integer id : ids) {
            if (id != null) {
                seen.add(id);
            }
        }
        return new ArrayList<Integer>(seen);
    }
}
