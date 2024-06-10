package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 12-13 09:47
 */

import com.nongxinle.entity.NxRestrauntBillEntity;
import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;


public interface NxRestrauntBillDao extends BaseDao<NxRestrauntBillEntity> {

    List<NxRestrauntBillEntity> queryRestrauntBillsByParams(Map<String, Object> map);

    int queryTotalRestrauntBillByParams(Map<String, Object> map);

    NxRestrauntBillEntity queryRestrauntBillApplys(Integer billId);

    NxRestrauntBillEntity queryRestrauntBillByTradeNo(String ordersSn);

    NxRestrauntBillEntity queryUnPayRestrauntBill(Map<String, Object> map);

    List<NxRestrauntBillEntity> queryResDailyBillWithOrders(Map<String, Object> map);

    List<NxRestrauntBillEntity> queryUnSignCustomer(Integer comId);

    List<NxRestrauntBillEntity> queryUnProfitBill(Integer comId);
}
