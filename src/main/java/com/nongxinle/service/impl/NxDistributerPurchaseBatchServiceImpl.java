package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDistributerPurchaseGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerPurchaseBatchDao;
import com.nongxinle.service.NxDistributerPurchaseBatchService;

@Service("nxDistributerPurchaseBatchService")
public class NxDistributerPurchaseBatchServiceImpl implements NxDistributerPurchaseBatchService {
	@Autowired
	private NxDistributerPurchaseBatchDao nxDistributerPurchaseBatchDao;



	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;

	@Autowired
	private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;

	
	@Override
	public NxDistributerPurchaseBatchEntity queryObject(Integer nxDistributerPurchaseBatchId){

		return nxDistributerPurchaseBatchDao.queryObject(nxDistributerPurchaseBatchId);
	}
	
	@Override
	public List<NxDistributerPurchaseBatchEntity> queryList(Map<String, Object> map){
		return nxDistributerPurchaseBatchDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerPurchaseBatchDao.queryTotal(map);
	}



	@Override
	public void save(NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatch){
		nxDistributerPurchaseBatchDao.save(nxDistributerPurchaseBatch);

	}

	
	@Override
	public void update(NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatch){
		nxDistributerPurchaseBatchDao.update(nxDistributerPurchaseBatch);
	}
	
	@Override
	public void delete(Integer nxDpbUuid){
		nxDistributerPurchaseBatchDao.delete(nxDpbUuid);
	}
	
	@Override
	public void deleteBatch(String[] nxDpbUuids){
		nxDistributerPurchaseBatchDao.deleteBatch(nxDpbUuids);
	}

	@Override
	public List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map) {
		return nxDistributerPurchaseBatchDao.queryDisPurchaseBatch(map);
	}



    @Override
    public NxDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId) {

		return nxDistributerPurchaseBatchDao.queryBatchWithOrders(batchId);
    }

    @Override
    public int queryDisPurchaseBatchCount(Map<String, Object> map) {

		return nxDistributerPurchaseBatchDao.queryDisPurchaseBatchCount(map);
    }

    @Override
    public Double queryDisPurchaseBatchTotal(Map<String, Object> map) {

		return nxDistributerPurchaseBatchDao.queryDisPurchaseBatchTotal(map);
    }

	@Override
	public Double queryPurchaseGoodsSubTotal(Map<String, Object> map) {
		return null;
	}


    @Override
    public Double queryPurchaserCashTotal(Map<String, Object> map1) {

		return nxDistributerPurchaseBatchDao.queryPurchaserCashTotal(map1);
    }

    @Override
    public List<NxDistributerEntity> queryNxDistributerBySellerId(String sellId) {

		return nxDistributerPurchaseBatchDao.queryNxDistributerBySellerId(sellId);
    }

    @Override
    public NxDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB) {

		return nxDistributerPurchaseBatchDao.queryBatchItemByParams(mapB);
    }


}
