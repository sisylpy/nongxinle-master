package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDepartmentOrdersHistoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepartmentOrdersHistoryDao;
import com.nongxinle.entity.GbDepartmentOrdersHistoryEntity;
import com.nongxinle.service.GbDepartmentOrdersHistoryService;



@Service("gbDepartmentOrdersHistoryService")
public class GbDepartmentOrdersHistoryServiceImpl implements GbDepartmentOrdersHistoryService {
	@Autowired
	private GbDepartmentOrdersHistoryDao gbDepartmentOrdersHistoryDao;
	
	@Override
	public GbDepartmentOrdersHistoryEntity queryObject(Integer gbDepartmentOrdersHistoryId){
		return gbDepartmentOrdersHistoryDao.queryObject(gbDepartmentOrdersHistoryId);
	}
	
	@Override
	public List<GbDepartmentOrdersHistoryEntity> queryList(Map<String, Object> map){
		return gbDepartmentOrdersHistoryDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentOrdersHistoryDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory){
		gbDepartmentOrdersHistoryDao.save(gbDepartmentOrdersHistory);
	}
	
	@Override
	public void update(GbDepartmentOrdersHistoryEntity gbDepartmentOrdersHistory){
		gbDepartmentOrdersHistoryDao.update(gbDepartmentOrdersHistory);
	}
	
	@Override
	public void delete(Integer gbDepartmentOrdersHistoryId){
		gbDepartmentOrdersHistoryDao.delete(gbDepartmentOrdersHistoryId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentOrdersHistoryIds){
		gbDepartmentOrdersHistoryDao.deleteBatch(gbDepartmentOrdersHistoryIds);
	}

    @Override
    public List<GbDepartmentOrdersHistoryEntity> queryGbDepHistoryOrdersByParams(Map<String, Object> map1) {
        return gbDepartmentOrdersHistoryDao.queryGbDepHistoryOrdersByParams(map1);
    }

    @Override
    public List<GbDepartmentOrdersHistoryEntity> queryDepHistoryOrdersByParamsGb(Map<String, Object> map1) {

		return gbDepartmentOrdersHistoryDao.queryDepHistoryOrdersByParamsGb(map1);
    }

}
