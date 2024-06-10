package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-22 16:10
 */

import com.nongxinle.entity.GbDepartmentSettleEntity;

import java.util.List;
import java.util.Map;


public interface GbDepartmentSettleDao extends BaseDao<GbDepartmentSettleEntity> {

    List<GbDepartmentSettleEntity> queryDepartmentSettlesByParams(Map<String, Object> map);

    GbDepartmentSettleEntity queryTotalBySettleId(String settleId);
}
