package com.nongxinle.community.dispatch.model;

import com.nongxinle.entity.NxCommunityDispatchDriverRouteEntity;
import com.nongxinle.entity.NxCommunityDispatchStopEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CommunityDispatchSandboxResult {
    private Integer communityId;
    private String routeDate;
    private String pageMode;
    private String depotLat;
    private String depotLng;
    private List<NxCommunityOrdersEntity> eligibleOrders = new ArrayList<>();
    private List<NxCommunityUserEntity> availableDrivers = new ArrayList<>();
    private List<NxCommunityDispatchDriverRouteEntity> confirmedRoutes = new ArrayList<>();
    private List<NxCommunityDispatchStopEntity> confirmedStops = new ArrayList<>();
    /** addressId -> orders，仅沙箱模拟，不落库 */
    private Map<Integer, List<NxCommunityOrdersEntity>> simulatedStopGroups = new LinkedHashMap<>();
    /** addressId -> suggested driverUserId，仅展示 */
    private Map<Integer, Integer> simulatedDriverByAddress = new LinkedHashMap<>();
}
