package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-21 21:51
 */

import com.nongxinle.entity.*;
import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface NxDepartmentOrdersDao extends BaseDao<NxDepartmentOrdersEntity> {

    List<NxDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map);

    List<NxDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity>  disGetUnPlanPurchaseApplys(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1);

    int queryTotalByParams(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map);

//    List<GbDepartmentEntity> queryDistributerTodayGbDepartments(Map<String, Object> map1);

    List<NxDepartmentOrdersEntity> queryDisOrdersGbByParams(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryReturnOrdersByBillId(Integer billId);

    List<NxDepartmentEntity> queryOrderDepartmentList(Map<String, Object> map1);

    List<NxDistributerFatherGoodsEntity> queryDepOrdersOrderFatherGoods(Map<String, Object> map);

    Integer queryDepOrdersAcount(Map<String, Object> map);

    Integer queryMaxTodayOrder(Map<String, Object> map);

    Double queryDepOrdersSubtotal(Map<String, Object> map);

    Double queryDepOrdersCostSubtotal(Map<String, Object> map2);

    Double queryDepOrdersProfitSubtotal(Map<String, Object> map2);

    List<NxRestrauntEntity> queryOrderNxRestrauntList(Map<String, Object> map1);

    List<GbDepartmentEntity> queryOrderGbDepartmentList(Map<String, Object> map1);

    List<NxDepartmentOrdersEntity> queryDepWeightOrder(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryNotWeightDisOrdersByParams(Map<String, Object> map1);

    /**
     * 查询订单列表（简化版，返回DTO，只包含必要字段）
     * 用于phoneGetToFillDepOrders接口优化
     */
    List<NxDepartmentOrdersSimpleDTO> queryNotWeightDisOrdersSimpleByParams(Map<String, Object> map1);
    
    /**
     * 根据OCR任务ID查询订单列表
     * @param ocrTaskId OCR任务ID
     * @return 订单列表
     */
    List<NxDepartmentOrdersEntity> queryListByOcrTaskId(Integer ocrTaskId);

    /**
     * 根据OCR任务ID分页查询订单列表
     * @param map 查询参数，包含ocrTaskId、offset、limit
     * @return 订单列表
     */
    List<NxDepartmentOrdersEntity> queryListByOcrTaskIdWithPage(Map<String, Object> map);

    /**
     * 根据OCR任务ID查询订单总数
     * @param ocrTaskId OCR任务ID
     * @return 订单总数
     */
    Integer queryTotalByOcrTaskId(Integer ocrTaskId);

    /**
     * 根据OCR任务ID一次性查询各状态订单数量（避免遍历内存列表）
     * @param ocrTaskId OCR任务ID
     * @return Map: completedCount(status=0), pendingCount(status=-2)
     */
    Map<String, Object> queryCountByOcrTaskIdGroupByStatus(Integer ocrTaskId);

    /**
     * 批量查询多个部门的订单列表（简化版，返回DTO）
     * 用于优化子部门订单查询，避免N+1问题
     */
    List<NxDepartmentOrdersSimpleDTO> queryDisOrdersSimpleByDepIds(Map<String, Object> map1);

    List<GbDistributerEntity> queryOrderGbDistributerList(Map<String, Object> map1);

    NxDepartmentOrdersEntity queryGbOrderItem(Integer gbDepartmentOrdersId);

    List<NxDistributerFatherGoodsEntity> disGetPurchaseGoodsApplys(Map<String, Object> map);

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
    List<com.nongxinle.entity.GreatGrandFatherGoodsSimpleDTO> queryGreatGrandOrderFatherGoodsUltraSimple(Map<String, Object> map);

    List<com.nongxinle.entity.OutGoodsSimpleDTO> queryOutGoodsWithOrdersUltraSimple(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGoodsForTodayOrders(Map<String, Object> map);

    List<NxDepartmentEntity> queryPureOrderNxDepartment(Map<String, Object> map);

    List<GbDepartmentEntity> queryPureOrderGbDepartment(Map<String, Object> map);

    double queryDisGoodsOrderWeightTotal(Map<String, Object> map1);

    List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApplyForStock(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryPrintDepOrder(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGetPrintOrderGreatGrandGoods(Map<String, Object> map);

    List<NxDepartmentOrdersEntity> queryDepWeightOrderGb(Map<String, Object> map);

    /**
     * 根据depFatherId查询订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     */
    List<NxDepartmentOrdersEntity> queryDepOrdersWithTraceReport(Map<String, Object> map);

    /**
     * 根据departmentBillId查询订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     */
    List<NxDepartmentOrdersEntity> queryOrdersByBillIdWithTraceReport(Map<String, Object> map);

    List<NxDepartmentEntity> queryDistributerFatherGoodsTodayDepartments(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryFatherGoodsByParams(Map<String, Object> map1222);

    double queryDepOrdersProfitScale(Map<String, Object> map1222);

    double queryCostSubtotal(Map<String, Object> map1222);

    List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map2);

    Integer queryDepOrdersAcountByDepGoods(Map<String, Object> mapDep);

    List<NxDistributerGoodsShelfEntity> queryShelfGoodsOrder(Map<String, Object> map);

    Integer queryReturnOrderCount(Map<String, Object> map);

    double queryReturnSubtotal(Map<String, Object> mapR);

//    List<GbDepartmentEntity> queryqueryOrderGbDepartmentList(Map<String, Object> map1);

    NxDepartmentOrdersEntity queryNxOrderByGbOrderId(Integer gbDoNxDepartmentOrderId);

    int insertFromOrder(Integer nxDepartmentOrdersId);

    NxDepartmentOrdersEntity queryHistoryOrderId(Integer orderId);

    int insertToOrder(Integer orderId);

    List<NxDepartmentEntity> queryPureOrderNxDepartmentSimple(Map<String, Object> map);

    List<Map<String, Object>> batchQueryDepStats(@Param("list") List<Integer> depIds, @Param("params") Map<String, Object> params);

    List<Map<String, Object>> batchQueryGbDepStats(@Param("list") List<Integer> gbDepIds, @Param("params") Map<String, Object> params);

    List<Map<String, Object>> batchQueryDepartmentStats(List<Integer> depIds);

    List<Map<String, Object>> batchQueryGbDepartmentStats(List<Integer> gbDepIds);

    int queryLinshiGoodsCount(Map<String, Object> countParams);

    List<Map<String, Object>> batchQueryFatherGoodsOrderCount(Map<String, Object> params);
    
    List<Map<String, Object>> batchQueryDepartmentOrderStats(@Param("depFatherIds") List<Integer> depFatherIds);
    
    List<Map<String, Object>> batchQueryGbDistributerDepartmentStats(@Param("gbDisIds") List<Integer> gbDisIds, @Param("gbDepIds") List<Integer> gbDepIds);

    List<Integer> queryGoodsIds(Map<String, Object> map);


    List<Integer> queryOnlyNxGoodsIds(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> disGetOutGoodsGrandCata(Map<String, Object> map);

    List<NxDistributerGoodsEntity> disGetNxGoodsApply(Map<String, Object> map);

    List<OutGoodsSimpleDTO> disGetNxGoodsApplyUltraSimple(Map<String, Object> map);

    Integer queryOrderGoodsCount(Map<String, Object> mapCount);

    /**
     * 查询货架列表（仅基本信息，不包含商品详情）
     * @param params 查询参数
     * @return 货架列表
     */
    List<ShelfListSimpleDTO> queryShelfListBasic(Map<String, Object> params);

    /**
     * 查询指定货架的商品数量
     * @param params 查询参数，必须包含shelfId
     * @return 商品数量
     */
    Integer queryShelfGoodsCount(Map<String, Object> params);

    /**
     * 查询非货架订单数量
     * @param params 查询参数
     * @return 非货架订单数量
     */
    Integer queryNonShelfOrdersCount(Map<String, Object> params);

    /**
     * 查询指定货架的商品详情（简化版，只返回必要字段）
     * @param params 查询参数，必须包含targetShelfId
     * @return 货架对象（包含商品和订单，但字段已简化）
     */
    List<NxDistributerGoodsShelfEntity> queryShelfGoodsDetailSimple(Map<String, Object> params);

    /**
     * 查询指定货架的商品详情（超简化版，使用DTO对象，字段扁平化）
     * @param params 查询参数，必须包含targetShelfId
     * @return 货架详情DTO列表（MyBatis 会自动聚合为单个对象，但返回 List 以避免 TooManyResultsException）
     */
    List<ShelfDetailSimpleDTO> queryShelfGoodsDetailUltraSimple(Map<String, Object> params);

    /**
     * 查询类别列表（曾祖父级别，仅基本信息，不包含商品详情）
     * @param params 查询参数
     * @return 类别列表
     */
    List<CategoryListSimpleDTO> queryCategoryListBasic(Map<String, Object> params);

    /**
     * 查询指定类别的商品数量
     * @param params 查询参数，必须包含categoryId
     * @return 商品数量
     */
    Integer queryCategoryGoodsCount(Map<String, Object> params);

    /**
     * 查询指定类别的商品详情（超简化版，使用DTO对象，字段扁平化）
     * @param params 查询参数，必须包含categoryId
     * @return 类别详情DTO（字段已最大程度简化）
     */
    CategoryDetailSimpleDTO queryCategoryGoodsDetailUltraSimple(Map<String, Object> params);

    List<Map<String, Object>> batchQueryDepartmentOrderStatsSunla(@Param("depFatherIds") List<Integer> depFatherIds);

    List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatchDto(Map<String, Object> map2);

    int queryMaxTodayOrder(Integer depIdForTodayOrder);

    List<NxDistributerEntity> queryOfferOrderNxDistributer(Map<String, Object> mapOffer);

    List<NxDepartmentEntity> queryCollDisDeps(Map<String, Object> map);

    NxDepartmentOrdersEntity querycollOrder(Map<String, Object> map);

    List<NxDistributerEntity> queryOfferOrderNxDistributerWithOrder(Map<String, Object> map);

    Integer queryCollReplyPartnerCount(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryOfferOrdersGoods(Map<String, Object> map);

    NxDepartmentOrdersEntity queryByRestrauntId(Integer nxDoNxRestrauntOrderId);

    List<NxDepartmentEntity> queryRetailOrderNxDepartment(Map<String, Object> map);
}
