package com.nongxinle.dao;

import com.nongxinle.dto.platform.PlatformOrderDetailRow;
import com.nongxinle.dto.platform.PlatformPendingGroupRow;
import com.nongxinle.dto.platform.distributer.PlatformDistributerCustomerRow;
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

    int resetAssignToPending(Integer platformAssignId);

    int deleteByOrderId(Integer orderId);

    /** 配送商今日平台客户展示信息（gb 饭店名、市场） */
    List<Map<String, Object>> queryPlatformCustomerDisplayByDistributer(Map<String, Object> params);

    /** 配送商今日平台客户列表（含 GB 京采饭店 + NX 两类） */
    List<PlatformDistributerCustomerRow> queryPlatformDistributerCustomers(Map<String, Object> params);

    /** 列表为空时诊断：各阶段 ASSIGNED 平台单行数 */
    Map<String, Object> queryPlatformDistributerCustomerDiagnostics(Map<String, Object> params);
}
