package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-21 20:42
 */

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerUserEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerDao extends BaseDao<GbDistributerEntity> {


    GbDistributerEntity queryDisInfo(Integer diuDistributerId);

    List<GbDistributerUserEntity> queryGbUser(Integer disId);


    List<GbDistributerEntity> queryListAll();


    void kaitongGbDis(Integer id);

    List<GbDistributerEntity> queryGbDisCustomerBySellerOpenId(String openId);
}
