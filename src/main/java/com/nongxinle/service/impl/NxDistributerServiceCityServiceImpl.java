package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerServiceCityDao;
import com.nongxinle.entity.NxDistributerServiceCityEntity;
import com.nongxinle.service.NxDistributerServiceCityService;



@Service("nxDistributerServiceCityService")
public class NxDistributerServiceCityServiceImpl implements NxDistributerServiceCityService {
	@Autowired
	private NxDistributerServiceCityDao nxDistributerServiceCityDao;
	
	@Override
	public NxDistributerServiceCityEntity queryObject(Integer nxDistributerServiceCity){
		return nxDistributerServiceCityDao.queryObject(nxDistributerServiceCity);
	}
	
	@Override
	public List<NxDistributerServiceCityEntity> queryList(Map<String, Object> map){
		return nxDistributerServiceCityDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerServiceCityDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerServiceCityEntity nxDistributerServiceCity){
		nxDistributerServiceCityDao.save(nxDistributerServiceCity);
	}
	
	@Override
	public void update(NxDistributerServiceCityEntity nxDistributerServiceCity){
		nxDistributerServiceCityDao.update(nxDistributerServiceCity);
	}
	
	@Override
	public void delete(Integer nxDistributerServiceCity){
		nxDistributerServiceCityDao.delete(nxDistributerServiceCity);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerServiceCitys){
		nxDistributerServiceCityDao.deleteBatch(nxDistributerServiceCitys);
	}
	
}
