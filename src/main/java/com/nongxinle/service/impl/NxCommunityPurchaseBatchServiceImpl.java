package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityPurchaseBatchDao;
import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;
import com.nongxinle.service.NxCommunityPurchaseBatchService;



@Service("nxCommunityPurchaseBatchService")
public class NxCommunityPurchaseBatchServiceImpl implements NxCommunityPurchaseBatchService {
	@Autowired
	private NxCommunityPurchaseBatchDao nxCommunityPurchaseBatchDao;
	
	@Override
	public NxCommunityPurchaseBatchEntity queryObject(Integer nxCommunityPurchaseBatchId){
		return nxCommunityPurchaseBatchDao.queryObject(nxCommunityPurchaseBatchId);
	}
	
	@Override
	public List<NxCommunityPurchaseBatchEntity> queryList(Map<String, Object> map){
		return nxCommunityPurchaseBatchDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityPurchaseBatchDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatch){
		nxCommunityPurchaseBatchDao.save(nxCommunityPurchaseBatch);
	}
	
	@Override
	public void update(NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatch){
		nxCommunityPurchaseBatchDao.update(nxCommunityPurchaseBatch);
	}
	
	@Override
	public void delete(Integer nxCommunityPurchaseBatchId){
		nxCommunityPurchaseBatchDao.delete(nxCommunityPurchaseBatchId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityPurchaseBatchIds){
		nxCommunityPurchaseBatchDao.deleteBatch(nxCommunityPurchaseBatchIds);
	}

    @Override
    public List<NxCommunityPurchaseBatchEntity> queryComPurchaseBatchByParams(Map<String, Object> map2) {

		return nxCommunityPurchaseBatchDao.queryComPurchaseBatchByParams(map2);
    }

    @Override
    public NxCommunityPurchaseBatchEntity queryBatchDetail(Map<String, Object> map) {

		return nxCommunityPurchaseBatchDao.queryBatchDetail(map);
    }

    @Override
    public Integer queryCommPurchaseBatchCount(Map<String, Object> map41) {

		return nxCommunityPurchaseBatchDao.queryCommPurchaseBatchCount(map41);
    }

    @Override
    public Double queryCommSupplierUnSettleSubtotal(Map<String, Object> map41) {

		return nxCommunityPurchaseBatchDao.queryCommSupplierUnSettleSubtotal(map41);
    }


}
