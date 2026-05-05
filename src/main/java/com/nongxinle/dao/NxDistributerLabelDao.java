package com.nongxinle.dao;

import com.nongxinle.entity.NxDistributerLabelEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerLabelDao extends BaseDao<NxDistributerLabelEntity> {

    List<NxDistributerLabelEntity> queryLabelsByDisId(Integer disId);

    int deleteByDisId(Integer disId);

    NxDistributerLabelEntity queryByDisIdAndName(Map<String, Object> map);
}
