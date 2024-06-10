package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepartmentSettleDao;
import com.nongxinle.entity.GbDepartmentSettleEntity;
import com.nongxinle.service.GbDepartmentSettleService;



@Service("gbDepartmentSettleService")
public class GbDepartmentSettleServiceImpl implements GbDepartmentSettleService {
	@Autowired
	private GbDepartmentSettleDao gbDepartmentSettleDao;
	
	@Override
	public GbDepartmentSettleEntity queryObject(Integer gbDepartmentSettleId){
		return gbDepartmentSettleDao.queryObject(gbDepartmentSettleId);
	}
	
	@Override
	public List<GbDepartmentSettleEntity> queryList(Map<String, Object> map){
		return gbDepartmentSettleDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentSettleDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentSettleEntity gbDepartmentSettle){
		gbDepartmentSettleDao.save(gbDepartmentSettle);
	}
	
	@Override
	public void update(GbDepartmentSettleEntity gbDepartmentSettle){
		gbDepartmentSettleDao.update(gbDepartmentSettle);
	}
	
	@Override
	public void delete(Integer gbDepartmentSettleId){
		gbDepartmentSettleDao.delete(gbDepartmentSettleId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentSettleIds){
		gbDepartmentSettleDao.deleteBatch(gbDepartmentSettleIds);
	}

    @Override
    public List<GbDepartmentSettleEntity> queryDepartmentSettlesByParams(Map<String, Object> map) {

		return gbDepartmentSettleDao.queryDepartmentSettlesByParams(map);
    }

    @Override
    public GbDepartmentSettleEntity queryTotalBySettleId(String settleId) {

		return gbDepartmentSettleDao.queryTotalBySettleId(settleId);
    }

}
