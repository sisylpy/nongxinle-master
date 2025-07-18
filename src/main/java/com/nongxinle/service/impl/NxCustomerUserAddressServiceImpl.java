package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerUserAddressDao;
import com.nongxinle.entity.NxCustomerUserAddressEntity;
import com.nongxinle.service.NxCustomerUserAddressService;



@Service("nxCustomerUserAddressService")
public class NxCustomerUserAddressServiceImpl implements NxCustomerUserAddressService {
	@Autowired
	private NxCustomerUserAddressDao nxCustomerUserAddressDao;
	
	@Override
	public NxCustomerUserAddressEntity queryObject(Integer nxCustomerUserAddressId){
		return nxCustomerUserAddressDao.queryObject(nxCustomerUserAddressId);
	}
	
	@Override
	public List<NxCustomerUserAddressEntity> queryList(Map<String, Object> map){
		return nxCustomerUserAddressDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCustomerUserAddressDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCustomerUserAddressEntity nxCustomerUserAddress){
		nxCustomerUserAddressDao.save(nxCustomerUserAddress);
	}
	
	@Override
	public void update(NxCustomerUserAddressEntity nxCustomerUserAddress){
		nxCustomerUserAddressDao.update(nxCustomerUserAddress);
	}
	
	@Override
	public void delete(Integer nxCustomerUserAddressId){
		nxCustomerUserAddressDao.delete(nxCustomerUserAddressId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCustomerUserAddressIds){
		nxCustomerUserAddressDao.deleteBatch(nxCustomerUserAddressIds);
	}

    @Override
    public List<NxCustomerUserAddressEntity> queryAddressByUserId(Integer userId) {

		return nxCustomerUserAddressDao.queryAddressByUserId(userId);
    }

    @Override
    public NxCustomerUserAddressEntity queryMainAddressByUserId(Integer nxCuaCustomerUserId) {

		return nxCustomerUserAddressDao.queryMainAddressByUserId(nxCuaCustomerUserId);
    }

}
