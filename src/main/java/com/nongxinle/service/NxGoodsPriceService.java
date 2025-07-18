package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 03-09 08:28
 */

import com.nongxinle.entity.NxGoodsPriceEntity;

import java.util.List;
import java.util.Map;

public interface NxGoodsPriceService {
	
	NxGoodsPriceEntity queryObject(Integer nxGoodsPriceId);
	
	List<NxGoodsPriceEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxGoodsPriceEntity nxGoodsPrice);
	
	void update(NxGoodsPriceEntity nxGoodsPrice);
	
	void delete(Integer nxGoodsPriceId);
	
	void deleteBatch(Integer[] nxGoodsPriceIds);

    NxGoodsPriceEntity queryPriceGoodsByParams(Map<String, Object> map);

    double queryLowestPriceByParams(Map<String, Object> mapL);

    double queryHighestPriceByParams(Map<String, Object> mapL);

    int queryPriceGoodsCount(Map<String, Object> mapL);
}
