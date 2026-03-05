package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerBillDao;
import com.nongxinle.entity.NxDistributerBillEntity;
import com.nongxinle.service.NxDistributerBillService;



@Service("nxDistributerBillService")
public class NxDistributerBillServiceImpl implements NxDistributerBillService {
	@Autowired
	private NxDistributerBillDao nxDistributerBillDao;
	
	@Override
	public NxDistributerBillEntity queryObject(Integer nxDistributerBillId){
		return nxDistributerBillDao.queryObject(nxDistributerBillId);
	}
	
	@Override
	public List<NxDistributerBillEntity> queryList(Map<String, Object> map){
		return nxDistributerBillDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerBillDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerBillEntity nxDistributerBill){
		nxDistributerBillDao.save(nxDistributerBill);
	}
	
	@Override
	public void update(NxDistributerBillEntity nxDistributerBill){
		nxDistributerBillDao.update(nxDistributerBill);
	}
	
	@Override
	public void delete(Integer nxDistributerBillId){
		nxDistributerBillDao.delete(nxDistributerBillId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerBillIds){
		nxDistributerBillDao.deleteBatch(nxDistributerBillIds);
	}

    @Override
    public Integer queryDisPurchaseBatchCount(Map<String, Object> mapS) {


		return nxDistributerBillDao.queryDisPurchaseBatchCount(mapS);
    }

	@Override
	public Double querySupplierUnSettleSubtotal(Map<String, Object> mapS) {
		return nxDistributerBillDao.querySupplierUnSettleSubtotal(mapS);
	}

	@Override
	public Double queryPartnerMutualBillTotal(Map<String, Object> map) {
		Double total = nxDistributerBillDao.queryPartnerMutualBillTotal(map);
		return total != null ? total : 0.0;
	}

	@Override
	public Integer queryPartnerMutualBillCount(Map<String, Object> map) {
		Integer count = nxDistributerBillDao.queryPartnerMutualBillCount(map);
		return count != null ? count : 0;
	}

	@Override
	public List<NxDistributerBillEntity> queryDisBillsWithStatus(Map<String, Object> map) {
		List<NxDistributerBillEntity> list = nxDistributerBillDao.queryDisBillsWithStatus(map);
		return list != null ? list : java.util.Collections.emptyList();
	}

    @Override
    public List<NxDistributerBillEntity> queryDisBillsDetail(Map<String, Object> map) {

		return nxDistributerBillDao.queryDisBillsDetail(map);
    }

    @Override
    public Integer queryDisBillsDetailCount(Map<String, Object> map) {

		return nxDistributerBillDao.queryDisBillsDetailCount(map);
    }

}
