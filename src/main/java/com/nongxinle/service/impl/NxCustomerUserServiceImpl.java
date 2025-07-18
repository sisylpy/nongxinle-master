package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.service.NxCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerUserDao;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.service.NxCustomerUserService;



@Service("nxCustomerUserService")
public class NxCustomerUserServiceImpl implements NxCustomerUserService {
	@Autowired
	private NxCustomerUserDao nxCustomerUserDao;
	@Autowired
	private NxCustomerService nxCustomerService;
	
	@Override
	public NxCustomerUserEntity queryObject(Integer custUserId){
		return nxCustomerUserDao.queryObject(custUserId);
	}
	
	@Override
	public List<NxCustomerUserEntity> queryList(Map<String, Object> map){
		return nxCustomerUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCustomerUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCustomerUserEntity nxCustomerUser){
		nxCustomerUserDao.save(nxCustomerUser);
	}
	
	@Override
	public void update(NxCustomerUserEntity nxCustomerUser){
		nxCustomerUserDao.update(nxCustomerUser);
	}
	
	@Override
	public void delete(Integer custUserId){
		nxCustomerUserDao.delete(custUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] custUserIds){
		nxCustomerUserDao.deleteBatch(custUserIds);
	}



    @Override
    public NxCustomerUserEntity queryUserByOpenId(String openid) {

		return nxCustomerUserDao.queryUserByOpenId(openid);
    }

    @Override
    public Map<String, Object> queryCustomerUserInfo(Integer gbDepartmentUserId) {

		NxCustomerUserEntity userEntity = nxCustomerUserDao.queryUserWithAddress(gbDepartmentUserId);
		System.out.println("dkfjdkf;lkafa;lfdas===" + userEntity.getMainAddress());

		Integer nxCuCustomerId = userEntity.getNxCuCustomerId();
		System.out.println("ududud");
		NxCustomerEntity nxCustomerEntity = nxCustomerService.querycustomerDetail(nxCuCustomerId);

		Map<String, Object> map = new HashMap<>();
		map.put("userInfo",userEntity );
		map.put("customerInfo", nxCustomerEntity);

		return map;
    }

    @Override
    public List<NxCustomerUserEntity> queryCustomerByParams(Map<String, Object> map) {

		return nxCustomerUserDao.queryCustomerByParams(map);
    }

    @Override
    public Integer queryCustomerUserCount(Map<String, Object> map) {

		return  nxCustomerUserDao.queryCustomerUserCount(map);
    }

    @Override
    public Integer queryCommerceCustomerUserCount(Map<String, Object> map) {

		return nxCustomerUserDao.queryCommerceCustomerUserCount(map);
    }

	@Override
	public NxCustomerUserEntity queryUserWithAddress(Integer orderUserId) {
		return nxCustomerUserDao.queryUserWithAddress(orderUserId);
	}

    @Override
    public NxCustomerUserEntity queryUserByOpenIdAndCommerceId(Map<String, Object> mapU) {

		return nxCustomerUserDao.queryUserByOpenIdAndCommerceId(mapU);
    }

}
