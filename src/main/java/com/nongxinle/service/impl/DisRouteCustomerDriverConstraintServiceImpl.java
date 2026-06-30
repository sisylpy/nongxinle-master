package com.nongxinle.service.impl;

import com.nongxinle.dto.route.DeliveryHistoryPreferenceBatchResult;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceDto;
import com.nongxinle.dto.route.DeliveryHistoryPreferenceResolveRequest;
import com.nongxinle.dto.route.DisRouteCustomerDriverConstraintDto;
import com.nongxinle.route.DisRouteCustomerDriverConstraintTypes;
import com.nongxinle.service.DisRouteCustomerDriverConstraintService;
import com.nongxinle.service.DisRouteDeliveryHistoryPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DisRouteCustomerDriverConstraintServiceImpl implements DisRouteCustomerDriverConstraintService {

    /** 预留：后续可接 DB / 管理后台配置。 */
    private final List<DisRouteCustomerDriverConstraintDto> reservedConstraints =
            Collections.synchronizedList(new ArrayList<DisRouteCustomerDriverConstraintDto>());

    @Autowired
    private DisRouteDeliveryHistoryPreferenceService disRouteDeliveryHistoryPreferenceService;

    @Override
    public Map<Integer, DisRouteCustomerDriverConstraintDto> resolveConstraints(Integer disId,
                                                                              List<Integer> departmentIds,
                                                                              List<Integer> eligibleDriverUserIds) {
        Map<Integer, DisRouteCustomerDriverConstraintDto> result =
                new LinkedHashMap<Integer, DisRouteCustomerDriverConstraintDto>();
        if (departmentIds == null || departmentIds.isEmpty()) {
            return result;
        }
        for (Integer depId : departmentIds) {
            if (depId != null) {
                result.put(depId, buildDefaultConstraint(depId));
            }
        }
        applyReservedConstraints(result);
        applyHistoryPreferences(disId, departmentIds, eligibleDriverUserIds, result);
        return result;
    }

    @Override
    public ConstraintCheckResult check(Integer driverUserId, DisRouteCustomerDriverConstraintDto constraint) {
        if (driverUserId == null || constraint == null) {
            return ConstraintCheckResult.ok();
        }
        if (constraint.getForbiddenDriverUserIds() != null
                && constraint.getForbiddenDriverUserIds().contains(driverUserId)) {
            return ConstraintCheckResult.blocked("客户禁止该司机配送");
        }
        if (DisRouteCustomerDriverConstraintTypes.MUST.equals(constraint.getConstraintType())) {
            if (constraint.getAllowedDriverUserIds() != null && !constraint.getAllowedDriverUserIds().isEmpty()
                    && !constraint.getAllowedDriverUserIds().contains(driverUserId)) {
                return ConstraintCheckResult.blocked("客户仅允许指定司机配送");
            }
        }
        if (constraint.getPreferredDriverUserId() != null
                && !driverUserId.equals(constraint.getPreferredDriverUserId())) {
            if (DisRouteCustomerDriverConstraintTypes.PREFER.equals(constraint.getConstraintType())) {
                return ConstraintCheckResult.warning("建议由偏好司机配送");
            }
            return ConstraintCheckResult.warning("非历史偏好司机");
        }
        return ConstraintCheckResult.ok();
    }

    @Override
    public String buildConstraintLabel(Integer driverUserId, DisRouteCustomerDriverConstraintDto constraint) {
        if (constraint == null) {
            return null;
        }
        if (constraint.getPreferredDriverUserId() != null
                && driverUserId != null
                && constraint.getPreferredDriverUserId().equals(driverUserId)) {
            return "历史偏好司机";
        }
        if (constraint.getPreferredDriverUserId() != null
                && (driverUserId == null || !constraint.getPreferredDriverUserId().equals(driverUserId))) {
            return "建议其他司机配送";
        }
        if (constraint.getRemark() != null && !constraint.getRemark().trim().isEmpty()) {
            return constraint.getRemark().trim();
        }
        return null;
    }

    private static DisRouteCustomerDriverConstraintDto buildDefaultConstraint(Integer departmentId) {
        DisRouteCustomerDriverConstraintDto dto = new DisRouteCustomerDriverConstraintDto();
        dto.setDepartmentId(departmentId);
        dto.setConstraintType(DisRouteCustomerDriverConstraintTypes.PREFER);
        return dto;
    }

    private void applyReservedConstraints(Map<Integer, DisRouteCustomerDriverConstraintDto> result) {
        for (DisRouteCustomerDriverConstraintDto reserved : reservedConstraints) {
            if (reserved == null || reserved.getDepartmentId() == null) {
                continue;
            }
            result.put(reserved.getDepartmentId(), reserved);
        }
    }

    private void applyHistoryPreferences(Integer disId,
                                         List<Integer> departmentIds,
                                         List<Integer> eligibleDriverUserIds,
                                         Map<Integer, DisRouteCustomerDriverConstraintDto> result) {
        DeliveryHistoryPreferenceResolveRequest request = DeliveryHistoryPreferenceResolveRequest.of(
                disId, departmentIds, eligibleDriverUserIds, null);
        DeliveryHistoryPreferenceBatchResult batch = disRouteDeliveryHistoryPreferenceService.resolve(request);
        if (batch == null || batch.getPreferencesByDepFatherId() == null) {
            return;
        }
        for (Map.Entry<Integer, DeliveryHistoryPreferenceDto> entry : batch.getPreferencesByDepFatherId().entrySet()) {
            DeliveryHistoryPreferenceDto pref = entry.getValue();
            if (pref == null || entry.getKey() == null) {
                continue;
            }
            DisRouteCustomerDriverConstraintDto constraint = result.get(entry.getKey());
            if (constraint == null) {
                constraint = buildDefaultConstraint(entry.getKey());
                result.put(entry.getKey(), constraint);
            }
            if (pref.getPreferredDriverUserId() != null) {
                constraint.setPreferredDriverUserId(pref.getPreferredDriverUserId());
                constraint.setConstraintType(DisRouteCustomerDriverConstraintTypes.PREFER);
            }
            if (pref.getPreferredDriverName() != null && !pref.getPreferredDriverName().trim().isEmpty()) {
                constraint.setRemark("历史偏好：" + pref.getPreferredDriverName().trim());
            }
        }
    }
}
