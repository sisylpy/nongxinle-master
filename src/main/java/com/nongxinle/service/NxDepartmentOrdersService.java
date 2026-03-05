package com.nongxinle.service;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.dto.PasteSearchGoodsResponseDTO;
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

    /**
     * 查询订单列表（简化版，返回DTO，只包含必要字段）
     * 用于phoneGetToFillDepOrders接口优化
     */
    List<NxDepartmentOrdersSimpleDTO> queryNotWeightDisOrdersSimpleByParams(Map<String, Object> map1);

    /**
     * 批量查询多个部门的订单列表（简化版，返回DTO）
     * 用于优化子部门订单查询，避免N+1问题
     */
    List<NxDepartmentOrdersSimpleDTO> queryDisOrdersSimpleByDepIds(Map<String, Object> map1);

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

    Map<Integer, Map<String, Integer>> batchQueryDepStats(@Param("list") List<Integer> depIds, @Param("params") Map<String, Object> params);


    Map<Integer, Map<String, Integer>> batchQueryGbDepStats(@Param("list") List<Integer>  gbDepIds, @Param("params") Map<String, Object> params);

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

    List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatchDto(Map<String, Object> map2);
    
    /**
     * 从 OCR 识别结果中搜索商品并保存订单
     * 将业务逻辑从 Controller 层下沉到 Service 层
     * 
     * @param orderList 订单列表（来自 OCR 识别结果）
     * @return 处理后的订单响应列表
     */
    List<NxDepartmentOrdersEntity> searchAndSaveOrdersFromOcr(List<NxDepartmentOrdersEntity> orderList);
    
    /**
     * 保存订单（包含完整的订单保存逻辑，从 Controller 的 saveOneOrder 方法提取）
     *
     * @param order 订单实体
     * @param disGoodsEntity 分销商商品实体
     * @return 保存后的订单实体
     */
    NxDepartmentOrdersEntity saveOrderWithGoods(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity);

    /**
     * 为订单添加推荐商品（参考 pasteSearchGoods 的查询方式）
     * 用于弱查询场景，在找到唯一商品后，继续查询相似商品作为推荐
     *
     * @param order 已保存的订单（状态应为 -2）
     * @return 添加推荐商品后的订单实体（订单状态保持不变）
     */
    NxDepartmentOrdersEntity addCommentsGoodsForOrder(NxDepartmentOrdersEntity order);

    void  processOrderPrice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity);

    void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity, Integer inputType, Integer purchaseType);

    int queryMaxTodayOrder(Integer depIdForTodayOrder);
    
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
     * 根据OCR任务ID一次性查询各状态订单数量（status=0已完成, status=-2待修正）
     * @param ocrTaskId OCR任务ID
     * @return int[0]=completedCount, int[1]=pendingCount
     */
    int[] queryCountByOcrTaskIdGroupByStatus(Integer ocrTaskId);

    NxDepartmentOrdersEntity depSaveLinshiGoodsForPasteOrder(NxDepartmentOrdersEntity ordersEntity);

    NxDepartmentOrdersEntity updateOneOrderForChoice(NxDepartmentOrdersEntity ordersEntity, NxDistributerGoodsEntity disGoodsEntity);

    NxDepartmentOrdersEntity saveXiezuoOrderWithGoods(NxDepartmentOrdersEntity xiezuoOrder, NxDistributerGoodsEntity nxDistributerGoodsEntity);

    /**
     * 保存协作订单（完整流程）：当商品所属配送商 != 订单配送商时，创建并保存协作订单，
     * 执行价格处理、采购商品自动创建，并将主订单关联到协作订单。
     *
     * @param mainOrder 主订单（下单配送商的订单）
     * @param disGoodsEntity 商品实体
     * @return 已保存的协作订单，若非协作场景则返回 null
     */
    NxDepartmentOrdersEntity saveCollaborativeOrderWhenNeeded(NxDepartmentOrdersEntity mainOrder, NxDistributerGoodsEntity disGoodsEntity);

    List<NxDistributerEntity> queryOfferOrderNxDistributer(Map<String, Object> mapOffer);

    List<NxDepartmentEntity> queryCollDisDeps(Map<String, Object> map);

    NxDepartmentOrdersEntity querycollOrder(Map<String, Object> map);

    List<NxDistributerEntity> queryOfferOrderNxDistributerWithOrder(Map<String, Object> map);

    Integer queryCollReplyPartnerCount(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryOfferOrdersGoods(Map<String, Object> map);

    NxDepartmentOrdersEntity queryByRestrauntId(Integer nxDoNxRestrauntOrderId);

}
