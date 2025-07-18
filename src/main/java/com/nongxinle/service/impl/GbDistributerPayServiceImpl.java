package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerPayDao;
import com.nongxinle.entity.GbDistributerPayEntity;
import com.nongxinle.service.GbDistributerPayService;



@Service("gbDistributerPayService")
public class GbDistributerPayServiceImpl implements GbDistributerPayService {
	@Autowired
	private GbDistributerPayDao gbDistributerPayDao;
	
	@Override
	public GbDistributerPayEntity queryObject(Integer gbDistributerPayId){
		return gbDistributerPayDao.queryObject(gbDistributerPayId);
	}
	
	@Override
	public List<GbDistributerPayEntity> queryList(Map<String, Object> map){
		return gbDistributerPayDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerPayDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerPayEntity gbDistributerPay){
		gbDistributerPayDao.save(gbDistributerPay);
	}
	
	@Override
	public void update(GbDistributerPayEntity gbDistributerPay){
		gbDistributerPayDao.update(gbDistributerPay);
	}
	
	@Override
	public void delete(Integer gbDistributerPayId){
		gbDistributerPayDao.delete(gbDistributerPayId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerPayIds){
		gbDistributerPayDao.deleteBatch(gbDistributerPayIds);
	}

    @Override
    public List<GbDistributerPayEntity> queryDisPayListByParams(Map<String, Object> mapP) {

		return gbDistributerPayDao.queryDisPayListByParams(mapP);
    }

    @Override
    public List<GbDistributerPayEntity> queryListByTradeNo(String ordersSn) {

		return gbDistributerPayDao.queryListByTradeNo(ordersSn);
    }

    @Override
    public GbDistributerPayEntity queryPayItemByPayId(Integer payId) {

		return gbDistributerPayDao.queryPayItemByPayId(payId);
    }

}
