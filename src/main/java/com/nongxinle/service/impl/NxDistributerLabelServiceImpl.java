package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerLabelDao;
import com.nongxinle.dao.NxDepartmentLabelDao;
import com.nongxinle.entity.NxDistributerLabelEntity;
import com.nongxinle.service.NxDistributerLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("nxDistributerLabelService")
public class NxDistributerLabelServiceImpl implements NxDistributerLabelService {

    @Autowired
    private NxDistributerLabelDao nxDistributerLabelDao;
    @Autowired
    private NxDepartmentLabelDao nxDepartmentLabelDao;

    @Override
    public NxDistributerLabelEntity queryObject(Integer nxDistributerLabelId) {
        return nxDistributerLabelDao.queryObject(nxDistributerLabelId);
    }

    @Override
    public void save(NxDistributerLabelEntity nxDistributerLabel) {
        if (nxDistributerLabel.getNxDlSort() == null) {
            nxDistributerLabel.setNxDlSort(0);
        }
        if (nxDistributerLabel.getNxDlStatus() == null) {
            nxDistributerLabel.setNxDlStatus(1);
        }
        nxDistributerLabelDao.save(nxDistributerLabel);
    }

    @Override
    public void update(NxDistributerLabelEntity nxDistributerLabel) {
        nxDistributerLabelDao.update(nxDistributerLabel);
    }

    @Override
    @Transactional
    public void delete(Integer nxDistributerLabelId) {
        // 先删部门与该标签的关系，再删标签主表，避免残留脏数据。
        nxDepartmentLabelDao.deleteByDistributerLabelId(nxDistributerLabelId);
        nxDistributerLabelDao.delete(nxDistributerLabelId);
    }

    @Override
    public List<NxDistributerLabelEntity> queryLabelsByDisId(Integer disId) {
        return nxDistributerLabelDao.queryLabelsByDisId(disId);
    }
}
