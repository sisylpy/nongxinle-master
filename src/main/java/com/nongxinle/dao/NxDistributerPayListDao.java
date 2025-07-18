package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-20 11:05
 */

import com.nongxinle.entity.NxDistributerPayListEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerPayListDao extends BaseDao<NxDistributerPayListEntity> {

    double queryDisPayListSubtotal(Map<String, Object> map);

    int queryDisPayListCount(Map<String, Object> map);

    List<NxDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map);

    int queryDepRecordSecondsTotal(Map<String, Object> map);
}
