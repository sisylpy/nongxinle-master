package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-27 21:55
 */

import com.nongxinle.entity.GbDepFoodGoodsSalesEntity;
import com.nongxinle.entity.GbDepFoodSalesEntity;

import java.util.List;
import java.util.Map;


public interface GbDepFoodGoodsSalesDao extends BaseDao<GbDepFoodGoodsSalesEntity> {

    List<GbDepFoodGoodsSalesEntity> queryDepFoodGoodsSalesByParams(Map<String, Object> mapFoodSales);

    List<GbDepFoodSalesEntity> queryDepFoodsWithGoods(Map<String, Object> map);
}
