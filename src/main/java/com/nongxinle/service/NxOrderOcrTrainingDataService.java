package com.nongxinle.service;

/**
 * 订单OCR训练数据Service接口
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxOrderOcrTrainingDataEntity;

import java.util.List;
import java.util.Map;

public interface NxOrderOcrTrainingDataService {
	
	NxOrderOcrTrainingDataEntity queryObject(Integer nxOtdId);
	
	List<NxOrderOcrTrainingDataEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxOrderOcrTrainingDataEntity nxOrderOcrTrainingData);
	
	void update(NxOrderOcrTrainingDataEntity nxOrderOcrTrainingData);
	
	void delete(Integer nxOtdId);
	
	void deleteBatch(Integer[] nxOtdIds);

    /**
     * 根据订单ID查询训练数据
     * @param orderId 订单ID
     * @return 训练数据实体
     */
    NxOrderOcrTrainingDataEntity queryByOrderId(Integer orderId);

    /**
     * 根据条件查询训练数据列表
     * @param map 查询条件
     * @return 训练数据列表
     */
    List<NxOrderOcrTrainingDataEntity> queryTrainingDataList(Map<String, Object> map);

    /**
     * 根据部门ID、商品名称、订货数量、订货规格、规格重量查询训练数据（用于匹配）
     * @param map 查询条件（包含：departmentId, goodsName, quantity, standard, standardWeight）
     * @return 匹配的训练数据，如果找到多条返回第一条
     */
    NxOrderOcrTrainingDataEntity queryByMatchFields(Map<String, Object> map);
}
