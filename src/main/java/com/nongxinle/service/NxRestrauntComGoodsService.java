package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 12-01 08:51
 */

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxRestrauntComGoodsEntity;
import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;

public interface NxRestrauntComGoodsService {
	
	NxRestrauntComGoodsEntity queryObject(Integer nxRestrauntComGoodsId);
	
	List<NxRestrauntComGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRestrauntComGoodsEntity nxRestaruantComGoods);
	
	void update(NxRestrauntComGoodsEntity nxRestaruantComGoods);
	
	void delete(Integer nxRestrauntComGoodsId);
	
	void deleteBatch(Integer[] nxRestrauntComGoodsIds);

    List<NxRestrauntComGoodsEntity> queryResComGoodsByParams(Map<String, Object> map);

    List<NxCommunityFatherGoodsEntity> resGetResComGoodsCata(Map<String, Object> map);

	List<NxRestrauntComGoodsEntity> queryResGoodsByFatherId(Map<String, Object> map);

    int queryComGoodsTotal(Map<String, Object> map3);

    List<NxCommunityFatherGoodsEntity> queryHistoryGoods(Map<String, Object> map);

    List<NxRestrauntComGoodsEntity> queryHistoryGoodsQuickSearchStr(Map<String, Object> map1);

    List<NxCommunityFatherGoodsEntity> queryOrderUserGoods(Map<String, Object> map);

	List<NxCommunityFatherGoodsEntity> queryOrderResGoods(Map<String, Object> map);

    List<NxRestrauntComGoodsEntity> orderUserQueryResComGoodsQuickSearchStr(Map<String, Object> map);

	List<NxRestrauntComGoodsEntity> ordreUserQueryHistoryGoodsQuickSearchStr(Map<String, Object> map1);

    void deleteResComGoods(Map<String, Object> map1);

	List<NxRestrauntEntity> queryRestrantByResComGoodId(Map<String, Object> map1);

    List<NxCommunityFatherGoodsEntity> queryResManagedGoods(Map<String, Object> map);

    List<NxRestrauntComGoodsEntity> queryResComGoodsWithComGoods(Integer resId);

    List<NxRestrauntEntity> queryChainResGoodsByParams(Map<String, Object> map);

    List<NxCommunityFatherGoodsEntity> queryCgManagedGoods(Map<String, Object> map);

    List<NxRestrauntComGoodsEntity> cgQueryCgMangementGoodsQuickSearchStr(Map<String, Object> map);
}
