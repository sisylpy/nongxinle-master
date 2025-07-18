package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 01-08 12:27
 */

import com.nongxinle.entity.GbDistributerPayEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerPayDao extends BaseDao<GbDistributerPayEntity> {

    List<GbDistributerPayEntity> queryDisPayListByParams(Map<String, Object> mapP);

    List<GbDistributerPayEntity> queryListByTradeNo(String ordersSn);

    GbDistributerPayEntity queryPayItemByPayId(Integer payId);
}
