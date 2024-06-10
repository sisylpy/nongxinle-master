package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerPurchaseGoodsDao;
import com.nongxinle.entity.NxRetailerPurchaseGoodsEntity;
import com.nongxinle.service.NxRetailerPurchaseGoodsService;



@Service("nxRetailerPurchaseGoodsService")
public class NxRetailerPurchaseGoodsServiceImpl implements NxRetailerPurchaseGoodsService {
	@Autowired
	private NxRetailerPurchaseGoodsDao nxRetailerPurchaseGoodsDao;
	
	@Override
	public NxRetailerPurchaseGoodsEntity queryObject(Integer nxRetailerPurchaseGoodsId){
		return nxRetailerPurchaseGoodsDao.queryObject(nxRetailerPurchaseGoodsId);
	}
	
	@Override
	public List<NxRetailerPurchaseGoodsEntity> queryList(Map<String, Object> map){
		return nxRetailerPurchaseGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRetailerPurchaseGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRetailerPurchaseGoodsEntity nxRetailerPurchaseGoods){
		nxRetailerPurchaseGoodsDao.save(nxRetailerPurchaseGoods);
	}
	
	@Override
	public void update(NxRetailerPurchaseGoodsEntity nxRetailerPurchaseGoods){
		nxRetailerPurchaseGoodsDao.update(nxRetailerPurchaseGoods);
	}
	
	@Override
	public void delete(Integer nxRetailerPurchaseGoodsId){
		nxRetailerPurchaseGoodsDao.delete(nxRetailerPurchaseGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRetailerPurchaseGoodsIds){
		nxRetailerPurchaseGoodsDao.deleteBatch(nxRetailerPurchaseGoodsIds);
	}

    @Override
    public List<NxRetailerPurchaseGoodsEntity> queryRetShelfPurGoodsByParame(Map<String, Object> map) {

		return nxRetailerPurchaseGoodsDao.queryRetShelfPurGoodsByParame(map);
    }

}
