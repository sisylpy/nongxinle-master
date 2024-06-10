package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepInventoryGoodsDailyDao;
import com.nongxinle.entity.GbDepInventoryGoodsDailyEntity;
import com.nongxinle.service.GbDepInventoryGoodsDailyService;



@Service("gbDepInventoryGoodsDailyService")
public class GbDepInventoryGoodsDailyServiceImpl implements GbDepInventoryGoodsDailyService {
	@Autowired
	private GbDepInventoryGoodsDailyDao gbDepInventoryGoodsDailyDao;
	
	@Override
	public GbDepInventoryGoodsDailyEntity queryObject(Integer gbInventoryGoodsDailyId){
		return gbDepInventoryGoodsDailyDao.queryObject(gbInventoryGoodsDailyId);
	}
	
	@Override
	public List<GbDepInventoryGoodsDailyEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsDailyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsDailyDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsDailyEntity gbDepInventoryGoodsDaily){
		gbDepInventoryGoodsDailyDao.save(gbDepInventoryGoodsDaily);
	}
	
	@Override
	public void update(GbDepInventoryGoodsDailyEntity gbDepInventoryGoodsDaily){
		gbDepInventoryGoodsDailyDao.update(gbDepInventoryGoodsDaily);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsDailyId){
		gbDepInventoryGoodsDailyDao.delete(gbInventoryGoodsDailyId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsDailyIds){
		gbDepInventoryGoodsDailyDao.deleteBatch(gbInventoryGoodsDailyIds);
	}

    @Override
    public List<GbDepInventoryGoodsDailyEntity> queryDailyStockByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyDao.queryDailyStockByParams(map);
    }

    @Override
    public Double queryDailyGoodsTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsTotal(map6);
    }

    @Override
    public Integer queryDailyGoodsInventoryCount(Map<String, Object> map24) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsInventoryCount(map24);
    }

    @Override
    public List<GbDepInventoryGoodsDailyEntity> queryDailyStockListByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyDao.queryDailyStockListByParams(map);
    }

    @Override
    public Double queryDailyGoodsLossTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsLossTotal(map6);
    }

    @Override
    public Double queryDailyGoodsWasteTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsWasteTotal(map6);
    }

    @Override
    public Double queryDailyGoodsReturnTotal(Map<String, Object> map6) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsReturnTotal(map6);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryTreeDailyGoodsList(Map<String, Object> map1) {

		return gbDepInventoryGoodsDailyDao.queryTreeDailyGoodsList(map1);
    }

    @Override
    public Double queryDailyGoodsProduceTotal(Map<String, Object> map0) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsProduceTotal(map0);
    }

    @Override
    public TreeSet<GbDepInventoryGoodsDailyEntity> queryTreeDailyDisGoodsList(Map<String, Object> map0) {

		return gbDepInventoryGoodsDailyDao.queryTreeDailyDisGoodsList(map0);
    }

    @Override
    public GbDepInventoryGoodsDailyEntity queryDailyStockItemByParams(Map<String, Object> map) {
        return gbDepInventoryGoodsDailyDao.queryDailyStockItemByParams(map);
    }

    @Override
    public Double queryDailyGoodsProduceWeight(Map<String, Object> map0) {

		return gbDepInventoryGoodsDailyDao.queryDailyGoodsProduceWeight(map0);
    }

    @Override
    public Double queryDailyGoodsProfit(Map<String, Object> map0) {

	    return gbDepInventoryGoodsDailyDao.queryDailyGoodsProfit(map0);
    }

}
