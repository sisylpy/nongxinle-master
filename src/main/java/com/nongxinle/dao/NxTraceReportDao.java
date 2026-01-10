package com.nongxinle.dao;

/**
 * 溯源报告Dao接口
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxTraceReportEntity;

import java.util.List;
import java.util.Map;


public interface NxTraceReportDao extends BaseDao<NxTraceReportEntity> {

    /**
     * 根据条件查询溯源报告列表
     * @param map 查询条件
     * @return 溯源报告列表
     */
    List<NxTraceReportEntity> queryTraceReportList(Map<String, Object> map);
}

