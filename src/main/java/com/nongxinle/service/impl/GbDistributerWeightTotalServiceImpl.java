package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerWeightTotalDao;
import com.nongxinle.entity.GbDistributerWeightTotalEntity;
import com.nongxinle.service.GbDistributerWeightTotalService;



@Service("gbDistributerWeightTotalService")
public class GbDistributerWeightTotalServiceImpl implements GbDistributerWeightTotalService {
	@Autowired
	private GbDistributerWeightTotalDao gbDistributerWeightTotalDao;
	
	@Override
	public GbDistributerWeightTotalEntity queryObject(Integer gbDistributerWeightTotalId){
		return gbDistributerWeightTotalDao.queryObject(gbDistributerWeightTotalId);
	}
	
	@Override
	public List<GbDistributerWeightTotalEntity> queryList(Map<String, Object> map){
		return gbDistributerWeightTotalDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerWeightTotalDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerWeightTotalEntity gbDistributerWeightTotal){
		gbDistributerWeightTotalDao.save(gbDistributerWeightTotal);
	}
	
	@Override
	public void update(GbDistributerWeightTotalEntity gbDistributerWeightTotal){
		gbDistributerWeightTotalDao.update(gbDistributerWeightTotal);
	}
	
	@Override
	public void delete(Integer gbDistributerWeightTotalId){
		gbDistributerWeightTotalDao.delete(gbDistributerWeightTotalId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerWeightTotalIds){
		gbDistributerWeightTotalDao.deleteBatch(gbDistributerWeightTotalIds);
	}

    @Override
    public List<GbDistributerWeightTotalEntity> queryDepWeightListByParams(Map<String, Object> map) {

		return gbDistributerWeightTotalDao.queryDepWeightListByParams(map);
    }

    @Override
    public int queryDepWeightCountByParams(Map<String, Object> map3) {

		return gbDistributerWeightTotalDao.queryDepWeightCountByParams(map3);
    }

}
