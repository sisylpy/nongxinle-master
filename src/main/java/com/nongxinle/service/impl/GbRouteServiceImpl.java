package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbRouteDao;
import com.nongxinle.entity.GbRouteEntity;
import com.nongxinle.service.GbRouteService;



@Service("gbRouteService")
public class GbRouteServiceImpl implements GbRouteService {
	@Autowired
	private GbRouteDao gbRouteDao;
	
	@Override
	public GbRouteEntity queryObject(Integer gbRouteId){
		return gbRouteDao.queryObject(gbRouteId);
	}
	
	@Override
	public List<GbRouteEntity> queryList(Map<String, Object> map){
		return gbRouteDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbRouteDao.queryTotal(map);
	}
	
	@Override
	public void save(GbRouteEntity gbRoute){
		gbRouteDao.save(gbRoute);
	}
	
	@Override
	public void update(GbRouteEntity gbRoute){
		gbRouteDao.update(gbRoute);
	}
	
	@Override
	public void delete(Integer gbRouteId){
		gbRouteDao.delete(gbRouteId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbRouteIds){
		gbRouteDao.deleteBatch(gbRouteIds);
	}

    @Override
    public List<GbRouteEntity> getDisRoutesByDisId(Map<String, Object> map) {

		return gbRouteDao.getDisRoutesByDisId(map);
    }

}
