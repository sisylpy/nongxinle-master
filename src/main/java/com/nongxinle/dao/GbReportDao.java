package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 09-26 20:05
 */

import com.nongxinle.entity.GbReportEntity;

import java.util.List;
import java.util.Map;


public interface GbReportDao extends BaseDao<GbReportEntity> {

    List<GbReportEntity> queryReportList(Map<String, Object> map);
}
