package com.nongxinle.service.impl;

import com.nongxinle.dao.NxRestrauntDao;
import com.nongxinle.dao.NxRestrauntOrdersDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxRestrauntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRestrauntBillDao;
import com.nongxinle.service.NxRestrauntBillService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatDayTime;


@Service("nxRestrauntBillService")
public class NxRestrauntBillServiceImpl implements NxRestrauntBillService {
	@Autowired
	private NxRestrauntBillDao nxRestrauntBillDao;
	@Autowired
	private NxRestrauntOrdersDao nxRestrauntOrdersDao;
	@Autowired
	private NxRestrauntDao nxRestrauntDao;
	
	@Override
	public NxRestrauntBillEntity queryObject(Integer nxRestrauntBillId){
		return nxRestrauntBillDao.queryObject(nxRestrauntBillId);
	}
	
	@Override
	public List<NxRestrauntBillEntity> queryList(Map<String, Object> map){
		return nxRestrauntBillDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRestrauntBillDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRestrauntBillEntity nxRestrauntBill){
		nxRestrauntBillDao.save(nxRestrauntBill);
	}
	
	@Override
	public void update(NxRestrauntBillEntity nxRestrauntBill){
		nxRestrauntBillDao.update(nxRestrauntBill);
	}
	
	@Override
	public void delete(Integer nxRestrauntBillId){
		nxRestrauntBillDao.delete(nxRestrauntBillId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRestrauntBillIds){
		nxRestrauntBillDao.deleteBatch(nxRestrauntBillIds);
	}
//
//	@Override
//	public Integer restrauntCashPay(NxRestrauntBillEntity billEntity) {
//
//		//
////		nxRestrauntBillDao.save(billEntity);
////		//
//		Integer nxRbRestrauntId = billEntity.getNxRbRestrauntId();
////		NxRestrauntEntity nxRestrauntEntity = nxRestrauntDao.queryObject(nxRbRestrauntId);
////		nxRestrauntEntity.setNxRestrauntWorkingStatus(0);
////		nxRestrauntDao.update(nxRestrauntEntity);
////
//		Integer nxRestrauntBillId = billEntity.getNxRestrauntBillId();
////		List<NxRestrauntOrdersEntity> entities = billEntity.getNxRestrauntOrdersEntities();
////		for (NxRestrauntOrdersEntity sub : entities) {
////			//子订单
////			sub.setNxRoBillId(nxRestrauntBillId);
////			sub.setNxRoStatus(5);
////			nxRestrauntOrdersDao.update(sub);
////		}
//
//		return nxRestrauntBillId ;
//	}

    @Override
    public List<NxRestrauntBillEntity> queryRestrauntBillsByParams(Map<String, Object> map) {

		return nxRestrauntBillDao.queryRestrauntBillsByParams(map);
    }

	@Override
	public int queryTotalRestrauntBillByParams(Map<String, Object> map) {

		return nxRestrauntBillDao.queryTotalRestrauntBillByParams(map);
	}

    @Override
    public NxRestrauntBillEntity queryRestrauntBillApplys(Integer billId) {

		return nxRestrauntBillDao.queryRestrauntBillApplys(billId);
    }

	@Override
	public NxRestrauntBillEntity queryRestrauntBillByTradeNo(String ordersSn) {
		return nxRestrauntBillDao.queryRestrauntBillByTradeNo(ordersSn);
	}

    @Override
    public NxRestrauntBillEntity queryUnPayRestrauntBill(Map<String, Object> map) {

		return nxRestrauntBillDao.queryUnPayRestrauntBill(map);
    }

    @Override
    public List<NxRestrauntBillEntity> queryResDailyBillWithOrders(Map<String, Object> map) {

		return nxRestrauntBillDao.queryResDailyBillWithOrders(map);
    }

//    @Override
//    public Integer queryUnSignCustomerCount(Integer comId) {
//
//		return nxRestrauntBillDao.queryUnSignCustomerCount(comId);
//    }

    @Override
    public List<NxRestrauntBillEntity> queryUnSignCustomer(Integer comId) {

		return nxRestrauntBillDao.queryUnSignCustomer(comId);
    }

    @Override
    public List<NxRestrauntBillEntity> queryUnProfitBill(Integer comId) {

		return nxRestrauntBillDao.queryUnProfitBill(comId);
    }


}
