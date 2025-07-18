package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-23 14:26
 */

import com.nongxinle.entity.NxCommunityCardEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityCardDao extends BaseDao<NxCommunityCardEntity> {

    List<NxCommunityCardEntity> queryCardListByParams(Map<String, Object> map);
}
