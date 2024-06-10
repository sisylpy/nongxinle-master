package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerSupplierUserDao;
import com.nongxinle.entity.GbDistributerSupplierUserEntity;
import com.nongxinle.service.GbDistributerSupplierUserService;



@Service("gbDistributerSupplierUserService")
public class GbDistributerSupplierUserServiceImpl implements GbDistributerSupplierUserService {
	@Autowired
	private GbDistributerSupplierUserDao gbDistributerSupplierUserDao;
	
	@Override
	public GbDistributerSupplierUserEntity queryObject(Integer gbDistributerSupplierUserId){
		return gbDistributerSupplierUserDao.queryObject(gbDistributerSupplierUserId);
	}
	
	@Override
	public List<GbDistributerSupplierUserEntity> queryList(Map<String, Object> map){
		return gbDistributerSupplierUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerSupplierUserDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerSupplierUserEntity gbDistributerSupplierUser){
		gbDistributerSupplierUserDao.save(gbDistributerSupplierUser);
	}
	
	@Override
	public void update(GbDistributerSupplierUserEntity gbDistributerSupplierUser){
		gbDistributerSupplierUserDao.update(gbDistributerSupplierUser);
	}
	
	@Override
	public void delete(Integer gbDistributerSupplierUserId){
		gbDistributerSupplierUserDao.delete(gbDistributerSupplierUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerSupplierUserIds){
		gbDistributerSupplierUserDao.deleteBatch(gbDistributerSupplierUserIds);
	}

    @Override
    public GbDistributerSupplierUserEntity querySupplierUserByParams(Map<String, Object> map) {

		return gbDistributerSupplierUserDao.querySupplierUserByParams(map);
    }

    @Override
    public GbDistributerSupplierUserEntity querySupplierUserByOpenId(String openId) {

		return gbDistributerSupplierUserDao.querySupplierUserByOpenId(openId);
    }

    @Override
    public List<GbDistributerSupplierUserEntity> querySupplierUsersBySupplierId(Integer returnSupplierId) {

		return gbDistributerSupplierUserDao.querySupplierUsersBySupplierId(returnSupplierId);
    }

    @Override
    public GbDistributerSupplierUserEntity queryAppointUserBySupplierId(Integer supplierId) {

		return gbDistributerSupplierUserDao.queryAppointUserBySupplierId(supplierId);
    }



}
