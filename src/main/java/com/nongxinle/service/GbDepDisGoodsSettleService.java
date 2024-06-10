package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 04-01 10:51
 */

import com.nongxinle.entity.GbDepDisGoodsSettleEntity;

import java.util.List;
import java.util.Map;

public interface GbDepDisGoodsSettleService {
	
	GbDepDisGoodsSettleEntity queryObject(Integer gbDepDisGoodsSettleId);
	
	List<GbDepDisGoodsSettleEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepDisGoodsSettleEntity gbDepDisGoodsSettle);
	
	void update(GbDepDisGoodsSettleEntity gbDepDisGoodsSettle);
	
	void delete(Integer gbDepDisGoodsSettleId);
	
	void deleteBatch(Integer[] gbDepDisGoodsSettleIds);

    GbDepDisGoodsSettleEntity queryDisGoodsSettleByParams(Map<String, Object> mapGoodsSettle);

	List<GbDepDisGoodsSettleEntity> queryDepDisGoodsSettleByParams(Map<String, Object> map);
}
