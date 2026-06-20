package com.nongxinle.dao;

import com.nongxinle.entity.GbDepartmentBillPaymentEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentBillPaymentDao extends BaseDao<GbDepartmentBillPaymentEntity> {

    GbDepartmentBillPaymentEntity queryByOutTradeNo(String outTradeNo);

    List<GbDepartmentBillPaymentEntity> queryList(Map<String, Object> map);
}
