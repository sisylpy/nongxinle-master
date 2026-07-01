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
    /** 分派中页合并视图：已确认站 + 本轮模拟站，按司机分组 */
    private List<NxCommunityDispatchDriverRouteEntity> suggestedRoutes = new ArrayList<>();
    /** 真正无法模拟分派的客户（无司机 / 无建议司机） */
    private Map<Integer, List<NxCommunityOrdersEntity>> unassignedStopGroups = new LinkedHashMap<>();

    /** 分派中页展示用路线；装车/配送页仍读 confirmedRoutes。 */
    public List<NxCommunityDispatchDriverRouteEntity> resolveSandboxRoutes() {
        return suggestedRoutes != null
                ? suggestedRoutes : new ArrayList<NxCommunityDispatchDriverRouteEntity>();
    }
}
