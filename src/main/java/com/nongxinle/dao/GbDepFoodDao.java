package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodEntity;

import java.util.List;
import java.util.Map;


public interface GbDepFoodDao extends BaseDao<GbDepFoodEntity> {

    List<GbDepFoodEntity> queryDepFoodByParams(Map<String, Object> map);

    List<GbDepFoodEntity> queryDepFoodByParamsWithoutFather(Map<String, Object> map);
}
