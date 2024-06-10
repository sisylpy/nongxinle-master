package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 03-31 10:13
 */

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDepFatherGoodsSettleEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public interface GbDepFatherGoodsSettleDao extends BaseDao<GbDepFatherGoodsSettleEntity> {

    TreeSet<GbDistributerFatherGoodsEntity> queryPankuFatherGoods(Map<String, Object> map);

    List<GbDepFatherGoodsSettleEntity> queryDisFatherGoodsSettleTotalByParams(Map<String, Object> mapSettleCost);

    Double queryPankuFatherGoodsTypeTotal(Map<String, Object> mapSettleCost);
}
