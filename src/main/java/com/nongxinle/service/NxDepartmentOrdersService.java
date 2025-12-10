package com.nongxinle.service;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import com.nongxinle.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NxDepartmentOrdersService {


    List<NxDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map);

    List<NxDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map);

    void save(NxDepartmentOrdersEntity nxDepartmentOrders);

    List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplys(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1);


//	////////


    NxDepartmentOrdersEntity queryObject(Integer nxDepartmentOrdersId);

    List<NxDepartmentOrdersEntity> queryList(Map<String, Object> map);

    void update(NxDepartmentOrdersEntity nxDepartmentOrders);

    void delete(Integer nxDepartmentOrdersId);


    int queryTotalByParams(Map<String, Object> deliverymap);

    List<NxDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryReturnOrdersByBillId(Integer billId);

//    void saveGbOrders(NxDepartmentOrdersEntity ordersEntity);

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

    /**
     * 查询曾祖父商品列表（超简化版，只返回基本信息，不包含嵌套对象）
     */
    List<GreatGrandFatherGoodsSimpleDTO> queryGreatGrandOrderFatherGoodsUltraSimple(Map<String, Object> map);

    List<OutGoodsSimpleDTO> queryOutGoodsWithOrdersUltraSimple(Map<String, Object> map);

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

    void deleteBatch(Integer[] nxOrdersSubIds);

    Integer queryDepOrdersAcountByDepGoods(Map<String, Object> mapDep);

    List<NxDistributerGoodsShelfEntity> queryShelfGoodsOrder(Map<String, Object> map);

    Integer queryReturnOrderCount(Map<String, Object> map);

    double queryReturnSubtotal(Map<String, Object> mapR);

//    List<GbDepartmentEntity> queryqueryOrderGbDepartmentList(Map<String, Object> map1);

    void moveOrderToHistory(NxDepartmentOrdersEntity orders);

    Map<String, Object> queryStockGoodsData(Map<String, Object> params);

    List<NxDepartmentEntity> queryPureOrderNxDepartmentSimple(Map<String, Object> map);

    Map<Integer, Map<String, Integer>> batchQueryDepStats(@Param("list") List<Integer> depIds);


    Map<Integer, Map<String, Integer>> batchQueryGbDepStats(@Param("list") List<Integer>  gbDepIds);

    Map<Integer, Integer> batchQueryFatherGoodsOrderCount(List<Integer> grandIds, Map<String, Object> params);
    
    Map<Integer, Map<String, Object>> batchQueryDepartmentOrderStats(List<Integer> depFatherIds);
    
    Map<String, Map<String, Object>> batchQueryGbDistributerDepartmentStats(List<Integer> gbDisIds, List<Integer> gbDepIds);

    Map<String, Object> getOrderStats(Integer disId);


    List<Integer> queryOnlyNxGoodsIds(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> disGetOutGoodsGrandCata(Map<String, Object> map);

    List<NxDistributerGoodsEntity> disGetNxGoodsApply(Map<String, Object> map);

    List<OutGoodsSimpleDTO> disGetNxGoodsApplyUltraSimple(Map<String, Object> map);

    Integer queryOrderGoodsCount(Map<String, Object> mapCount);

    /**
     * 获取货架列表（仅基本信息，不包含商品详情）
     * @param params 查询参数
     * @return 包含货架列表、部门列表、统计数据的Map
     */
    Map<String, Object> queryShelfListWithDepIds(Map<String, Object> params);

    /**
     * 获取指定货架的商品详情（包含订单信息）
     * @param params 查询参数，必须包含shelfId
     * @return 完整的货架对象（包含商品列表和订单）
     */
    NxDistributerGoodsShelfEntity queryShelfGoodsDetail(Map<String, Object> params);

    /**
     * 获取指定货架的商品详情（超简化版，使用DTO对象，字段扁平化）
     * @param params 查询参数，必须包含shelfId
     * @return 货架详情DTO（字段已最大程度简化）
     */
    ShelfDetailSimpleDTO queryShelfGoodsDetailUltraSimple(Map<String, Object> params);

    /**
     * 获取统计数据（不包含货架和商品数据）
     * @param params 查询参数
     * @return 包含部门列表和统计数据的Map
     */
    Map<String, Object> queryShelfStatistics(Map<String, Object> params);

    /**
     * 查询类别列表（曾祖父级别，仅基本信息，不包含商品详情）
     * @param params 查询参数
     * @return 包含类别列表、部门列表和统计数据的Map
     */
    Map<String, Object> queryCategoryListWithDepIds(Map<String, Object> params);

    /**
     * 查询指定类别的商品详情（超简化版，使用DTO对象，字段扁平化）
     * @param params 查询参数，必须包含categoryId
     * @return 类别详情DTO（字段已最大程度简化）
     */
    CategoryDetailSimpleDTO queryCategoryGoodsDetailUltraSimple(Map<String, Object> params);

    /**
     * 查询统计数据（不包含类别和商品数据）
     * @param params 查询参数
     * @return 包含部门列表和统计数据的Map
     */
    Map<String, Object> queryCategoryStatistics(Map<String, Object> params);

    Map<Integer, Map<String, Object>> batchQueryDepartmentOrderStatsSunla(List<Integer> depFatherIds);
}
