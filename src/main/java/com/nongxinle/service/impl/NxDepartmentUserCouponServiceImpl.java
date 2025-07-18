package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentUserCouponDao;
import com.nongxinle.entity.NxDepartmentUserCouponEntity;
import com.nongxinle.service.NxDepartmentUserCouponService;



@Service("nxDepartmentUserCouponService")
public class NxDepartmentUserCouponServiceImpl implements NxDepartmentUserCouponService {
	@Autowired
	private NxDepartmentUserCouponDao nxDepartmentUserCouponDao;
	
	@Override
	public NxDepartmentUserCouponEntity queryObject(Integer nxDepartmentUserCouponId){
		return nxDepartmentUserCouponDao.queryObject(nxDepartmentUserCouponId);
	}
	
	@Override
	public List<NxDepartmentUserCouponEntity> queryList(Map<String, Object> map){
		return nxDepartmentUserCouponDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDepartmentUserCouponDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDepartmentUserCouponEntity nxDepartmentUserCoupon){
		nxDepartmentUserCouponDao.save(nxDepartmentUserCoupon);
	}
	
	@Override
	public void update(NxDepartmentUserCouponEntity nxDepartmentUserCoupon){
		nxDepartmentUserCouponDao.update(nxDepartmentUserCoupon);
	}
	
	@Override
	public void delete(Integer nxDepartmentUserCouponId){
		nxDepartmentUserCouponDao.delete(nxDepartmentUserCouponId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDepartmentUserCouponIds){
		nxDepartmentUserCouponDao.deleteBatch(nxDepartmentUserCouponIds);
	}
	
}
