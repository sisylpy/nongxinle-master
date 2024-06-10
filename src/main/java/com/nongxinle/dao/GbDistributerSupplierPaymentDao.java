package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-28 13:40
 */

import com.nongxinle.entity.GbDistributerSupplierPaymentEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerSupplierPaymentDao extends BaseDao<GbDistributerSupplierPaymentEntity> {

    List<GbDistributerSupplierPaymentEntity> queryPaymentListByParams(Map<String, Object> map);

    GbDistributerSupplierPaymentEntity queryPaymentByWxTradeNo(String ordersSn);

}
