package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerPurchaseBatchDao;
import com.nongxinle.entity.NxRetailerPurchaseBatchEntity;
import com.nongxinle.service.NxRetailerPurchaseBatchService;



@Service("nxRetailerPurchaseBatchService")
public class NxRetailerPurchaseBatchServiceImpl implements NxRetailerPurchaseBatchService {
	@Autowired
	private NxRetailerPurchaseBatchDao nxRetailerPurchaseBatchDao;
	
	@Override
	public NxRetailerPurchaseBatchEntity queryObject(Integer nxRetailerPurchaseBatchId){
		return nxRetailerPurchaseBatchDao.queryObject(nxRetailerPurchaseBatchId);
	}
	
	@Override
	public List<NxRetailerPurchaseBatchEntity> queryList(Map<String, Object> map){
		return nxRetailerPurchaseBatchDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRetailerPurchaseBatchDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatch){
		nxRetailerPurchaseBatchDao.save(nxRetailerPurchaseBatch);
	}
	
	@Override
	public void update(NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatch){
		nxRetailerPurchaseBatchDao.update(nxRetailerPurchaseBatch);
	}
	
	@Override
	public void delete(Integer nxRetailerPurchaseBatchId){
		nxRetailerPurchaseBatchDao.delete(nxRetailerPurchaseBatchId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRetailerPurchaseBatchIds){
		nxRetailerPurchaseBatchDao.deleteBatch(nxRetailerPurchaseBatchIds);
	}

    @Override
    public List<NxRetailerPurchaseBatchEntity> queryRetPurBatchByParams(Map<String, Object> map3) {

		return nxRetailerPurchaseBatchDao.queryRetPurBatchByParams(map3);

    }

    @Override
    public List<NxRetailerPurchaseBatchEntity> queryRetPurBatchSizeByParams(Map<String, Object> map3) {

		return nxRetailerPurchaseBatchDao.queryRetPurBatchSizeByParams(map3);
    }

    @Override
    public NxRetailerPurchaseBatchEntity queryRetPurBatchDetail(Map<String, Object> map) {

		return nxRetailerPurchaseBatchDao.queryRetPurBatchDetail(map);
    }

}
