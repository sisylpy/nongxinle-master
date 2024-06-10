package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerFoodDao;
import com.nongxinle.entity.GbDistributerFoodEntity;
import com.nongxinle.service.GbDistributerFoodService;



@Service("gbDistributerFoodService")
public class GbDistributerFoodServiceImpl implements GbDistributerFoodService {
	@Autowired
	private GbDistributerFoodDao gbDistributerFoodDao;
	
	@Override
	public GbDistributerFoodEntity queryObject(Integer gbDistributerFoodId){
		return gbDistributerFoodDao.queryObject(gbDistributerFoodId);
	}
	
	@Override
	public List<GbDistributerFoodEntity> queryList(Map<String, Object> map){
		return gbDistributerFoodDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerFoodDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerFoodEntity gbDistributerFood){
		gbDistributerFoodDao.save(gbDistributerFood);
	}
	
	@Override
	public void update(GbDistributerFoodEntity gbDistributerFood){
		gbDistributerFoodDao.update(gbDistributerFood);
	}
	
	@Override
	public void delete(Integer gbDistributerFoodId){
		gbDistributerFoodDao.delete(gbDistributerFoodId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerFoodIds){
		gbDistributerFoodDao.deleteBatch(gbDistributerFoodIds);
	}

    @Override
    public List<GbDistributerFoodEntity> queryFoodByParams(Map<String, Object> map) {

		return gbDistributerFoodDao.queryFoodByParams(map);
    }

    @Override
    public List<GbDistributerFoodEntity> queryDisAllFood(Map<String, Object> map) {

		return gbDistributerFoodDao.queryDisAllFood(map);
    }

}
