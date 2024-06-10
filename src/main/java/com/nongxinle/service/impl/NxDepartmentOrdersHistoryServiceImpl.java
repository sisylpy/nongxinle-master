package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentOrdersHistoryDao;
import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;
import com.nongxinle.service.NxDepartmentOrdersHistoryService;



@Service("nxDepartmentOrdersHistoryService")
public class NxDepartmentOrdersHistoryServiceImpl implements NxDepartmentOrdersHistoryService {
	@Autowired
	private NxDepartmentOrdersHistoryDao nxDepartmentOrdersHistoryDao;
	
	@Override
	public NxDepartmentOrdersHistoryEntity queryObject(Integer nxDepartmentOrdersHistoryId){
		return nxDepartmentOrdersHistoryDao.queryObject(nxDepartmentOrdersHistoryId);
	}
	
	@Override
	public List<NxDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map){
		return nxDepartmentOrdersHistoryDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDepartmentOrdersHistoryDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory){
		nxDepartmentOrdersHistoryDao.save(nxDepartmentOrdersHistory);
	}
	
	@Override
	public void update(NxDepartmentOrdersHistoryEntity nxDepartmentOrdersHistory){
		nxDepartmentOrdersHistoryDao.update(nxDepartmentOrdersHistory);
	}
	
	@Override
	public void delete(Integer nxDepartmentOrdersHistoryId){
		nxDepartmentOrdersHistoryDao.delete(nxDepartmentOrdersHistoryId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDepartmentOrdersHistoryIds){
		nxDepartmentOrdersHistoryDao.deleteBatch(nxDepartmentOrdersHistoryIds);
	}

    @Override
    public List<NxDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParams(Map<String, Object> map1) {
        return nxDepartmentOrdersHistoryDao.queryDepHistoryOrdersByParams(map1);
    }

    @Override
    public int queryOrderTimes(Map<String, Object> map) {

		return nxDepartmentOrdersHistoryDao.queryOrderTimes(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDepTodayOrder(Map<String, Object> map) {

		return nxDepartmentOrdersHistoryDao.queryDepTodayOrder(map);
    }

}
