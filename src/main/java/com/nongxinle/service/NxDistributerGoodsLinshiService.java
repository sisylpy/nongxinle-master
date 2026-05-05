package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 08-14 22:10
 */

import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerGoodsLinshiService {
	
	NxDistributerGoodsLinshiEntity queryObject(Integer nxDistributerGoodsLsId);
	
	List<NxDistributerGoodsLinshiEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi);
	
	void update(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi);
	
	void delete(Integer nxDistributerGoodsLsId);
	
	void deleteBatch(Integer[] nxDistributerGoodsLsIds);

	List<NxDistributerGoodsLinshiEntity> disGetLinshiGoodsList(Map<String, Object> map);

	int disGetLinshiGoodsTotal(Map<String, Object> map);

	NxDistributerGoodsLinshiEntity queryLinshiByFromGoodsId(Integer fromGoodsId);

	NxDistributerGoodsLinshiEntity queryLinshiByToGoodsId(Integer toGoodsId);

	int updateRevertToLinshi(Integer linshiId);

	List<NxDistributerGoodsLinshiEntity> queryLinshiListByStatus(Map<String, Object> map);

	List<Integer> queryFromGoodsIdsByDisId(Integer disId);

	/**
	 * 根据搜索词和配送商ID查询临时商品（支持分页）
	 * @param map disId, searchGoodsName, searchPinyin(可选), status(可选), offset, limit
	 */
	List<NxDistributerGoodsLinshiEntity> searchLinshiGoodsList(Map<String, Object> map);

	int searchLinshiGoodsTotal(Map<String, Object> map);
}
