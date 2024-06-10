package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-25 10:39
 */

import com.nongxinle.entity.NxCommunitySplicingOrdersEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunitySplicingOrdersDao extends BaseDao<NxCommunitySplicingOrdersEntity> {

    NxCommunitySplicingOrdersEntity queryNewPindan(Map<String, Object> map);

    List<NxCommunitySplicingOrdersEntity> querySplicingListByParams(Map<String, Object> map);
}
