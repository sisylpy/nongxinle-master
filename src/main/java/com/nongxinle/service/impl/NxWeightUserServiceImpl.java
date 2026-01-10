package com.nongxinle.service.impl;

import com.nongxinle.dao.NxWeightUserDao;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxJrdhSupplierEntity;
import com.nongxinle.entity.NxJrdhUserEntity;
import com.nongxinle.entity.NxWeightUserEntity;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.NxJrdhSupplierService;
import com.nongxinle.service.NxJrdhUserService;
import com.nongxinle.service.NxWeightUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 称重员工Service实现类
 * @author lpy
 * @date 2025-01-XX
 */
@Service("nxWeightUserService")
public class NxWeightUserServiceImpl implements NxWeightUserService {

    @Autowired
    private NxWeightUserDao nxWeightUserDao;

    @Autowired
    private NxDistributerService nxDistributerService;

    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;

    @Override
    public NxWeightUserEntity queryObject(Integer nxWeightUserId) {
        return nxWeightUserDao.queryObject(nxWeightUserId);
    }

    @Override
    public List<NxWeightUserEntity> queryList(Map<String, Object> map) {
        return nxWeightUserDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxWeightUserDao.queryTotal(map);
    }

    @Override
    public void save(NxWeightUserEntity nxWeightUser) {
        // 设置默认值
        if (nxWeightUser.getNxWuStatus() == null) {
            nxWeightUser.setNxWuStatus(1); // 默认启用
        }
        if (nxWeightUser.getNxWuLoginTimes() == null) {
            nxWeightUser.setNxWuLoginTimes(0);
        }
        // 设置加入日期
        if (nxWeightUser.getNxWuJoinDate() == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            nxWeightUser.setNxWuJoinDate(sdf.format(new Date()));
        }
        nxWeightUserDao.save(nxWeightUser);
    }

    @Override
    public void update(NxWeightUserEntity nxWeightUser) {
        nxWeightUserDao.update(nxWeightUser);
    }

    @Override
    public void delete(Integer nxWeightUserId) {
        nxWeightUserDao.delete(nxWeightUserId);
    }

    @Override
    public void deleteBatch(Integer[] nxWeightUserIds) {
        nxWeightUserDao.deleteBatch(nxWeightUserIds);
    }

    @Override
    public NxWeightUserEntity queryUserByLoginPhone(String loginPhone) {
        return nxWeightUserDao.queryUserByLoginPhone(loginPhone);
    }

    @Override
    public NxWeightUserEntity queryUserByOpenId(String openId) {
        return nxWeightUserDao.queryUserByOpenId(openId);
    }

    @Override
    public List<NxWeightUserEntity> queryUsersByDistributerId(Integer distributerId) {
        return nxWeightUserDao.queryUsersByDistributerId(distributerId);
    }

    @Override
    public List<NxWeightUserEntity> queryUsersBySupplierId(Integer supplierId) {
        return nxWeightUserDao.queryUsersBySupplierId(supplierId);
    }

    @Override
    public List<NxWeightUserEntity> queryUsersByParams(Map<String, Object> map) {
        return nxWeightUserDao.queryUsersByParams(map);
    }

    @Override
    public NxWeightUserEntity queryUserByPhoneAndCode(Map<String, Object> map) {
        return nxWeightUserDao.queryUserByPhoneAndCode(map);
    }

    @Override
    public Map<String, Object> queryWeightUserAndInfo(Integer userId) {
        // 查询称重员工信息
        NxWeightUserEntity weightUserEntity = nxWeightUserDao.queryObject(userId);
        if (weightUserEntity == null) {
            return null;
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userInfo", weightUserEntity);

        // 根据用户类型查询对应的配送商或供货商信息
        Integer userType = weightUserEntity.getNxWuUserType();
        if (userType == 1) {
            // 配送商员工，查询配送商信息
            Integer distributerId = weightUserEntity.getNxWuNxDistributerId();
            if (distributerId != null) {
                NxDistributerEntity distributerEntity = nxDistributerService.queryObject(distributerId);
                resultMap.put("disInfo", distributerEntity);
                resultMap.put("userType", 1);
            }
        } else if (userType == 2) {
            // 供货商员工，查询供货商信息
            Integer jrdhUserId = weightUserEntity.getNxWuUserId();
            if (jrdhUserId != null) {
                List<NxJrdhSupplierEntity> supplierEntities =  nxJrdhSupplierService.querySupplierByUserId(jrdhUserId);
                resultMap.put("customerArr", supplierEntities);
                resultMap.put("userType", 2);
            }
        }

        return resultMap;
    }
}

