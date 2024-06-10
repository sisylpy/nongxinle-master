package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentGoodsDailyDao;
import com.nongxinle.service.GbDepartmentGoodsDailyService;



@Service("gbDepartmentGoodsDailyService")
public class GbDepartmentGoodsDailyServiceImpl implements GbDepartmentGoodsDailyService {
	@Autowired
	private GbDepartmentGoodsDailyDao gbDepartmentGoodsDailyDao;
	
	@Override
	public GbDepartmentGoodsDailyEntity queryObject(Integer gbDepartmentGoodsDailyId){
		return gbDepartmentGoodsDailyDao.queryObject(gbDepartmentGoodsDailyId);
	}
	
	@Override
	public List<GbDepartmentGoodsDailyEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsDailyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsDailyDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsDailyEntity gbDepartmentGoodsDaily){
		gbDepartmentGoodsDailyDao.save(gbDepartmentGoodsDaily);
	}
	
	@Override
	public void update(GbDepartmentGoodsDailyEntity gbDepartmentGoodsDaily){
		gbDepartmentGoodsDailyDao.update(gbDepartmentGoodsDaily);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsDailyId){
		gbDepartmentGoodsDailyDao.delete(gbDepartmentGoodsDailyId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsDailyIds){
		gbDepartmentGoodsDailyDao.deleteBatch(gbDepartmentGoodsDailyIds);
	}

    @Override
    public GbDepartmentGoodsDailyEntity queryDepGoodsDailyItem(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyItem(map);
    }

    @Override
    public Integer queryDepGoodsDailyCount(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyCount(map);
    }

	@Override
	public List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListByParams(Map<String, Object> map) {
		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyListByParams(map);
	}

	@Override
	public Double queryDepGoodsDailyProfitSubtotal(Map<String, Object> map1) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyProfitSubtotal(map1);
	}

	@Override
	public Double queryDepGoodsDailySalesSubtotal(Map<String, Object> map1222) {
		return gbDepartmentGoodsDailyDao.queryDepGoodsDailySalesSubtotal(map1222);
	}

	@Override
	public Double queryDepGoodsDailySalesProfitSubtotal(Map<String, Object> map1222) {
		return gbDepartmentGoodsDailyDao.queryDepGoodsDailySalesProfitSubtotal(map1222);
	}

	@Override
	public List<GbDistributerFatherGoodsEntity> queryDepDailyGoodsFatherTypeByParams(Map<String, Object> map1222) {

		return gbDepartmentGoodsDailyDao.queryDepDailyGoodsFatherTypeByParams(map1222);
	}

    @Override
    public TreeSet<GbDistributerGoodsEntity> queryDisGoodsTreesetByParams(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDisGoodsTreesetByParams(map);
    }

    @Override
    public Double queryDepGoodsDailyLossSubtotal(Map<String, Object> map0) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyLossSubtotal(map0);
    }

	@Override
	public Double queryDepGoodsDailyWasteSubtotal(Map<String, Object> map0) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyWasteSubtotal(map0);
	}

	@Override
	public TreeSet<GbDepartmentEntity> queryWhichDepsHasProduceDepGoodsDaily(Map<String, Object> mapDep) {

		return gbDepartmentGoodsDailyDao.queryWhichDepsHasProduceDepGoodsDaily(mapDep);
	}

    @Override
    public Double queryDepGoodsDailyProduceWeight(Map<String, Object> map0) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyProduceWeight(map0);
    }

	@Override
	public Double queryDepGoodsDailyLossWeight(Map<String, Object> map0) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyLossWeight(map0);
	}

	@Override
	public Double queryDepGoodsDailyWasteWeight(Map<String, Object> map0) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyWasteWeight(map0);
	}

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryFreshFatherGoods(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryFreshFatherGoods(map);
    }

    @Override
    public Double queryDepGoodsDailyRestWeight(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyRestWeight(map);
    }

    @Override
    public TreeSet<GbDepartmentDisGoodsEntity> queryDepDisGoodsTreeByParams(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepDisGoodsTreeByParams(map);
    }

    @Override
    public Double queryDepGoodsDailyWeight(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyWeight(map);
    }

    @Override
    public Double queryDepGoodsDailyLastWeight(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyLastWeight(map);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryClearFatherGoods(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryClearFatherGoods(map);
    }

    @Override
    public int queryDepGoodsDailyClearHour(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyClearHour(map);
    }

    @Override
    public int queryDepGoodsDailyClearMinute(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyClearMinute(map);
    }

    @Override
    public Double queryDepGoodsDailyProduceSubtotal(Map<String, Object> map11) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyProduceSubtotal(map11);
    }

    @Override
    public double queryDepGoodsFreshRate(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsFreshRate(map);
    }

    @Override
    public Double queryDepGoodsDailySubtotal(Map<String, Object> disGoodsMap) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailySubtotal(disGoodsMap);
    }

    @Override
    public double queryDepGoodsDailyReturnSubtotal(Map<String, Object> disGoodsMap) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyReturnSubtotal(disGoodsMap);
    }

    @Override
    public double queryDepGoodsDailyHighestFreshRate(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyHighestFreshRate(map);
    }

	@Override
	public double queryDepGoodsDailyLowestFreshRate(Map<String, Object> map) {


		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyLowestFreshRate(map);
	}

    @Override
    public Double queryDepGoodsDailyReturnWeight(Map<String, Object> disGoodsMap) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyReturnWeight(disGoodsMap);
    }

    @Override
    public TreeSet<GbDepartmentEntity> queryWhichDepsHasProduceDepGoodsDailyNew(Map<String, Object> map) {

		return gbDepartmentGoodsDailyDao.queryWhichDepsHasProduceDepGoodsDailyNew(map);
    }

    @Override
    public List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListWithGoodsByParams(Map<String, Object> mapSearch) {
        return gbDepartmentGoodsDailyDao.queryDepGoodsDailyListWithGoodsByParams(mapSearch);
    }

    @Override
    public List<GbDepartmentGoodsDailyEntity> queryDepGoodsDailyListWithReduceByParams(Map<String, Object> mapSearch) {

		return gbDepartmentGoodsDailyDao.queryDepGoodsDailyListWithReduceByParams(mapSearch);
    }


}
