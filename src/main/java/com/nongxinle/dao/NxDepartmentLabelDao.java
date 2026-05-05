package com.nongxinle.dao;

import com.nongxinle.entity.NxDepartmentLabelEntity;

import java.util.List;
import java.util.Map;

public interface NxDepartmentLabelDao extends BaseDao<NxDepartmentLabelEntity> {

    int deleteByDepartmentId(Integer depId);

    int deleteByDepartmentIdAndLabelId(Map<String, Object> map);

    int deleteByDistributerLabelId(Integer labelId);

    List<Integer> queryLabelIdsByDepartmentId(Integer depId);
}
