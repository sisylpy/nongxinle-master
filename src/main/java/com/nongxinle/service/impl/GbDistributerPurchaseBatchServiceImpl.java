package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.service.GbDistributerPurchaseGoodsService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.service.GbDistributerPurchaseBatchService;

import static com.nongxinle.utils.CommonUtils.generateUUID;
import static com.nongxinle.utils.DateUtils.*;


@Service("gbDistributerPurchaseBatchService")
public class GbDistributerPurchaseBatchServiceImpl implements GbDistributerPurchaseBatchService {
	@Autowired
	private com.nongxinle.dao.GbDistributerPurchaseBatchDao nxDistributerPurchaseBatchDao;

	
	@Autowired
	private GbDistributerPurchaseGoodsService gbDisPurchaseGoodsService;

	
	@Override
	public GbDistributerPurchaseBatchEntity queryObject(Integer nxDistributerPurchaseBatchId){

		return nxDistributerPurchaseBatchDao.queryObject(nxDistributerPurchaseBatchId);
	}
	
	@Override
	public List<GbDistributerPurchaseBatchEntity> queryList(Map<String, Object> map){
		return nxDistributerPurchaseBatchDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerPurchaseBatchDao.queryTotal(map);
	}


	@Override
	public void save(GbDistributerPurchaseBatchEntity nxDistributerPurchaseBatch){
		nxDistributerPurchaseBatchDao.save(nxDistributerPurchaseBatch);

	}

	
	@Override
	public void update(GbDistributerPurchaseBatchEntity nxDistributerPurchaseBatch){
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
	public List<GbDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map) {
		return nxDistributerPurchaseBatchDao.queryDisPurchaseBatch(map);
	}



    @Override
    public GbDistributerPurchaseBatchEntity queryBatchWithOrders(Integer batchId) {

		return nxDistributerPurchaseBatchDao.queryBatchWithOrders(batchId);
    }

    @Override
    public List<GbDistributerPurchaseBatchEntity> queryDepartmentPurchaseBatch(Map<String, Object> map) {

		return nxDistributerPurchaseBatchDao.queryDepartmentPurchaseBatch(map);
    }

    @Override
    public Integer queryDisPurchaseBatchCount(Map<String, Object> map2) {

		return nxDistributerPurchaseBatchDao.queryDisPurchaseBatchCount(map2);
    }

    @Override
    public Double querySupplierUnSettleSubtotal(Map<String, Object> map4) {

		return nxDistributerPurchaseBatchDao.querySupplierUnSettleSubtotal(map4);
    }

    @Override
    public List<GbDepartmentEntity> queryDistributerAccountingData(Map<String, Object> map2) {

		return nxDistributerPurchaseBatchDao.queryDistributerAccountingData(map2);
    }

    @Override
    public Double queryPurchaserCashTotal(Map<String, Object> map1) {

		return nxDistributerPurchaseBatchDao.queryPurchaserCashTotal(map1);
    }

    @Override
    public int queryReturnList(Map<String, Object> map333) {

		return nxDistributerPurchaseBatchDao.queryReturnList(map333);
    }

    @Override
    public List<GbDistributerEntity> queryGbDistributerBySellerId(String sellId) {

		return nxDistributerPurchaseBatchDao.queryGbDistributerBySellerId(sellId);
    }

    @Override
    public GbDistributerPurchaseBatchEntity queryBatchItemByParams(Map<String, Object> mapB) {

		return nxDistributerPurchaseBatchDao.queryBatchItemByParams(mapB);
    }


}
