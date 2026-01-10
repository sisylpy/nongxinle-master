package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-19 23:55
 */

import com.nongxinle.entity.DailyUsage;
import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDepartmentOrderHistoryDao extends BaseDao<NxDepartmentOrderHistoryEntity> {

    List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParams(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map);

    Double queryDepOrdersSubtotal(Map<String, Object> map);

    Integer queryDepOrdersAcount(Map<String, Object> map);

    double queryDisGoodsOrderWeightTotal(Map<String, Object> mapFather);

    List<Integer> queryGoodsIds(Map<String, Object> mapToday);

    NxDepartmentOrderHistoryEntity selectLastOrder(Map<String, Object> lastParams);

    List<DailyUsage> selectDailyUsage(Map<String, Object> usageParams);

    List<Integer> selectFrequentGoods(Map<String, Object> freqParams);

    List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParamsForAi(Map<String, Object> orderHistoryParams);

    List<Integer> selectFrequentGoodsWithPage(Map<String, Object> freqParams);

    int selectFrequentGoodsCount(Map<String, Object> freqParams);

    List<Map<String, Object>> queryDepGoodsHistoryPrice(Map<String, Object> map);

    Integer queryReturnOrderCount(Map<String, Object> mapR);

    double queryReturnSubtotal(Map<String, Object> mapR);

    /**
     * 根据departmentBillId查询历史订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     */
    List<NxDepartmentOrderHistoryEntity> queryOrdersByBillIdWithTraceReport(Map<String, Object> map);
}
