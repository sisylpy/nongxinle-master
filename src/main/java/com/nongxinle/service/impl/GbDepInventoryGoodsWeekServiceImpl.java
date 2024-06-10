package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepInventoryWeekEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepInventoryGoodsWeekDao;
import com.nongxinle.entity.GbDepInventoryGoodsWeekEntity;
import com.nongxinle.service.GbDepInventoryGoodsWeekService;



@Service("gbDepInventoryGoodsWeekService")
public class GbDepInventoryGoodsWeekServiceImpl implements GbDepInventoryGoodsWeekService {
	@Autowired
	private GbDepInventoryGoodsWeekDao gbDepInventoryGoodsWeekDao;
	
	@Override
	public GbDepInventoryGoodsWeekEntity queryObject(Integer gbInventoryGoodsWeekId){
		return gbDepInventoryGoodsWeekDao.queryObject(gbInventoryGoodsWeekId);
	}
	
	@Override
	public List<GbDepInventoryGoodsWeekEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsWeekDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsWeekDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsWeekEntity gbDepInventoryGoodsWeek){
		gbDepInventoryGoodsWeekDao.save(gbDepInventoryGoodsWeek);
	}
	
	@Override
	public void update(GbDepInventoryGoodsWeekEntity gbDepInventoryGoodsWeek){
		gbDepInventoryGoodsWeekDao.update(gbDepInventoryGoodsWeek);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsWeekId){
		gbDepInventoryGoodsWeekDao.delete(gbInventoryGoodsWeekId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsWeekIds){
		gbDepInventoryGoodsWeekDao.deleteBatch(gbInventoryGoodsWeekIds);
	}

    @Override
    public List<GbDepInventoryGoodsWeekEntity> queryWeekStockByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsWeekDao.queryWeekStockByParams(map);
    }

    @Override
    public Integer queryWeekGoodsInventoryCount(Map<String, Object> map24) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsInventoryCount(map24);
    }

    @Override
    public Double queryWeekGoodsTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsTotal(map6);
    }

    @Override
    public List<GbDepInventoryGoodsWeekEntity> queryWeekStockListByParams(Map<String, Object> map1) {

		return gbDepInventoryGoodsWeekDao.queryWeekStockListByParams(map1);
    }

    @Override
    public Double queryWeekGoodsLossTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsLossTotal(map6);
    }

    @Override
    public Double queryWeekGoodsWasteTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsWasteTotal(map6);
    }

    @Override
    public Double queryWeekGoodsReturnTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsReturnTotal(map6);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryTreeWeekGoodsList(Map<String, Object> map0) {

		return gbDepInventoryGoodsWeekDao.queryTreeWeekGoodsList(map0);
    }

    @Override
    public Double queryWeekGoodsProduceTotal(Map<String, Object> map1) {

		return gbDepInventoryGoodsWeekDao.queryWeekGoodsProduceTotal(map1);

    }

    @Override
    public TreeSet<GbDepInventoryGoodsWeekEntity> queryTreeWeekDisGoodsList(Map<String, Object> map0) {

		return gbDepInventoryGoodsWeekDao.queryTreeWeekDisGoodsList(map0);
    }


}
