package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 10-18 12:14
 */

import com.nongxinle.entity.NxDistributerPayEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerPayDao extends BaseDao<NxDistributerPayEntity> {

    NxDistributerPayEntity queryItemByTradeNo(String ordersSn);

    List<NxDistributerPayEntity> queryDisPayListByParams(Map<String, Object> map);

    NxDistributerPayEntity queryPayItemByPayId(Integer payId);

    List<NxDistributerPayEntity> queryItemListByTradeNo(String ordersSn);

    NxDistributerPayEntity queryUnPayByParams(Map<String, Object> mapP);
}
