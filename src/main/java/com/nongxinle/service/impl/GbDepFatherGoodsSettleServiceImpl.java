package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepFatherGoodsSettleDao;
import com.nongxinle.entity.GbDepFatherGoodsSettleEntity;
import com.nongxinle.service.GbDepFatherGoodsSettleService;



@Service("gbDistributerFatherGoodsSettleService")
public class GbDepFatherGoodsSettleServiceImpl implements GbDepFatherGoodsSettleService {
	@Autowired
	private GbDepFatherGoodsSettleDao gbDepFatherGoodsSettleDao;
	
	@Override
	public GbDepFatherGoodsSettleEntity queryObject(Integer gbDepFatherGoodsSettleStaticsId){
		return gbDepFatherGoodsSettleDao.queryObject(gbDepFatherGoodsSettleStaticsId);
	}
	
	@Override
	public List<GbDepFatherGoodsSettleEntity> queryList(Map<String, Object> map){
		return gbDepFatherGoodsSettleDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepFatherGoodsSettleDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepFatherGoodsSettleEntity gbDistributerFatherGoodsSettle){
		gbDepFatherGoodsSettleDao.save(gbDistributerFatherGoodsSettle);
	}
	
	@Override
	public void update(GbDepFatherGoodsSettleEntity gbDistributerFatherGoodsSettle){
		gbDepFatherGoodsSettleDao.update(gbDistributerFatherGoodsSettle);
	}
	
	@Override
	public void delete(Integer gbDepFatherGoodsSettleStaticsId){
		gbDepFatherGoodsSettleDao.delete(gbDepFatherGoodsSettleStaticsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepFatherGoodsSettleStaticsIds){
		gbDepFatherGoodsSettleDao.deleteBatch(gbDepFatherGoodsSettleStaticsIds);
	}

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryPankuFatherGoods(Map<String, Object> map) {

		return gbDepFatherGoodsSettleDao.queryPankuFatherGoods(map);
    }

    @Override
    public List<GbDepFatherGoodsSettleEntity> queryDisFatherGoodsSettleTotalByParams(Map<String, Object> mapSettleCost) {

		return gbDepFatherGoodsSettleDao.queryDisFatherGoodsSettleTotalByParams(mapSettleCost);
    }

    @Override
    public Double queryPankuFatherGoodsTypeTotal(Map<String, Object> mapSettleCost) {

		return gbDepFatherGoodsSettleDao.queryPankuFatherGoodsTypeTotal(mapSettleCost);
    }

}
