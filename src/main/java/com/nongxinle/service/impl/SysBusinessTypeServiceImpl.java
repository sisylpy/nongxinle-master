package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.SysBusinessTypeDao;
import com.nongxinle.entity.SysBusinessTypeEntity;
import com.nongxinle.service.SysBusinessTypeService;



@Service("sysBusinessTypeService")
public class SysBusinessTypeServiceImpl implements SysBusinessTypeService {
	@Autowired
	private SysBusinessTypeDao sysBusinessTypeDao;
	
	@Override
	public SysBusinessTypeEntity queryObject(Integer sysBusinessTypeId){
		return sysBusinessTypeDao.queryObject(sysBusinessTypeId);
	}
	
	@Override
	public List<SysBusinessTypeEntity> queryList(Map<String, Object> map){
		return sysBusinessTypeDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return sysBusinessTypeDao.queryTotal(map);
	}
	
	@Override
	public void save(SysBusinessTypeEntity sysBusinessType){
		sysBusinessTypeDao.save(sysBusinessType);
	}
	
	@Override
	public void update(SysBusinessTypeEntity sysBusinessType){
		sysBusinessTypeDao.update(sysBusinessType);
	}
	
	@Override
	public void delete(Integer sysBusinessTypeId){
		sysBusinessTypeDao.delete(sysBusinessTypeId);
	}
	
	@Override
	public void deleteBatch(Integer[] sysBusinessTypeIds){
		sysBusinessTypeDao.deleteBatch(sysBusinessTypeIds);
	}

    @Override
    public List<SysBusinessTypeEntity> queryTypeNxDistribterWithPeisong(Map<String, Object> map) {

		return sysBusinessTypeDao.queryTypeNxDistribterWithPeisong(map);
    }

}
