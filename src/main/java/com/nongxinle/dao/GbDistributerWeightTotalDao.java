package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-29 07:10
 */

import com.nongxinle.entity.GbDistributerWeightTotalEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerWeightTotalDao extends BaseDao<GbDistributerWeightTotalEntity> {

    List<GbDistributerWeightTotalEntity> queryDepWeightListByParams(Map<String, Object> map);

    int queryDepWeightCountByParams(Map<String, Object> map3);
}
