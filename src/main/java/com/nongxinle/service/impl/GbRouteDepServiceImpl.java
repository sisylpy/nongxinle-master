package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbRouteDepDao;
import com.nongxinle.entity.GbRouteDepEntity;
import com.nongxinle.service.GbRouteDepService;



@Service("gbRouteDepService")
public class GbRouteDepServiceImpl implements GbRouteDepService {
	@Autowired
	private GbRouteDepDao gbRouteDepDao;
	
	@Override
	public GbRouteDepEntity queryObject(Integer gbRouteDepId){
		return gbRouteDepDao.queryObject(gbRouteDepId);
	}
	
	@Override
	public List<GbRouteDepEntity> queryList(Map<String, Object> map){
		return gbRouteDepDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbRouteDepDao.queryTotal(map);
	}
	
	@Override
	public void save(GbRouteDepEntity gbRouteDep){
		gbRouteDepDao.save(gbRouteDep);
	}
	
	@Override
	public void update(GbRouteDepEntity gbRouteDep){
		gbRouteDepDao.update(gbRouteDep);
	}
	
	@Override
	public void delete(Integer gbRouteDepId){
		gbRouteDepDao.delete(gbRouteDepId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbRouteDepIds){
		gbRouteDepDao.deleteBatch(gbRouteDepIds);
	}

    @Override
    public List<GbDepartmentEntity> queryHaveLineDepsByDisId(Integer disId) {

		return gbRouteDepDao.queryHaveLineDepsByDisId(disId);
    }

}
