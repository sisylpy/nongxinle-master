package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfStockDao;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;



@Service("nxDistributerGoodsShelfStockService")
public class NxDistributerGoodsShelfStockServiceImpl implements NxDistributerGoodsShelfStockService {
	@Autowired
	private NxDistributerGoodsShelfStockDao nxDistributerGoodsShelfStockDao;
	
	@Override
	public NxDistributerGoodsShelfStockEntity queryObject(Integer nxDistributerGoodsShelfStockId){
		return nxDistributerGoodsShelfStockDao.queryObject(nxDistributerGoodsShelfStockId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfStockEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfStockDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfStockDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockDao.save(nxDistributerGoodsShelfStock);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockDao.update(nxDistributerGoodsShelfStock);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfStockId){
		nxDistributerGoodsShelfStockDao.delete(nxDistributerGoodsShelfStockId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfStockIds){
		nxDistributerGoodsShelfStockDao.deleteBatch(nxDistributerGoodsShelfStockIds);
	}
	
}
