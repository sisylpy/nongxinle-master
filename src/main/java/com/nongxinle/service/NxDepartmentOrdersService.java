package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 06-21 21:51
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;

public interface NxDepartmentOrdersService {


	List<NxDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map);

	List<NxDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map);

	void save(NxDepartmentOrdersEntity nxDepartmentOrders);

	List<NxDistributerFatherGoodsEntity>  disGetUnPlanPurchaseApplys(Map<String, Object> map);
	List<NxDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1);


//	////////



	NxDepartmentOrdersEntity queryObject(Integer nxDepartmentOrdersId);
	
	List<NxDepartmentOrdersEntity> queryList(Map<String, Object> map);

	void update(NxDepartmentOrdersEntity nxDepartmentOrders);
	
	void delete(Integer nxDepartmentOrdersId);


	int queryTotalByParams(Map<String, Object> deliverymap);

    List<NxDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryReturnOrdersByBillId(Integer billId);

    void saveGbOrders(NxDepartmentOrdersEntity ordersEntity);

	List<NxDepartmentEntity> queryOrderDepartmentList(Map<String, Object> map1);

    List<NxDistributerFatherGoodsEntity> queryDepOrdersOrderFatherGoods(Map<String, Object> map);

    Integer queryDepOrdersAcount(Map<String, Object> map);

	Double queryDepOrdersSubtotal(Map<String, Object> map);


    Double queryDepOrdersProfitSubtotal(Map<String, Object> map2);

	List<NxRestrauntEntity> queryOrderNxRestrauntList(Map<String, Object> map1);

    List<GbDepartmentEntity> queryOrderGbDepartmentList(Map<String, Object> map1);

    List<NxDepartmentOrdersEntity> queryDepWeightOrder(Map<String, Object> map);

	List<NxDepartmentOrdersEntity> queryNotWeightDisOrdersByParams(Map<String, Object> map1);

    List<GbDistributerEntity> queryOrderGbDistributerList(Map<String, Object> map1);


	void saveForGb(NxDepartmentOrdersEntity ordersEntity);

    List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysSearch(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryDepWeightOrderSearch(Map<String, Object> map);

    int disGetPurchaseGoodsApplysCount(Map<String, Object> map1);

    List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysNew(Map<String, Object> map);

//    List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApply(Map<String, Object> map);

    NxDepartmentOrdersEntity queryObjectNew(Integer nxDepartmentOrdersId);

    List<NxDistributerFatherGoodsEntity> queryGreatGrandOrderFatherGoods(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGoodsForTodayOrders(Map<String, Object> map);

    List<NxDepartmentEntity> queryPureOrderNxDepartment(Map<String, Object> map);

    List<GbDepartmentEntity> queryPureOrderGbDepartment(Map<String, Object> map);

    double queryDisGoodsOrderWeightTotal(Map<String, Object> map1);

    List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApplyForStock(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryPrintDepOrder(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGetPrintOrderGreatGrandGoods(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryDepWeightOrderGb(Map<String, Object> map);


    List<NxDistributerFatherGoodsEntity> queryFatherGoodsByParams(Map<String, Object> map1222);

    double queryDepOrdersProfitScale(Map<String, Object> map1222);


    double queryCostSubtotal(Map<String, Object> map1222);

    List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map2);
}
