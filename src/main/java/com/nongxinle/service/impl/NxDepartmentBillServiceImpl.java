package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    @Override
    public NxDepartmentBillEntity queryDepartBillByJustTradeNo(String gbDbTradeNo) {

        return nxDepartmentBillDao.queryDepartBillByJustTradeNo(gbDbTradeNo);
    }

    @Override
    public int queryBillsCount(Map<String, Object> mapB) {

	    return nxDepartmentBillDao.queryBillsCount(mapB);
    }

    @Override
    public double queryReturnSubtotalByBillId(Integer billId) {

	    return nxDepartmentBillDao.queryReturnSubtotalByBillId(billId);
    }

    @Override
    public List<NxDepartmentBillEntity> queryBindMap(Map<String, Object> map) {

	    return nxDepartmentBillDao.queryBindMap(map);

    }

    @Override
    public double querySubtoalBindMap(Map<String, Object> map1) {
        return  nxDepartmentBillDao.querySubtoalBindMap(map1);
    }

    @Override
    public int queryCountBindMap(Map<String, Object> params) {

	    return nxDepartmentBillDao.queryCountBindMap(params);
    }

    @Override
    public List<NxDepartmentBillEntity> queryReturnBill(Map<String, Object> map) {

	    return nxDepartmentBillDao.queryReturnBill(map);
    }

    @Override
    public NxDepartmentBillEntity queryItemByGbDepBillId(Integer gbDepartmentBillId) {

	    return nxDepartmentBillDao.queryItemByGbDepBillId(gbDepartmentBillId);
    }


    @Override
    public Map<String, Object> getBillStats(Integer disId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 获取未支付账单统计
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillDao.queryBillsCount(mapB);
        stats.put("unPayCount", unPayCount);

        // 2. 获取退货账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", -1);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillDao.queryReturnBill(map);
        stats.put("returnList", billEntityList);

        // 3. 获取国标账单统计
        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("disId", disId);
        mapGb.put("gbDepFatherIdNotEqual", -1);
        mapGb.put("status", 3);
        int unPayGbBills = nxDepartmentBillDao.queryBillsCount(mapGb);
        stats.put("unPayGbBills", unPayGbBills);

        return stats;
    }


}
