package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import com.nongxinle.entity.NxOrderTemplateItemEntity;

import java.util.List;
import java.util.Map;


public interface NxOrderTemplateItemDao extends BaseDao<NxOrderTemplateItemEntity> {

    NxOrderTemplateItemEntity queryDisGoods(Map<String, Object> map);

    List<NxOrderTemplateItemEntity> queryUserItem(Map<String, Object> map1);

    List<NxOrderTemplateItemEntity> queryCustomerUserItems(Integer nxCustomerId);
}
