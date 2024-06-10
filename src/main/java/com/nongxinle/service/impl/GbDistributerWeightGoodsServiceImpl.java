package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentDisGoodsEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsShelfEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerWeightGoodsDao;
import com.nongxinle.entity.GbDistributerWeightGoodsEntity;
import com.nongxinle.service.GbDistributerWeightGoodsService;



@Service("gbDistributerWeightGoodsService")
public class GbDistributerWeightGoodsServiceImpl implements GbDistributerWeightGoodsService {
	@Autowired
	private GbDistributerWeightGoodsDao gbDistributerWeightGoodsDao;
	
	@Override
	public GbDistributerWeightGoodsEntity queryObject(Integer gbDistributerWeightGoodsId){
		return gbDistributerWeightGoodsDao.queryObject(gbDistributerWeightGoodsId);
	}
	
	@Override
	public List<GbDistributerWeightGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerWeightGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerWeightGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerWeightGoodsEntity gbDistributerWeightGoods){
		gbDistributerWeightGoodsDao.save(gbDistributerWeightGoods);
	}
	
	@Override
	public void update(GbDistributerWeightGoodsEntity gbDistributerWeightGoods){
		gbDistributerWeightGoodsDao.update(gbDistributerWeightGoods);
	}
	
	@Override
	public void delete(Integer gbDistributerWeightGoodsId){
		gbDistributerWeightGoodsDao.delete(gbDistributerWeightGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerWeightGoodsIds){
		gbDistributerWeightGoodsDao.deleteBatch(gbDistributerWeightGoodsIds);
	}

    @Override
    public List<GbDistributerWeightGoodsEntity> queryWeightGoodsByParams(Map<String, Object> map) {

		return gbDistributerWeightGoodsDao.queryWeightGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> queryShelfGoodsToWeightByParams(Map<String, Object> map) {

		return gbDistributerWeightGoodsDao.queryShelfGoodsToWeightByParams(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryFatherGoodsToWeightByParams(Map<String, Object> map) {

		return gbDistributerWeightGoodsDao.queryFatherGoodsToWeightByParams(map);
    }

    @Override
    public List<GbDistributerWeightGoodsEntity> queryWeightGoodsWithOrderByParams(Map<String, Object> map) {

		return gbDistributerWeightGoodsDao.queryWeightGoodsWithOrderByParams(map);
    }

    @Override
    public int queryWeightGoodsAccount(Map<String, Object> map33) {

		return gbDistributerWeightGoodsDao.queryWeightGoodsAccount(map33);
    }

}
