package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCommunityRestrauantEntity;
import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityRestrauantDao extends BaseDao<NxCommunityRestrauantEntity> {

    List<NxRestrauntEntity> queryRestrauntsByComId(Integer comId);

    NxCommunityEntity queryCommunityByResId(Integer resId);

    List<NxRestrauntEntity> queryRestrauntsByParams(Map<String, Object> map);

}
