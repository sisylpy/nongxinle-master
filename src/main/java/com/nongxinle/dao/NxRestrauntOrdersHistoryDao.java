package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-07 09:20
 */

import com.nongxinle.entity.NxRestrauntOrdersHistoryEntity;

import java.util.List;
import java.util.Map;


public interface NxRestrauntOrdersHistoryDao extends BaseDao<NxRestrauntOrdersHistoryEntity> {

    List<NxRestrauntOrdersHistoryEntity> queryHistoryOrdersByParams(Map<String, Object> map);
}
