package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerPayDao;
import com.nongxinle.entity.NxDistributerPayEntity;
import com.nongxinle.service.NxDistributerPayService;



@Service("nxDistributerPayService")
public class NxDistributerPayServiceImpl implements NxDistributerPayService {
	@Autowired
	private NxDistributerPayDao nxDistributerPayDao;
	
	@Override
	public NxDistributerPayEntity queryObject(Integer nxDistributerPayId){
		return nxDistributerPayDao.queryObject(nxDistributerPayId);
	}
	
	@Override
	public List<NxDistributerPayEntity> queryList(Map<String, Object> map){
		return nxDistributerPayDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerPayDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerPayEntity nxDistributerPay){
		nxDistributerPayDao.save(nxDistributerPay);
	}
	
	@Override
	public void update(NxDistributerPayEntity nxDistributerPay){
		nxDistributerPayDao.update(nxDistributerPay);
	}
	
	@Override
	public void delete(Integer nxDistributerPayId){
		nxDistributerPayDao.delete(nxDistributerPayId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerPayIds){
		nxDistributerPayDao.deleteBatch(nxDistributerPayIds);
	}

    @Override
    public NxDistributerPayEntity queryItemByTradeNo(String ordersSn) {

		return nxDistributerPayDao.queryItemByTradeNo(ordersSn);
    }

    @Override
    public List<NxDistributerPayEntity> queryDisPayListByParams(Map<String, Object> map) {

		return nxDistributerPayDao.queryDisPayListByParams(map);
    }

    @Override
    public NxDistributerPayEntity queryPayItemByPayId(Integer payId) {

		return nxDistributerPayDao.queryPayItemByPayId(payId);
    }

    @Override
    public List<NxDistributerPayEntity> queryItemListByTradeNo(String ordersSn) {

		return nxDistributerPayDao.queryItemListByTradeNo(ordersSn);
    }

    @Override
    public NxDistributerPayEntity queryUnPayByParams(Map<String, Object> mapP) {

		return nxDistributerPayDao.queryUnPayByParams(mapP);
    }

}
