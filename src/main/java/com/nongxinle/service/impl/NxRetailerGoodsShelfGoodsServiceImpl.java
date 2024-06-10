package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerGoodsShelfGoodsDao;
import com.nongxinle.entity.NxRetailerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxRetailerGoodsShelfGoodsService;



@Service("nxRetailerGoodsShelfGoodsService")
public class NxRetailerGoodsShelfGoodsServiceImpl implements NxRetailerGoodsShelfGoodsService {
	@Autowired
	private NxRetailerGoodsShelfGoodsDao nxRetailerGoodsShelfGoodsDao;
	
	@Override
	public NxRetailerGoodsShelfGoodsEntity queryObject(Integer nxRetailerGoodsShelfGoodsId){
		return nxRetailerGoodsShelfGoodsDao.queryObject(nxRetailerGoodsShelfGoodsId);
	}
	
	@Override
	public List<NxRetailerGoodsShelfGoodsEntity> queryList(Map<String, Object> map){
		return nxRetailerGoodsShelfGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRetailerGoodsShelfGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRetailerGoodsShelfGoodsEntity nxRetailerGoodsShelfGoods){
		nxRetailerGoodsShelfGoodsDao.save(nxRetailerGoodsShelfGoods);
	}
	
	@Override
	public void update(NxRetailerGoodsShelfGoodsEntity nxRetailerGoodsShelfGoods){
		nxRetailerGoodsShelfGoodsDao.update(nxRetailerGoodsShelfGoods);
	}
	
	@Override
	public void delete(Integer nxRetailerGoodsShelfGoodsId){
		nxRetailerGoodsShelfGoodsDao.delete(nxRetailerGoodsShelfGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRetailerGoodsShelfGoodsIds){
		nxRetailerGoodsShelfGoodsDao.deleteBatch(nxRetailerGoodsShelfGoodsIds);
	}

    @Override
    public List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsByParams(Map<String, Object> map) {

		return nxRetailerGoodsShelfGoodsDao.queryRetShelfGoodsByParams(map);
    }

    @Override
    public List<NxRetailerGoodsShelfGoodsEntity> queryRetShelfGoodsWithPurchaseByParams(Map<String, Object> map) {
        return nxRetailerGoodsShelfGoodsDao.queryRetShelfGoodsWithPurchaseByParams(map);
    }

}
