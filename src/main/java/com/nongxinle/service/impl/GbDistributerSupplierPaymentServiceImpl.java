package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerSupplierPaymentDao;
import com.nongxinle.entity.GbDistributerSupplierPaymentEntity;
import com.nongxinle.service.GbDistributerSupplierPaymentService;



@Service("gbDistributerSupplierPaymentService")
public class GbDistributerSupplierPaymentServiceImpl implements GbDistributerSupplierPaymentService {
	@Autowired
	private GbDistributerSupplierPaymentDao gbDistributerSupplierPaymentDao;
	
	@Override
	public GbDistributerSupplierPaymentEntity queryObject(Integer gbDistributerSupplierPaymentId){
		return gbDistributerSupplierPaymentDao.queryObject(gbDistributerSupplierPaymentId);
	}
	
	@Override
	public List<GbDistributerSupplierPaymentEntity> queryList(Map<String, Object> map){
		return gbDistributerSupplierPaymentDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerSupplierPaymentDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerSupplierPaymentEntity gbDistributerSupplierPayment){
		gbDistributerSupplierPaymentDao.save(gbDistributerSupplierPayment);
	}
	
	@Override
	public void update(GbDistributerSupplierPaymentEntity gbDistributerSupplierPayment){
		gbDistributerSupplierPaymentDao.update(gbDistributerSupplierPayment);
	}
	
	@Override
	public void delete(Integer gbDistributerSupplierPaymentId){
		gbDistributerSupplierPaymentDao.delete(gbDistributerSupplierPaymentId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerSupplierPaymentIds){
		gbDistributerSupplierPaymentDao.deleteBatch(gbDistributerSupplierPaymentIds);
	}

    @Override
    public List<GbDistributerSupplierPaymentEntity> queryPaymentListByParams(Map<String, Object> map) {

		return gbDistributerSupplierPaymentDao.queryPaymentListByParams(map);
    }

    @Override
    public GbDistributerSupplierPaymentEntity queryPaymentByWxTradeNo(String ordersSn) {

		return gbDistributerSupplierPaymentDao.queryPaymentByWxTradeNo(ordersSn);
    }

}
