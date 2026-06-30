package com.nongxinle.community.customer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerUserCardDao;
import com.nongxinle.entity.NxCustomerUserCardEntity;
import com.nongxinle.community.customer.service.NxCustomerUserCardService;



@Service("nxCustomerUserCardService")
public class NxCustomerUserCardServiceImpl implements NxCustomerUserCardService {
	@Autowired
	private NxCustomerUserCardDao nxCustomerUserCardDao;
	
	@Override
	public NxCustomerUserCardEntity queryObject(Integer nxCustomerUserCardId){
		return nxCustomerUserCardDao.queryObject(nxCustomerUserCardId);
	}
	
	@Override
	public List<NxCustomerUserCardEntity> queryList(Map<String, Object> map){
		return nxCustomerUserCardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCustomerUserCardDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCustomerUserCardEntity nxCustomerUserCard){
		nxCustomerUserCardDao.save(nxCustomerUserCard);
	}
	
	@Override
	public void update(NxCustomerUserCardEntity nxCustomerUserCard){
		nxCustomerUserCardDao.update(nxCustomerUserCard);
	}
	
	@Override
	public void delete(Integer nxCustomerUserCardId){
		nxCustomerUserCardDao.delete(nxCustomerUserCardId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCustomerUserCardIds){
		nxCustomerUserCardDao.deleteBatch(nxCustomerUserCardIds);
	}

    @Override
    public List<NxCustomerUserCardEntity> queryUserCardByParams(Map<String, Object> map) {

		return nxCustomerUserCardDao.queryUserCardByParams(map);
    }

    @Override
    public NxCustomerUserCardEntity queryUserGoodsCard(Map<String, Object> map) {

		return nxCustomerUserCardDao.queryUserGoodsCard(map);
    }

    @Override
    public int queryUserCardCount(Map<String, Object> mapCARD) {

		return nxCustomerUserCardDao.queryUserCardCount(mapCARD);
    }

}
