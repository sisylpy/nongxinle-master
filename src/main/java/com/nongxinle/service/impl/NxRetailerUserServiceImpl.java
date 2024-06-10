package com.nongxinle.service.impl;

import com.nongxinle.dao.NxRetailerDao;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerUserDao;
import com.nongxinle.service.NxRetailerUserService;



@Service("nxRetailerUserService")
public class NxRetailerUserServiceImpl implements NxRetailerUserService {
	@Autowired
	private NxRetailerUserDao nxRetailerUserDao;
	@Autowired
	private NxRetailerDao nxRetailerDao;
	
	@Override
	public NxRetailerUserEntity queryObject(Integer nxRetailerUserId){
		return nxRetailerUserDao.queryObject(nxRetailerUserId);
	}

	@Override
	public void save(NxRetailerUserEntity retailerUserEntity) {
		nxRetailerUserDao.save(retailerUserEntity);
	}

	@Override
    public NxRetailerUserEntity queryRetailerUserByOpenId(String openid) {

		return nxRetailerUserDao.queryRetailerUserByOpenId(openid);
    }

	@Override
	public Map<String, Object> queryRetailerAndUserInfo(Integer restailerUserId) {
		//订货群信息
		NxRetailerUserEntity nxRetailerUserEntity = nxRetailerUserDao.queryObject(restailerUserId);
		//用户信息
		Integer nxRetuRetailerId = nxRetailerUserEntity.getNxRetuRetailerId();
		NxRetailerEntity nxRetailerEntity = nxRetailerDao.queryRetailerInfo(nxRetuRetailerId);
		//返回
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", nxRetailerUserEntity);
		map.put("retailerInfo", nxRetailerEntity);
		return  map;
	}

    @Override
    public List<NxRetailerUserEntity> queryRetUsersById(Integer retId) {

		return nxRetailerUserDao.queryRetUsersById(retId);
    }

}
