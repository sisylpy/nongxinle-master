package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityGoodsSetItemDao extends BaseDao<NxCommunityGoodsSetItemEntity> {


    List<NxCommunityGoodsSetItemEntity> queryCgGoodsSetListByParams(Map<String, Object> map);
}
