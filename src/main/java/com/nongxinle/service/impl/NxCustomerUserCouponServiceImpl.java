package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityCouponEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCustomerUserCouponDao;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.service.NxCustomerUserCouponService;



@Service("nxCustomerUserCouponService")
public class NxCustomerUserCouponServiceImpl implements NxCustomerUserCouponService {
	@Autowired
	private NxCustomerUserCouponDao nxCustomerUserCouponDao;


    @Override
    public void save(NxCustomerUserCouponEntity customerUserCouponEntity) {

        nxCustomerUserCouponDao.save(customerUserCouponEntity);

    }

    @Override
    public List<NxCustomerUserCouponEntity> queryUserCouponListByParams(Map<String, Object> map) {

        return nxCustomerUserCouponDao.queryUserCouponListByParams(map);
    }

    @Override
    public NxCustomerUserCouponEntity equalObject(Integer nxCosCucId) {
        return nxCustomerUserCouponDao.queryObject(nxCosCucId);
    }

    @Override
    public void update(NxCustomerUserCouponEntity userCouponEntity) {
        nxCustomerUserCouponDao.update(userCouponEntity);
    }

    @Override
    public NxCustomerUserCouponEntity queryUserCouponDetail(Integer id) {

        return nxCustomerUserCouponDao.queryUserCouponDetail(id);
    }

    @Override
    public void delete(Integer nxCustomerUserCouponId) {
         nxCustomerUserCouponDao.delete(nxCustomerUserCouponId);
    }

    @Override
    public int queryUserCouponCount(Map<String, Object> mapUC) {

        return nxCustomerUserCouponDao.queryUserCouponCount(mapUC);

    }


}
