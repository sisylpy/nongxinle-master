package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerFoodGoodsDao;
import com.nongxinle.entity.GbDistributerFoodGoodsEntity;
import com.nongxinle.service.GbDistributerFoodGoodsService;



@Service("gbDistributerFoodGoodsService")
public class GbDistributerFoodGoodsServiceImpl implements GbDistributerFoodGoodsService {
	@Autowired
	private GbDistributerFoodGoodsDao gbDistributerFoodGoodsDao;
	
	@Override
	public GbDistributerFoodGoodsEntity queryObject(Integer gbDistributerFoodGoodsId){
		return gbDistributerFoodGoodsDao.queryObject(gbDistributerFoodGoodsId);
	}
	
	@Override
	public List<GbDistributerFoodGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerFoodGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerFoodGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerFoodGoodsEntity gbDistributerFoodGoods){
		gbDistributerFoodGoodsDao.save(gbDistributerFoodGoods);
	}
	
	@Override
	public void update(GbDistributerFoodGoodsEntity gbDistributerFoodGoods){
		gbDistributerFoodGoodsDao.update(gbDistributerFoodGoods);
	}
	
	@Override
	public void delete(Integer gbDistributerFoodGoodsId){
		gbDistributerFoodGoodsDao.delete(gbDistributerFoodGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerFoodGoodsIds){
		gbDistributerFoodGoodsDao.deleteBatch(gbDistributerFoodGoodsIds);
	}

    @Override
    public List<GbDistributerFoodGoodsEntity> queryFoodGoodsByParams(Map<String, Object> map) {

		return gbDistributerFoodGoodsDao.queryFoodGoodsByParams(map);
    }

}
