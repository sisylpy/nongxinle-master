package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.nongxinle.dao.NxDepartmentOrdersHistoryDao;



@Service("nxDepartmentOrdersHistoryService")
public class NxDepartmentOrdersHistoryServiceImpl implements NxDepartmentOrdersHistoryService {

    @Autowired
    private NxDepartmentOrdersHistoryDao nxDepartmentOrdersHistoryDao;
    @Autowired
    NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    NxDepartmentDisGoodsService nxDepartmentDisGoodsService;

    @Override
    public NxDepartmentOrdersHistoryEntity queryObject(Integer nxDepartmentOrdersHistoryId) {
        return nxDepartmentOrdersHistoryDao.queryObject(nxDepartmentOrdersHistoryId);
    }

    @Override
    public List<NxDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map) {
        return nxDepartmentOrdersHistoryDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxDepartmentOrdersHistoryDao.queryTotal(map);
    }

    @Override
    public void save(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory) {
        nxDepartmentOrdersHistoryDao.save(nxDepartmentOrdersHistory);
    }

    @Override
    public void update(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory) {
        nxDepartmentOrdersHistoryDao.update(nxDepartmentOrdersHistory);
    }

    @Override
    public void delete(Integer nxDepartmentOrdersHistoryId) {
        nxDepartmentOrdersHistoryDao.delete(nxDepartmentOrdersHistoryId);
    }

    @Override
    public void deleteBatch(Integer[] nxDepartmentOrdersHistoryIds) {
        nxDepartmentOrdersHistoryDao.deleteBatch(nxDepartmentOrdersHistoryIds);
    }

    @Override
    public List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1) {
        return nxDepartmentOrdersHistoryDao.queryDepHistoryOrdersByParams(map1);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map) {

        return nxDepartmentOrdersHistoryDao.queryDepTodayOrder(map);
    }


    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> mapGD) {

        return nxDepartmentOrdersHistoryDao.queryDisGoodsByParams(mapGD);
    }

    @Override
    public List<Map<String, Object>> queryLogs(Map<String, Object> mapLog) {

        return nxDepartmentOrdersHistoryDao.queryLogs(mapLog);
    }




}

