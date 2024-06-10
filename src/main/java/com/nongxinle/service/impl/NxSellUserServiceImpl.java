package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxSellUserDao;
import com.nongxinle.entity.NxSellUserEntity;
import com.nongxinle.service.NxSellUserService;



@Service("nxSellUserService")
public class NxSellUserServiceImpl implements NxSellUserService {
	@Autowired
	private NxSellUserDao nxSellUserDao;
	
	@Override
	public NxSellUserEntity queryObject(Integer nxSellUserId){
		return nxSellUserDao.queryObject(nxSellUserId);
	}
	
	@Override
	public List<NxSellUserEntity> queryList(Map<String, Object> map){
		return nxSellUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxSellUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxSellUserEntity nxSellUser){
		nxSellUserDao.save(nxSellUser);
	}
	
	@Override
	public void update(NxSellUserEntity nxSellUser){
		nxSellUserDao.update(nxSellUser);
	}
	
	@Override
	public void delete(Integer nxSellUserId){
		nxSellUserDao.delete(nxSellUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxSellUserIds){
		nxSellUserDao.deleteBatch(nxSellUserIds);
	}

    @Override
    public NxSellUserEntity querySellerUserByOpenId(String openId) {

		return nxSellUserDao.querySellerUserByOpenId(openId);
    }

    @Override
    public List<NxSellUserEntity> querySupplierUserBySupplierId(Integer supplierId) {

		return nxSellUserDao.querySupplierUserBySupplierId(supplierId);
    }

    @Override
    public NxSellUserEntity queryDisSellerUserByParmas(Map<String, Object> map) {

		return nxSellUserDao.queryDisSellerUserByParmas(map);
    }

	@Override
	public List<NxSellUserEntity> queryDisSellerUsersByParams(Integer disId) {
		return nxSellUserDao.queryDisSellerUsersByParams(disId);
	}

    @Override
    public List<NxSellUserEntity> queryAllSellUsers() {

		return nxSellUserDao.queryAllSellUsers();
    }


}
