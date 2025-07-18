package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxGbDistibuterUserCouponDao;
import com.nongxinle.entity.NxGbDistibuterUserCouponEntity;
import com.nongxinle.service.NxGbDistibuterUserCouponService;



@Service("nxGbDistibuterUserCouponService")
public class NxGbDistibuterUserCouponServiceImpl implements NxGbDistibuterUserCouponService {
	@Autowired
	private NxGbDistibuterUserCouponDao nxGbDistibuterUserCouponDao;
	
	@Override
	public NxGbDistibuterUserCouponEntity queryObject(Integer nxGbDistributerUserCouponId){
		return nxGbDistibuterUserCouponDao.queryObject(nxGbDistributerUserCouponId);
	}
	
	@Override
	public List<NxGbDistibuterUserCouponEntity> queryList(Map<String, Object> map){
		return nxGbDistibuterUserCouponDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxGbDistibuterUserCouponDao.queryTotal(map);
	}
	
	@Override
	public void save(NxGbDistibuterUserCouponEntity nxGbDistibuterUserCoupon){
		nxGbDistibuterUserCouponDao.save(nxGbDistibuterUserCoupon);
	}
	
	@Override
	public void update(NxGbDistibuterUserCouponEntity nxGbDistibuterUserCoupon){
		nxGbDistibuterUserCouponDao.update(nxGbDistibuterUserCoupon);
	}
	
	@Override
	public void delete(Integer nxGbDistributerUserCouponId){
		nxGbDistibuterUserCouponDao.delete(nxGbDistributerUserCouponId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxGbDistributerUserCouponIds){
		nxGbDistibuterUserCouponDao.deleteBatch(nxGbDistributerUserCouponIds);
	}

    @Override
    public List<NxGbDistibuterUserCouponEntity> queryGbCouponListByParams(Map<String, Object> mapG) {

		return nxGbDistibuterUserCouponDao.queryGbCouponListByParams(mapG);
    }

}
