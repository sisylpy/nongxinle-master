package com.nongxinle.service;

/**
 * OCR任务Service接口
 * 
 * @author lpy
 * @date 2025-01-24
 */

import com.nongxinle.entity.NxOcrTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;

import java.util.List;
import java.util.Map;

public interface NxOcrTaskService {
	
	NxOcrTaskEntity queryObject(Integer nxOcrTaskId);
	
	List<NxOcrTaskEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxOcrTaskEntity nxOcrTask);
	
	void update(NxOcrTaskEntity nxOcrTask);
	
	void delete(Integer nxOcrTaskId);
	
	void deleteBatch(Integer[] nxOcrTaskIds);
	
	/**
	 * 更新任务统计信息（已完成订单数、未完成订单数、任务状态）
	 * @param taskId 任务ID
	 */
	void updateTaskStatistics(Integer taskId);
	
	/**
	 * 根据部门ID和状态查询任务列表
	 * @param map 查询条件（包含departmentId和status）
	 * @return 任务列表
	 */
	List<NxOcrTaskEntity> queryTasksByDepartmentAndStatus(Map<String, Object> map);
	
	/**
	 * 根据部门ID和状态查询任务总数
	 * @param map 查询条件（包含departmentId和status）
	 * @return 任务总数
	 */
	int queryTotalByDepartmentAndStatus(Map<String, Object> map);
	
	/**
	 * 根据分销商ID查询有任务的部门父列表
	 * @param map 查询条件（包含disId和xiaoyuStatus）
	 * @return 部门父列表（去重）
	 */
	List<NxDepartmentEntity> queryTasksDepartmentByDisId(Map<String, Object> map);

}
