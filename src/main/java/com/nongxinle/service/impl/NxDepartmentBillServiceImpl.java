package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentBillDao;
import com.nongxinle.entity.NxDepartmentBillEntity;
import com.nongxinle.service.NxDepartmentBillService;



@Service("nxDepartmentBillService")
public class NxDepartmentBillServiceImpl implements NxDepartmentBillService {
	@Autowired
	private NxDepartmentBillDao nxDepartmentBillDao;
	
	@Override
	public NxDepartmentBillEntity queryObject(Integer nxDepartmentBillId){
		return nxDepartmentBillDao.queryObject(nxDepartmentBillId);
	}

	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDepartmentBillDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDepartmentBillEntity nxDepartmentBill){
		nxDepartmentBillDao.save(nxDepartmentBill);
	}
	
	@Override
	public void update(NxDepartmentBillEntity nxDepartmentBill){
		nxDepartmentBillDao.update(nxDepartmentBill);
	}
	
	@Override
	public void delete(Integer nxDepartmentBillId){
		nxDepartmentBillDao.delete(nxDepartmentBillId);
	}

    @Override
    public List<NxDepartmentBillEntity> queryBillsByParams(Map<String, Object> map) {
     return   nxDepartmentBillDao.queryBillsByParams(map);
    }

    @Override
    public NxDepartmentBillEntity querySalesBillApplys(Integer billId) {
        return nxDepartmentBillDao.querySalesBillApplys(billId);
    }

    @Override
    public int queryTotalByParams(Map<String, Object> map1) {

		return nxDepartmentBillDao.queryTotalByParams(map1);
    }

    @Override
    public Integer queryReturnNumberByBillId(Integer billId) {

		return nxDepartmentBillDao.queryReturnNumberByBillId(billId);
    }

    @Override
    public NxDepartmentBillEntity queryReturnBillOrdersByBillId(Integer billId) {

		return nxDepartmentBillDao.queryReturnBillOrdersByBillId(billId);
    }

    @Override
    public List<NxDepartmentBillEntity> queryGbDepBillsByParams(Map<String, Object> map) {

		return nxDepartmentBillDao.queryGbDepBillsByParams(map);
    }

    @Override
    public Double queryBillSubtotalByParams(Map<String, Object> map) {

		return nxDepartmentBillDao.queryBillSubtotalByParams(map);
    }

    @Override
    public Double queryBillCostSubtotalByParams(Map<String, Object> map) {

	    return nxDepartmentBillDao.queryBillCostSubtotalByParams(map);
    }

    @Override
    public List<NxDepartmentBillEntity> queryBillsListByParams(Map<String, Object> map) {

	    return nxDepartmentBillDao.queryBillsListByParams(map);
    }

    @Override
    public NxDepartmentBillEntity queryDepartBillByTradeNo(String ordersSn) {

	    return nxDepartmentBillDao.queryDepartBillByTradeNo(ordersSn);
    }


}
