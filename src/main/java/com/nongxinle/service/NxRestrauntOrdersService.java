package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 12-01 07:19
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface NxRestrauntOrdersService {
	
	NxRestrauntOrdersEntity queryObject(Integer nxRestrauntOrdersId);
	
	List<NxRestrauntOrdersEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxRestrauntOrdersEntity nxRestrauntOrders);
	
	void update(NxRestrauntOrdersEntity nxRestrauntOrders);
	
	void delete(Integer nxRestrauntOrdersId);
	
	void deleteBatch(Integer[] nxRestrauntOrdersIds);

	TreeSet<NxRestrauntEntity> queryCommunityTodayRestruants(Map<String, Object> map1);

	List<NxRestrauntOrdersEntity> queryResOrdersByParams(Map<String, Object> map3);


    List<NxRestrauntOrdersEntity> queryResOrdersForComGoods(Map<String, Object> map1);

    List<NxCommunityFatherGoodsEntity> queryResOrdersByComStockGoodsType(Map<String, Object> map2);

    List<NxDistributerEntity> queryResOrdersByComDistributerGoodsType(Map<String, Object> map2);

	List<NxCommunityFatherGoodsEntity> queryTodayComGoodsType(Map<String, Object> map1);

	List<NxCommunityGoodsEntity> queryOrderGoodsByFatherId(Map<String, Object> map2);

    List<NxRestrauntOrdersEntity> queryComDeliveryResOrdersByParams(Map<String, Object> map);

	TreeSet<NxRestrauntEntity> queryDeliveryResByParams(Map<String, Object> map1);

    int queryOrderComGoodsTodayTotal(Map<String, Object> map);

	TreeSet<NxRestrauntOrdersEntity> queryUnCostDate(Map<String, Object> map);

//	TreeSet<NxCommunityPurchaseBatchEntity> queryBuyingBatchGoods(Map<String, Object> map);

	TreeSet<NxRestrauntEntity> queryTodayComRestrauant(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDistributerGoodsWithResOrdersByParams(Map<String, Object> map3);
	List<NxCommunityEntity> queryDistributerTodayCommunity(Map<String, Object> map3);

	List<NxRestrauntOrdersEntity> queryResChainOrdersByParams(Map<String, Object> map);

	List<NxCommunityFatherGoodsEntity> queryResWithSubDepsOrder(Map<String, Object> map);

    List<NxRestrauntOrdersEntity> queryPurchaseResOrders(Map<String, Object> map);

    List<NxRestrauntOrdersEntity> queryPurchaseGoodsOrders(Integer nxCommunityPurchaseGoodsId);

	TreeSet<NxRestrauntEntity> queryDateOrderResId(Map<String, Object> map1);

    List<Integer> queryResOrdersIdByBillId(Integer nxRestrauntBillId);

	TreeSet<NxRestrauntComGoodsEntity> queryTry(Map<String, Object> userId);

    NxRestrauntOrdersEntity queryNxRestrauntOrderByNxOrderId(Integer nxDepartmentOrdersId);


//    List<NxDepartmentOrdersEntity> queryDepChainOrdersByParams(Map<String, Object> map);
}
