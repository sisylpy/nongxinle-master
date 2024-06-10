package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepFoodDao;
import com.nongxinle.entity.GbDepFoodEntity;
import com.nongxinle.service.GbDepFoodService;



@Service("gbDepFoodService")
public class GbDepFoodServiceImpl implements GbDepFoodService {
	@Autowired
	private GbDepFoodDao gbDepFoodDao;
	
	@Override
	public GbDepFoodEntity queryObject(Integer gbDepFoodId){
		return gbDepFoodDao.queryObject(gbDepFoodId);
	}
	
	@Override
	public List<GbDepFoodEntity> queryList(Map<String, Object> map){
		return gbDepFoodDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepFoodDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepFoodEntity gbDepFood){
		gbDepFoodDao.save(gbDepFood);
	}
	
	@Override
	public void update(GbDepFoodEntity gbDepFood){
		gbDepFoodDao.update(gbDepFood);
	}
	
	@Override
	public void delete(Integer gbDepFoodId){
		gbDepFoodDao.delete(gbDepFoodId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepFoodIds){
		gbDepFoodDao.deleteBatch(gbDepFoodIds);
	}

    @Override
    public List<GbDepFoodEntity> queryDepFoodByParams(Map<String, Object> map) {

		return gbDepFoodDao.queryDepFoodByParams(map);
    }

    @Override
    public List<GbDepFoodEntity> queryDepFoodByParamsWithoutFather(Map<String, Object> map) {

		return gbDepFoodDao.queryDepFoodByParamsWithoutFather(map);
    }

}
