package com.nongxinle.route.dispatch.strategy;

import com.nongxinle.dto.route.DeliveryHistoryPreferenceBatchResult;
import com.nongxinle.entity.NxDisRouteStopEntity;
import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.route.model.GeoPoint;
import lombok.Getter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 策略层输入快照。由 {@code DisRouteSandboxComputeServiceImpl} 在 compute 中组装。
 */
@Getter
public class DispatchStrategyContext {

    private final Integer disId;
    private final String routeDate;
    private final String batchCode;
    private final GeoPoint depot;
    private final Date serverNow;
    private final List<NxDisShipmentTaskEntity> virtualTasks;
    private final List<NxDisShipmentTaskEntity> optimizableTasks;
    private final List<NxDistributerUserEntity> dispatchEligibleDrivers;
    private final List<NxDisRouteStopEntity> confirmedStops;
    private final Set<Integer> confirmedDepIds;
    private final DeliveryHistoryPreferenceBatchResult deliveryHistoryPreferences;
    private final Map<Integer, NxDepartmentEntity> departmentByDepId;
    private final Map<Integer, NxDisSandboxDayTimeWindowEntity> sandboxDayOverrideByDepId;

    private DispatchStrategyContext(Builder builder) {
        this.disId = builder.disId;
        this.routeDate = builder.routeDate;
        this.batchCode = builder.batchCode;
        this.depot = builder.depot;
        this.serverNow = builder.serverNow != null ? builder.serverNow : new Date();
        this.virtualTasks = builder.virtualTasks != null
                ? builder.virtualTasks : Collections.<NxDisShipmentTaskEntity>emptyList();
        this.optimizableTasks = builder.optimizableTasks != null
                ? builder.optimizableTasks : Collections.<NxDisShipmentTaskEntity>emptyList();
        this.dispatchEligibleDrivers = builder.dispatchEligibleDrivers != null
                ? builder.dispatchEligibleDrivers : Collections.<NxDistributerUserEntity>emptyList();
        this.confirmedStops = builder.confirmedStops != null
                ? builder.confirmedStops : Collections.<NxDisRouteStopEntity>emptyList();
        this.confirmedDepIds = builder.confirmedDepIds != null
                ? builder.confirmedDepIds : Collections.<Integer>emptySet();
        this.deliveryHistoryPreferences = builder.deliveryHistoryPreferences;
        this.departmentByDepId = builder.departmentByDepId != null
                ? builder.departmentByDepId : Collections.<Integer, NxDepartmentEntity>emptyMap();
        this.sandboxDayOverrideByDepId = builder.sandboxDayOverrideByDepId != null
                ? builder.sandboxDayOverrideByDepId
                : Collections.<Integer, NxDisSandboxDayTimeWindowEntity>emptyMap();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer disId;
        private String routeDate;
        private String batchCode;
        private GeoPoint depot;
        private Date serverNow;
        private List<NxDisShipmentTaskEntity> virtualTasks;
        private List<NxDisShipmentTaskEntity> optimizableTasks;
        private List<NxDistributerUserEntity> dispatchEligibleDrivers;
        private List<NxDisRouteStopEntity> confirmedStops;
        private Set<Integer> confirmedDepIds;
        private DeliveryHistoryPreferenceBatchResult deliveryHistoryPreferences;
        private Map<Integer, NxDepartmentEntity> departmentByDepId;
        private Map<Integer, NxDisSandboxDayTimeWindowEntity> sandboxDayOverrideByDepId;

        public Builder disId(Integer disId) {
            this.disId = disId;
            return this;
        }

        public Builder routeDate(String routeDate) {
            this.routeDate = routeDate;
            return this;
        }

        public Builder batchCode(String batchCode) {
            this.batchCode = batchCode;
            return this;
        }

        public Builder depot(GeoPoint depot) {
            this.depot = depot;
            return this;
        }

        public Builder serverNow(Date serverNow) {
            this.serverNow = serverNow;
            return this;
        }

        public Builder virtualTasks(List<NxDisShipmentTaskEntity> virtualTasks) {
            this.virtualTasks = virtualTasks;
            return this;
        }

        public Builder optimizableTasks(List<NxDisShipmentTaskEntity> optimizableTasks) {
            this.optimizableTasks = optimizableTasks;
            return this;
        }

        public Builder dispatchEligibleDrivers(List<NxDistributerUserEntity> dispatchEligibleDrivers) {
            this.dispatchEligibleDrivers = dispatchEligibleDrivers;
            return this;
        }

        public Builder confirmedStops(List<NxDisRouteStopEntity> confirmedStops) {
            this.confirmedStops = confirmedStops;
            return this;
        }

        public Builder confirmedDepIds(Set<Integer> confirmedDepIds) {
            this.confirmedDepIds = confirmedDepIds;
            return this;
        }

        public Builder deliveryHistoryPreferences(DeliveryHistoryPreferenceBatchResult deliveryHistoryPreferences) {
            this.deliveryHistoryPreferences = deliveryHistoryPreferences;
            return this;
        }

        public Builder departmentByDepId(Map<Integer, NxDepartmentEntity> departmentByDepId) {
            this.departmentByDepId = departmentByDepId;
            return this;
        }

        public Builder sandboxDayOverrideByDepId(
                Map<Integer, NxDisSandboxDayTimeWindowEntity> sandboxDayOverrideByDepId) {
            this.sandboxDayOverrideByDepId = sandboxDayOverrideByDepId;
            return this;
        }

        public DispatchStrategyContext build() {
            return new DispatchStrategyContext(this);
        }
    }
}
