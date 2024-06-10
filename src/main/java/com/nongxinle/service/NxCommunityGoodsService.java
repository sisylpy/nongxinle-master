package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface NxCommunityGoodsService {
	
	NxCommunityGoodsEntity queryObject(Integer communityGoodsId);
	
	List<NxCommunityGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxCommunityGoodsEntity nxDistributerGoods);
	
	void update(NxCommunityGoodsEntity nxDistributerGoods);
	
	void delete(Integer disGoodsId);
	
	void deleteBatch(Integer[] disGoodsIds);


	List<NxCommunityGoodsEntity> queryCommunityGoods(Map<String, Object> map);

	int queryTotalByFatherId(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryDisDownloadGoods(Map<String, Object> map);


    List<NxCommunityGoodsEntity> queryCommunityDownloadGoods(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryDistributerGoods(Map<String, Object> map);

	List<NxCommunityGoodsEntity>  queryHasNxGoodsFather(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryStockGoods(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryCommunityGoodsWithPinyin(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryComGoodsHasNxGoodsFather(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryComGoodsByParams(Map<String, Object> map7);

    NxCommunityGoodsEntity queryComGoodsDetail(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryAddCommunityNxGoods(Map<String, Object> map);

    List<NxCommunityGoodsEntity> resQueryComGoodsQuickSearchStr(Map<String, Object> map);

	List<NxCommunityGoodsEntity> queryComGoodsQuickSearchStr(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryCgSubNameByFatherId(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryComResGoodsByParams(Map<String, Object> map);

    List<NxCommunityGoodsEntity> resManQueryComResGoodsQuickSearchStr(Map<String, Object> map);

	List<NxCommunityGoodsEntity> comQueryDisComGoodsByParams(Map<String, Object> map);

    List<NxCommunityGoodsEntity> queryComGoodsWithSupplierByParams(Map<String, Object> map);

//	Treeset<NxCommunityGoodsEntity> queryChainComResGoodsByParams(Map<String, Object> map);

	List<NxCommunityGoodsEntity> resQueryComExchangePriceGoodsByDate(Map<String, Object> map7);

	List<NxCommunityGoodsEntity> queryChainComResGoodsByParams(Map<String, Object> map);

    List<NxCommunityGoodsEntity> cgQueryCgMangementGoodsQuickSearchStr(Map<String, Object> map);

    NxCommunityGoodsEntity queryRemarkComGoodsDetail(Map<String, Object> map);

    NxCommunityGoodsEntity queryPropertyComGoodsDetail(Map<String, Object> map);
}
