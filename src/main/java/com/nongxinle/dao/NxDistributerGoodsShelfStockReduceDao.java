package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsShelfStockReduceDao extends BaseDao<NxDistributerGoodsShelfStockReduceEntity> {

    List<NxDistributerGoodsShelfStockReduceEntity> queryReduceListByParams(Map<String, Object> map);
    
    Integer queryReduceTypeCount(Map<String, Object> map);
    
    Double queryReduceCostSubtotal(Map<String, Object> map);

    // 出货统计新增方法
    Double queryReduceWasteSubtotal(Map<String, Object> map);
    
    Double queryReduceLossSubtotal(Map<String, Object> map);
    
    Double queryReduceProduceSubtotal(Map<String, Object> map);
    
    Integer queryReduceGoodsTotalCount(Map<String, Object> map);
    
    List<NxDistributerGoodsEntity> queryGoodsWithReduceRecords(Map<String, Object> map);

    List<NxDistributerGoodsShelfStockReduceEntity> queryReduceRecordsByDisGoodsIdAndDate(Map<String, Object> map);

    // 盘库相关方法
    NxDistributerGoodsShelfStockReduceEntity queryStockInventoryRecord(Map<String, Object> map);

    // 统计TOP方法
    List<NxDistributerGoodsEntity> queryStockProduceSubtotalTopTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryStockLossSubtotalTopTimes(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryStockWasteSubtotalTopTimes(Map<String, Object> map);

	List<Map<String, Object>> queryNxPurchaseGoodsTopDay(Map<String, Object> map);

	Double queryReduceReturnTotal(Map<String, Object> map);

	// 查询重量方法
	Double queryReduceWasteWeightTotal(Map<String, Object> map);

	Double queryReduceLossWeightTotal(Map<String, Object> map);

	Double queryReduceProduceWeightTotal(Map<String, Object> map);

	Double queryReduceCostWeightTotal(Map<String, Object> map);

	// 查询门店列表（通过订单关联）
	List<NxDepartmentEntity> queryReduceDepartment(Map<String, Object> map);
}
