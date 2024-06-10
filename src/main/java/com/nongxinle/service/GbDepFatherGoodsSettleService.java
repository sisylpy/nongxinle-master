package com.nongxinle.service;

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

public interface GbDepFatherGoodsSettleService {
	
	GbDepFatherGoodsSettleEntity queryObject(Integer gbDepFatherGoodsSettleStaticsId);
	
	List<GbDepFatherGoodsSettleEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepFatherGoodsSettleEntity gbDistributerFatherGoodsSettle);
	
	void update(GbDepFatherGoodsSettleEntity gbDistributerFatherGoodsSettle);
	
	void delete(Integer gbDepFatherGoodsSettleStaticsId);
	
	void deleteBatch(Integer[] gbDepFatherGoodsSettleStaticsIds);

    TreeSet<GbDistributerFatherGoodsEntity> queryPankuFatherGoods(Map<String, Object> map);

    List<GbDepFatherGoodsSettleEntity> queryDisFatherGoodsSettleTotalByParams(Map<String, Object> mapSettleCost);

	Double queryPankuFatherGoodsTypeTotal(Map<String, Object> mapSettleCost);
}
