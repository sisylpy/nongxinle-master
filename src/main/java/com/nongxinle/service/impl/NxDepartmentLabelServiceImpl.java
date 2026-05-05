package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentLabelDao;
import com.nongxinle.entity.NxDepartmentLabelEntity;
import com.nongxinle.service.NxDepartmentLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;

@Service("nxDepartmentLabelService")
public class NxDepartmentLabelServiceImpl implements NxDepartmentLabelService {

    @Autowired
    private NxDepartmentLabelDao nxDepartmentLabelDao;

    @Override
    public void save(NxDepartmentLabelEntity nxDepartmentLabel) {
        nxDepartmentLabelDao.save(nxDepartmentLabel);
    }

    @Override
    public void deleteByDepartmentId(Integer depId) {
        nxDepartmentLabelDao.deleteByDepartmentId(depId);
    }

    @Override
    public void deleteByDepartmentIdAndLabelId(Integer depId, Integer labelId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("labelId", labelId);
        nxDepartmentLabelDao.deleteByDepartmentIdAndLabelId(map);
    }

    @Override
    public List<Integer> queryLabelIdsByDepartmentId(Integer depId) {
        return nxDepartmentLabelDao.queryLabelIdsByDepartmentId(depId);
    }

    @Override
    public void saveDepartmentLabels(Integer depId, Integer disId, List<Integer> labelIds) {
        nxDepartmentLabelDao.deleteByDepartmentId(depId);
        if (labelIds == null || labelIds.size() == 0) {
            return;
        }
        for (Integer labelId : labelIds) {
            if (labelId == null) {
                continue;
            }
            NxDepartmentLabelEntity entity = new NxDepartmentLabelEntity();
            entity.setNxDdlDepartmentId(depId);
            entity.setNxDdlDistributerLabelId(labelId);
            entity.setNxDdlDistributerId(disId);
            nxDepartmentLabelDao.save(entity);
        }
    }

    @Override
    public void syncDepartmentLabelsByDiff(Integer depId, Integer disId, Collection<Integer> labelIds) {
        List<Integer> oldLabelIds = nxDepartmentLabelDao.queryLabelIdsByDepartmentId(depId);
        Set<Integer> oldSet = new HashSet<>();
        if (oldLabelIds != null) {
            oldSet.addAll(oldLabelIds);
        }

        Set<Integer> newSet = new HashSet<>();
        if (labelIds != null) {
            for (Integer id : labelIds) {
                if (id != null) {
                    newSet.add(id);
                }
            }
        }

        for (Integer oldId : oldSet) {
            if (!newSet.contains(oldId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", depId);
                map.put("labelId", oldId);
                nxDepartmentLabelDao.deleteByDepartmentIdAndLabelId(map);
            }
        }

        for (Integer newId : newSet) {
            if (!oldSet.contains(newId)) {
                NxDepartmentLabelEntity entity = new NxDepartmentLabelEntity();
                entity.setNxDdlDepartmentId(depId);
                entity.setNxDdlDistributerLabelId(newId);
                entity.setNxDdlDistributerId(disId);
                nxDepartmentLabelDao.save(entity);
            }
        }
    }
}
