package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerUserGoodsDao;
import com.nongxinle.entity.NxCustomerUserGoodsEntity;
import com.nongxinle.service.NxCustomerUserGoodsService;



@Service("nxCustomerUserGoodsService")
public class NxCustomerUserGoodsServiceImpl implements NxCustomerUserGoodsService {
	@Autowired
	private NxCustomerUserGoodsDao nxCustomerUserGoodsDao;
	
	@Override
	public NxCustomerUserGoodsEntity queryObject(Integer custUGoodsId){
		return nxCustomerUserGoodsDao.queryObject(custUGoodsId);
	}
	
	@Override
	public List<NxCustomerUserGoodsEntity> queryList(Map<String, Object> map){
		return nxCustomerUserGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCustomerUserGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCustomerUserGoodsEntity nxCustomerUserGoods){
		nxCustomerUserGoodsDao.save(nxCustomerUserGoods);
	}
	
	@Override
	public void update(NxCustomerUserGoodsEntity nxCustomerUserGoods){
		nxCustomerUserGoodsDao.update(nxCustomerUserGoods);
	}
	
	@Override
	public void delete(Integer custUGoodsId){
		nxCustomerUserGoodsDao.delete(custUGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] custUGoodsIds){
		nxCustomerUserGoodsDao.deleteBatch(custUGoodsIds);
	}

    @Override
    public List<NxCustomerUserGoodsEntity> queryUserGoods(Map<String, Object> map) {
        return nxCustomerUserGoodsDao.queryUserGoods(map);
    }

}
