package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.SysCityDao;
import com.nongxinle.entity.SysCityEntity;
import com.nongxinle.service.SysCityService;



@Service("sysCityService")
public class SysCityServiceImpl implements SysCityService {
	@Autowired
	private SysCityDao sysCityDao;
	
	@Override
	public SysCityEntity queryObject(Integer sysCityId){
		return sysCityDao.queryObject(sysCityId);
	}
	
	@Override
	public List<SysCityEntity> queryList(Map<String, Object> map){
		return sysCityDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return sysCityDao.queryTotal(map);
	}
	
	@Override
	public void save(SysCityEntity sysCity){
		sysCityDao.save(sysCity);
	}
	
	@Override
	public void update(SysCityEntity sysCity){
		sysCityDao.update(sysCity);
	}
	
	@Override
	public void delete(Integer sysCityId){
		sysCityDao.delete(sysCityId);
	}
	
	@Override
	public void deleteBatch(Integer[] sysCityIds){
		sysCityDao.deleteBatch(sysCityIds);
	}
	
}
