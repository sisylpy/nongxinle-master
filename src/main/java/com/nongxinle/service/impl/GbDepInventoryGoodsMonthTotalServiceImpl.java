package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepInventoryGoodsMonthTotalDao;
import com.nongxinle.entity.GbDepInventoryGoodsMonthTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsMonthTotalService;



@Service("gbDepInventoryGoodsMonthTotalService")
public class GbDepInventoryGoodsMonthTotalServiceImpl implements GbDepInventoryGoodsMonthTotalService {
	@Autowired
	private GbDepInventoryGoodsMonthTotalDao gbDepInventoryGoodsMonthTotalDao;
	
	@Override
	public GbDepInventoryGoodsMonthTotalEntity queryObject(Integer gbInventoryGoodsMonthTotalId){
		return gbDepInventoryGoodsMonthTotalDao.queryObject(gbInventoryGoodsMonthTotalId);
	}
	
	@Override
	public List<GbDepInventoryGoodsMonthTotalEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsMonthTotalDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsMonthTotalDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsMonthTotalEntity gbDepInventoryGoodsMonthTotal){
		gbDepInventoryGoodsMonthTotalDao.save(gbDepInventoryGoodsMonthTotal);
	}
	
	@Override
	public void update(GbDepInventoryGoodsMonthTotalEntity gbDepInventoryGoodsMonthTotal){
		gbDepInventoryGoodsMonthTotalDao.update(gbDepInventoryGoodsMonthTotal);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsMonthTotalId){
		gbDepInventoryGoodsMonthTotalDao.delete(gbInventoryGoodsMonthTotalId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsMonthTotalIds){
		gbDepInventoryGoodsMonthTotalDao.deleteBatch(gbInventoryGoodsMonthTotalIds);
	}
	
}
