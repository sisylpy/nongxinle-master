package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-20 21:43
 */

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbRouteDepEntity;

import java.util.List;


public interface GbRouteDepDao extends BaseDao<GbRouteDepEntity> {

    List<GbDepartmentEntity> queryHaveLineDepsByDisId(Integer disId);
}
