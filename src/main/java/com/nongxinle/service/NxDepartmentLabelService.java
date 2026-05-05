package com.nongxinle.service;

import com.nongxinle.entity.NxDepartmentLabelEntity;

import java.util.Collection;
import java.util.List;

public interface NxDepartmentLabelService {

    void save(NxDepartmentLabelEntity nxDepartmentLabel);

    void deleteByDepartmentId(Integer depId);

    void deleteByDepartmentIdAndLabelId(Integer depId, Integer labelId);

    List<Integer> queryLabelIdsByDepartmentId(Integer depId);

    void saveDepartmentLabels(Integer depId, Integer disId, List<Integer> labelIds);

    void syncDepartmentLabelsByDiff(Integer depId, Integer disId, Collection<Integer> labelIds);
}
