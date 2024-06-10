package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import com.nongxinle.entity.NxCommunityGoodsSetPropertyEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityGoodsSetPropertyDao extends BaseDao<NxCommunityGoodsSetPropertyEntity> {

    List<NxCommunityGoodsSetPropertyEntity> queryCgGoodsPropertyListByParams(Map<String, Object> mapP);
}
