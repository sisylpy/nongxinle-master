package com.nongxinle.dao;

/**
 * OCR任务Dao接口
 * 
 * @author lpy
 * @date 2025-01-24
 */

import com.nongxinle.entity.NxOcrTaskEntity;
import com.nongxinle.entity.NxDepartmentEntity;

import java.util.List;
import java.util.Map;


public interface NxOcrTaskDao extends BaseDao<NxOcrTaskEntity> {

    /**
     * 根据任务ID查询任务信息
     * @param taskId 任务ID
     * @return 任务实体
     */
    NxOcrTaskEntity queryByTaskId(Integer taskId);

    /**
     * 根据条件查询任务列表
     * @param map 查询条件
     * @return 任务列表
     */
    List<NxOcrTaskEntity> queryTaskList(Map<String, Object> map);

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
