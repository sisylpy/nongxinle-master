package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepInventoryGoodsMonthDao;
import com.nongxinle.entity.GbDepInventoryGoodsMonthEntity;
import com.nongxinle.service.GbDepInventoryGoodsMonthService;



@Service("gbDepInventoryGoodsMonthService")
public class GbDepInventoryGoodsMonthServiceImpl implements GbDepInventoryGoodsMonthService {
	@Autowired
	private GbDepInventoryGoodsMonthDao gbDepInventoryGoodsMonthDao;
	
	@Override
	public GbDepInventoryGoodsMonthEntity queryObject(Integer gbInventoryGoodsMonthId){
		return gbDepInventoryGoodsMonthDao.queryObject(gbInventoryGoodsMonthId);
	}
	
	@Override
	public List<GbDepInventoryGoodsMonthEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsMonthDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsMonthDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsMonthEntity gbDepInventoryGoodsMonth){
		gbDepInventoryGoodsMonthDao.save(gbDepInventoryGoodsMonth);
	}
	
	@Override
	public void update(GbDepInventoryGoodsMonthEntity gbDepInventoryGoodsMonth){
		gbDepInventoryGoodsMonthDao.update(gbDepInventoryGoodsMonth);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsMonthId){
		gbDepInventoryGoodsMonthDao.delete(gbInventoryGoodsMonthId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsMonthIds){
		gbDepInventoryGoodsMonthDao.deleteBatch(gbInventoryGoodsMonthIds);
	}

    @Override
    public List<GbDepInventoryGoodsMonthEntity> queryMonthStockByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsMonthDao.queryMonthStockByParams(map);

    }

    @Override
    public Integer queryMonthGoodsInventoryCount(Map<String, Object> map24) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsInventoryCount(map24);
    }

    @Override
    public Double queryMonthGoodsTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsTotal(map6);

    }

    @Override
    public List<GbDepInventoryGoodsMonthEntity> queryMonthStockListByParams(Map<String, Object> map2) {

		return gbDepInventoryGoodsMonthDao.queryMonthStockListByParams(map2);
    }

    @Override
    public Double queryMonthGoodsLossTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsLossTotal(map6);
    }

    @Override
    public Double queryMonthGoodsWasteTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsWasteTotal(map6);
    }

    @Override
    public Double queryMonthGoodsReturnTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsReturnTotal(map6);
    }

    @Override
    public GbDepInventoryGoodsMonthEntity queryDepMonthStockByParams(Map<String, Object> map1) {

		return gbDepInventoryGoodsMonthDao.queryDepMonthStockByParams(map1);

    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryTreeMonthGoodsList(Map<String, Object> map2) {

		return gbDepInventoryGoodsMonthDao.queryTreeMonthGoodsList(map2);
    }

    @Override
    public Double queryMonthGoodsProduceTotal(Map<String, Object> map2) {

		return gbDepInventoryGoodsMonthDao.queryMonthGoodsProduceTotal(map2);
    }

}
