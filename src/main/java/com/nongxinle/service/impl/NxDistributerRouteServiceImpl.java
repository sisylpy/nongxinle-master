package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerRouteDao;
import com.nongxinle.entity.NxDistributerRouteEntity;
import com.nongxinle.service.NxDistributerRouteService;



@Service("nxDistributerRouteService")
public class NxDistributerRouteServiceImpl implements NxDistributerRouteService {
	@Autowired
	private NxDistributerRouteDao nxDistributerRouteDao;
	
	@Override
	public NxDistributerRouteEntity queryObject(Integer nxDistributerRouteId){
		return nxDistributerRouteDao.queryObject(nxDistributerRouteId);
	}
	
	@Override
	public List<NxDistributerRouteEntity> queryList(Map<String, Object> map){
		return nxDistributerRouteDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerRouteDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerRouteEntity nxDistributerRoute){
		nxDistributerRouteDao.save(nxDistributerRoute);
	}
	
	@Override
	public void update(NxDistributerRouteEntity nxDistributerRoute){
		nxDistributerRouteDao.update(nxDistributerRoute);
	}
	
	@Override
	public void delete(Integer nxDistributerRouteId){
		nxDistributerRouteDao.delete(nxDistributerRouteId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerRouteIds){
		nxDistributerRouteDao.deleteBatch(nxDistributerRouteIds);
	}
	
}
