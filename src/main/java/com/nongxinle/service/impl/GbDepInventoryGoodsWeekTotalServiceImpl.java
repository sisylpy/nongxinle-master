package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepInventoryGoodsWeekTotalDao;
import com.nongxinle.entity.GbDepInventoryGoodsWeekTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsWeekTotalService;



@Service("gbDepInventoryGoodsWeekTotalService")
public class GbDepInventoryGoodsWeekTotalServiceImpl implements GbDepInventoryGoodsWeekTotalService {
	@Autowired
	private GbDepInventoryGoodsWeekTotalDao gbDepInventoryGoodsWeekTotalDao;
	
	@Override
	public GbDepInventoryGoodsWeekTotalEntity queryObject(Integer gbInventoryGoodsWeekTotalId){
		return gbDepInventoryGoodsWeekTotalDao.queryObject(gbInventoryGoodsWeekTotalId);
	}
	
	@Override
	public List<GbDepInventoryGoodsWeekTotalEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsWeekTotalDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsWeekTotalDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal){
		gbDepInventoryGoodsWeekTotalDao.save(gbDepInventoryGoodsWeekTotal);
	}
	
	@Override
	public void update(GbDepInventoryGoodsWeekTotalEntity gbDepInventoryGoodsWeekTotal){
		gbDepInventoryGoodsWeekTotalDao.update(gbDepInventoryGoodsWeekTotal);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsWeekTotalId){
		gbDepInventoryGoodsWeekTotalDao.delete(gbInventoryGoodsWeekTotalId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsWeekTotalIds){
		gbDepInventoryGoodsWeekTotalDao.deleteBatch(gbInventoryGoodsWeekTotalIds);
	}
	
}
