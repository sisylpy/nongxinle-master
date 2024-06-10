package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerDao;
import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.service.NxCustomerService;



@Service("nxCustomerService")
public class NxCustomerServiceImpl implements NxCustomerService {
	@Autowired
	private NxCustomerDao nxCustomerDao;
	
	@Override
	public NxCustomerEntity queryObject(Integer customerId){
		return nxCustomerDao.queryObject(customerId);
	}
	
	@Override
	public List<NxCustomerEntity> queryList(Map<String, Object> map){
		return nxCustomerDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCustomerDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCustomerEntity nxCustomer){
		nxCustomerDao.save(nxCustomer);
	}
	
	@Override
	public void update(NxCustomerEntity nxCustomer){
		nxCustomerDao.update(nxCustomer);
	}
	
	@Override
	public void delete(Integer customerId){
		nxCustomerDao.delete(customerId);
	}
	
	@Override
	public void deleteBatch(Integer[] customerIds){
		nxCustomerDao.deleteBatch(customerIds);
	}

    @Override
    public List<NxCustomerEntity> queryCommunityCustomers(Map<String, Object> map) {

		return nxCustomerDao.queryCommunityCustomers(map);
    }

	@Override
	public int queryCustomerOfCommunityTotal(Map<String, Object> map) {
		return nxCustomerDao.queryCustomerOfCommunityTotal(map);
	}

}
