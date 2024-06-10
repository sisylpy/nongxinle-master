package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityPurchaseBatchEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunitySupplierPaymentDao;
import com.nongxinle.entity.NxCommunitySupplierPaymentEntity;
import com.nongxinle.service.NxCommunitySupplierPaymentService;



@Service("nxCommunitySupplierPaymentService")
public class NxCommunitySupplierPaymentServiceImpl implements NxCommunitySupplierPaymentService {
	@Autowired
	private NxCommunitySupplierPaymentDao nxCommunitySupplierPaymentDao;
	
	@Override
	public NxCommunitySupplierPaymentEntity queryObject(Integer nxCommuntiySupplierPaymentId){
		return nxCommunitySupplierPaymentDao.queryObject(nxCommuntiySupplierPaymentId);
	}
	
	@Override
	public List<NxCommunitySupplierPaymentEntity> queryList(Map<String, Object> map){
		return nxCommunitySupplierPaymentDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunitySupplierPaymentDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunitySupplierPaymentEntity nxCommunitySupplierPayment){
		nxCommunitySupplierPaymentDao.save(nxCommunitySupplierPayment);
	}
	
	@Override
	public void update(NxCommunitySupplierPaymentEntity nxCommunitySupplierPayment){
		nxCommunitySupplierPaymentDao.update(nxCommunitySupplierPayment);
	}
	
	@Override
	public void delete(Integer nxCommuntiySupplierPaymentId){
		nxCommunitySupplierPaymentDao.delete(nxCommuntiySupplierPaymentId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommuntiySupplierPaymentIds){
		nxCommunitySupplierPaymentDao.deleteBatch(nxCommuntiySupplierPaymentIds);
	}



}
