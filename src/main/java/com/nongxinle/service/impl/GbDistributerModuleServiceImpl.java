package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerModuleDao;
import com.nongxinle.entity.GbDistributerModuleEntity;
import com.nongxinle.service.GbDistributerModuleService;



@Service("gbDistributerModuleService")
public class GbDistributerModuleServiceImpl implements GbDistributerModuleService {
	@Autowired
	private GbDistributerModuleDao gbDistributerModuleDao;
	
	@Override
	public GbDistributerModuleEntity queryObject(Integer gbDistributerModuleId){
		return gbDistributerModuleDao.queryObject(gbDistributerModuleId);
	}
	
	@Override
	public List<GbDistributerModuleEntity> queryList(Map<String, Object> map){
		return gbDistributerModuleDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerModuleDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerModuleEntity gbDistributerModule){
		gbDistributerModuleDao.save(gbDistributerModule);
	}
	
	@Override
	public void update(GbDistributerModuleEntity gbDistributerModule){
		gbDistributerModuleDao.update(gbDistributerModule);
	}
	
	@Override
	public void delete(Integer gbDistributerModuleId){
		gbDistributerModuleDao.delete(gbDistributerModuleId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerModuleIds){
		gbDistributerModuleDao.deleteBatch(gbDistributerModuleIds);
	}
	
}
