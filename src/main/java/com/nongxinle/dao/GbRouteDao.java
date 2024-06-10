package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbRouteEntity;

import java.util.List;
import java.util.Map;


public interface GbRouteDao extends BaseDao<GbRouteEntity> {

    List<GbRouteEntity> getDisRoutesByDisId(Map<String, Object> map);
}
