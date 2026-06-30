package com.nongxinle.service;

import com.nongxinle.dto.route.DisRouteCustomerDriverConstraintDto;

import java.util.List;
import java.util.Map;

/** 客户-司机约束解析与校验。 */
public interface DisRouteCustomerDriverConstraintService {

    Map<Integer, DisRouteCustomerDriverConstraintDto> resolveConstraints(Integer disId,
                                                                         List<Integer> departmentIds,
                                                                         List<Integer> eligibleDriverUserIds);

    ConstraintCheckResult check(Integer driverUserId, DisRouteCustomerDriverConstraintDto constraint);

    String buildConstraintLabel(Integer driverUserId, DisRouteCustomerDriverConstraintDto constraint);

    final class ConstraintCheckResult {
        private final boolean blocked;
        private final boolean warning;
        private final String message;

        public ConstraintCheckResult(boolean blocked, boolean warning, String message) {
            this.blocked = blocked;
            this.warning = warning;
            this.message = message;
        }

        public static ConstraintCheckResult ok() {
            return new ConstraintCheckResult(false, false, null);
        }

        public static ConstraintCheckResult warning(String message) {
            return new ConstraintCheckResult(false, true, message);
        }

        public static ConstraintCheckResult blocked(String message) {
            return new ConstraintCheckResult(true, false, message);
        }

        public boolean isBlocked() {
            return blocked;
        }

        public boolean isWarning() {
            return warning;
        }

        public String getMessage() {
            return message;
        }
    }
}
