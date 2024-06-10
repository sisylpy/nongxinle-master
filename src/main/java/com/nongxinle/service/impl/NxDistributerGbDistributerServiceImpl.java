package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerGbDistributerDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxDistributerGbDistributerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerDepartmentDao;
import com.nongxinle.service.NxDistributerDepartmentService;



@Service("nxDistributerGbDistributerService")
public class NxDistributerGbDistributerServiceImpl implements NxDistributerGbDistributerService {


    @Autowired
    private NxDistributerGbDistributerDao nxDistributerGbDistributerDao;

    @Override
    public NxDistributerGbDistributerEntity queryObject(Integer nxDistributerGbDistributerId) {
        return nxDistributerGbDistributerDao.queryObject(nxDistributerGbDistributerId);
    }

    @Override
    public List<GbDistributerEntity> queryAllGbDistribtuer(Integer disId) {

        return nxDistributerGbDistributerDao.queryAllGbDistribtuer(disId);
    }

    @Override
    public List<NxDistributerEntity> queryAllNxDistribtuer(Integer gbDisId) {

        return nxDistributerGbDistributerDao.queryAllNxDistribtuer(gbDisId);
    }

    @Override
    public NxDistributerGbDistributerEntity queryObjectByParams(Map<String, Object> map) {

        return nxDistributerGbDistributerDao.queryObjectByParams(map);
    }

    @Override
    public void save(NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity) {
        nxDistributerGbDistributerDao.save(nxDistributerGbDistributerEntity);

    }

    @Override
    public List<NxDistributerEntity> queryGbDistributerNxDistribtuer(Integer gbDepId) {


        return nxDistributerGbDistributerDao.queryGbDistributerNxDistribtuer(gbDepId);
    }

    @Override
    public void update(NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity) {
        nxDistributerGbDistributerDao.update(nxDistributerGbDistributerEntity);
    }

    @Override
    public void delete(Integer id) {
        nxDistributerGbDistributerDao.delete(id);
    }

    @Override
    public List<NxDistributerEntity> queryGbDistributerNxDistribtuerGoods(Map<String, Object> map) {

        return nxDistributerGbDistributerDao.queryGbDistributerNxDistribtuerGoods(map);
    }
}
