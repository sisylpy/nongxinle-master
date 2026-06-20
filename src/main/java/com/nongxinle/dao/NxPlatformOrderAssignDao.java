package com.nongxinle.dao;

import com.nongxinle.dto.platform.PlatformOrderDetailRow;
import com.nongxinle.dto.platform.PlatformPendingGroupRow;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;

import java.util.List;
import java.util.Map;

public interface NxPlatformOrderAssignDao {

    NxPlatformOrderAssignEntity queryObject(Integer id);

    NxPlatformOrderAssignEntity queryByOrderId(Integer orderId);

    NxPlatformOrderAssignEntity queryByGbDepartmentOrderId(Integer gbDepartmentOrderId);

    List<PlatformPendingGroupRow> queryPendingGroupedByDepartment(Map<String, Object> params);

    List<PlatformOrderDetailRow> queryPlatformOrderDetailLines(Map<String, Object> params);

    int countPendingByMarket(Map<String, Object> params);

    void save(NxPlatformOrderAssignEntity entity);

    void update(NxPlatformOrderAssignEntity entity);
}
