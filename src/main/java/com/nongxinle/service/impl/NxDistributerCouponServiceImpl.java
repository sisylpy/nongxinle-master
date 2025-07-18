package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerCouponDao;
import com.nongxinle.entity.NxDistributerCouponEntity;
import com.nongxinle.service.NxDistributerCouponService;



@Service("nxDistributerCouponService")
public class NxDistributerCouponServiceImpl implements NxDistributerCouponService {
	@Autowired
	private NxDistributerCouponDao nxDistributerCouponDao;
	
	@Override
	public NxDistributerCouponEntity queryObject(Integer nxDistributerCouponId){
		return nxDistributerCouponDao.queryObject(nxDistributerCouponId);
	}
	
	@Override
	public List<NxDistributerCouponEntity> queryList(Map<String, Object> map){
		return nxDistributerCouponDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerCouponDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerCouponEntity nxDistributerCoupon){
		nxDistributerCouponDao.save(nxDistributerCoupon);
	}
	
	@Override
	public void update(NxDistributerCouponEntity nxDistributerCoupon){
		nxDistributerCouponDao.update(nxDistributerCoupon);
	}
	
	@Override
	public void delete(Integer nxDistributerCouponId){
		nxDistributerCouponDao.delete(nxDistributerCouponId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerCouponIds){
		nxDistributerCouponDao.deleteBatch(nxDistributerCouponIds);
	}

    @Override
    public List<NxDistributerCouponEntity> queryLoadDownListByParams(Map<String, Object> map) {

		return nxDistributerCouponDao.queryLoadDownListByParams(map);
    }

}
