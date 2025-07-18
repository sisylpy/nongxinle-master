package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxGoodsEntity;

import java.util.List;
import java.util.Map;

public interface NxGoodsService {

	List<NxGoodsEntity> queryNxGoodsByParams(Map<String, Object> map);

	List<NxGoodsEntity> queryQuickSearchNxGoods(Map<String, Object> map);
	List<NxGoodsEntity> queryQuickSearchNxCategoryGoods(Map<String, Object> map);
	List<NxGoodsEntity> queryQuickSearchNxCategoryGoodsWithNxDis(Map<String, Object> map);



//	///////




	NxGoodsEntity queryObject(Integer nxGoodsId);
	

	int queryTotal(Map<String, Object> map);
	
	void save(NxGoodsEntity nxGoods);
	
	void update(NxGoodsEntity nxGoods);
	
	void delete(Integer nxGoodsId);

	List<NxGoodsEntity> getiBookCoverData();

	List<NxGoodsEntity> getAllFatherGoods(Integer fatherId);

	List<NxGoodsEntity> queryListWithFatherId(Map<String, Object> map);


	int queryTotalByFatherId(Integer fatherId);

	List<NxGoodsEntity> querySubNameByFatherId(Integer nxGoodsId);

    List<NxGoodsEntity> queryGoodsCataByType(Integer type);

    List<NxGoodsEntity> queryGoodsTree();


	List<NxGoodsEntity> queryIfHasSameGoods(Map<String, Object> map);

	List<NxGoodsEntity> queryNxGoodsOrderByGoodsId(Map<String, Object> map);

    List<NxGoodsEntity> queryNxFatherGoods();

	List<NxGoodsEntity> queryQuickSearchAllGoodsWithNxDis(Map<String, Object> map);

    List<NxGoodsEntity> queryQuickSearchFatherGoods(Map<String, Object> map);

	int querySecondLevelMaxId();

	int queryMaxSortByFatherId(Integer nxDfgNxGoodsId);

    int queryMaxIdByGrandId(Integer nxDfgNxGoodsId);

	List<NxGoodsEntity> queryQuickSearchNxGoodsWithNxDis(Map<String, Object> map);

    List<NxGoodsEntity> queryShelfQuickSearchNxGoodsWithNxDis(Map<String, Object> map);

    List<NxGoodsEntity> queryGbDepNxGrandGoodsByGreatId(Map<String, Object> map);

	List<NxGoodsEntity> queryGbDepNxGrandGoodsByGreatIdAll(Map<String, Object> map);

	List<NxGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map);

    List<NxGoodsEntity> queryDisGoodsEqualSearchStrWithDepOrders(Map<String, Object> map);

    List<NxGoodsEntity> queryDisGoodsQuickSearchPyWithDepOrders(Map<String, Object> map);

    List<NxGoodsEntity> queryListWithFatherIdDeep(Map<String, Object> map);

    List<NxGoodsEntity> queryNumberGoods();

    int queryTotalByParams(Map<String, Object> mapCount);

    List<Integer> queryOnlyGoodsIds(Map<String, Object> map);


//    List<NxGoodsEntity> queryCataNxDistribterWithPeisong(Map<String, Object> map);
}
