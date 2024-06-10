package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerGoodsShelfGoodsDao;
import com.nongxinle.entity.GbDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.GbDistributerGoodsShelfGoodsService;



@Service("gbDistributerGoodsShelfGoodsService")
public class GbDistributerGoodsShelfGoodsServiceImpl implements GbDistributerGoodsShelfGoodsService {
	@Autowired
	private GbDistributerGoodsShelfGoodsDao gbDistributerGoodsShelfGoodsDao;
	
	@Override
	public GbDistributerGoodsShelfGoodsEntity queryObject(Integer gbDistributerGoodsShelfGoodsId){
		return gbDistributerGoodsShelfGoodsDao.queryObject(gbDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public List<GbDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerGoodsShelfGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerGoodsShelfGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoods){
		gbDistributerGoodsShelfGoodsDao.save(gbDistributerGoodsShelfGoods);
	}
	
	@Override
	public void update(GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoods){
		gbDistributerGoodsShelfGoodsDao.update(gbDistributerGoodsShelfGoods);
	}
	
	@Override
	public void delete(Integer gbDistributerGoodsShelfGoodsId){
		gbDistributerGoodsShelfGoodsDao.delete(gbDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerGoodsShelfGoodsIds){
		gbDistributerGoodsShelfGoodsDao.deleteBatch(gbDistributerGoodsShelfGoodsIds);
	}

    @Override
    public List<GbDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map) {

		return gbDistributerGoodsShelfGoodsDao.queryShelfGoodsByParams(map);
    }

}
