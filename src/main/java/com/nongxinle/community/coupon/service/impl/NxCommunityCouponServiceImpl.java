package com.nongxinle.community.coupon.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityCouponDao;
import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;



@Service("nxCommunityCouponService")
public class NxCommunityCouponServiceImpl implements NxCommunityCouponService {
	@Autowired
	private NxCommunityCouponDao nxCommunityCouponDao;


    @Override
    public void save(NxCommunityCouponEntity nxCommunityCouponEntity) {
        nxCommunityCouponDao.save(nxCommunityCouponEntity);
    }

    @Override
    public List<NxCommunityCouponEntity> queryCouponListByParams(Map<String, Object> map) {

        return nxCommunityCouponDao.queryCouponListByParams(map);
    }

    @Override
    public NxCommunityCouponEntity queryObject(Integer id) {

        return nxCommunityCouponDao.queryObject(id);
    }

    @Override
    public void update(NxCommunityCouponEntity communityCouponEntity) {

        nxCommunityCouponDao.update(communityCouponEntity);
    }

    @Override
    public void delte(Integer id) {
        nxCommunityCouponDao.delete(id);
    }

    @Override
    public List<NxCommunityCouponEntity> queryCustomerShowCoupon(Map<String, Object> map) {

        return nxCommunityCouponDao.queryCustomerShowCoupon(map);
    }

    @Override
    public NxCommunityCouponEntity queryCouponDetail(Map<String, Object> map) {

        return nxCommunityCouponDao.queryCouponDetail(map);
    }
}
