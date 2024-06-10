package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodSalesEntity;

import java.util.List;
import java.util.Map;


public interface GbDepFoodSalesDao extends BaseDao<GbDepFoodSalesEntity> {

    List<GbDepFoodSalesEntity> queryDepFoodSalesByParams(Map<String, Object> mapFoodSales);

}
