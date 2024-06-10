package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepartmentBillDao;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.service.GbDepartmentBillService;



@Service("gbDepartmentBillService")

public class GbDepartmentBillServiceImpl implements GbDepartmentBillService {
	@Autowired
	private GbDepartmentBillDao gbDepartmentBillDao;
	
	@Override
	public GbDepartmentBillEntity queryObject(Integer gbDepartmentBillId){
		return gbDepartmentBillDao.queryObject(gbDepartmentBillId);
	}
	
	@Override
	public List<GbDepartmentBillEntity> queryList(Map<String, Object> map){
		return gbDepartmentBillDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentBillDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentBillEntity gbDepartmentBill){
		gbDepartmentBillDao.save(gbDepartmentBill);
	}
	
	@Override
	public void update(GbDepartmentBillEntity gbDepartmentBill){
		gbDepartmentBillDao.update(gbDepartmentBill);
	}
	
	@Override
	public void delete(Integer gbDepartmentBillId){
		gbDepartmentBillDao.delete(gbDepartmentBillId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentBillIds){
		gbDepartmentBillDao.deleteBatch(gbDepartmentBillIds);
	}

    @Override
    public List<GbDepartmentBillEntity> queryDepartmentBillList(Map<String, Object> map) {

		return gbDepartmentBillDao.queryDepartmentBillList(map);
    }

    @Override
    public GbDepartmentBillEntity queryGbDepartmentBillDetail(Integer billId) {

		return gbDepartmentBillDao.queryGbDepartmentBillDetail(billId);
    }

    @Override
    public List<GbDepartmentBillEntity> queryBillsByParamsGb(Map<String, Object> map) {

		return gbDepartmentBillDao.queryBillsByParamsGb(map);
    }

    @Override
    public int queryTotalByParamsGb(Map<String, Object> map1) {

		return gbDepartmentBillDao.queryTotalByParamsGb(map1);
    }

    @Override
    public Integer queryBillsCountByParamsGb(Map<String, Object> map4) {

		return gbDepartmentBillDao.queryBillsCountByParamsGb(map4);
    }

    @Override
    public Double queryGbDepBillsSubTotal(Map<String, Object> map) {

		return gbDepartmentBillDao.queryGbDepBillsSubTotal(map);
    }

    @Override
    public Double queryGbDepBillsSellingSubTotal(Map<String, Object> map12) {

		return gbDepartmentBillDao.queryGbDepBillsSellingSubTotal(map12);
    }

    @Override
    public int queryDepartmentBillCount(Map<String, Object> map3) {

		return gbDepartmentBillDao.queryDepartmentBillCount(map3);
    }

    @Override
    public List<GbDepartmentBillEntity> queryBillGoodsByParams(Map<String, Object> map) {

		return gbDepartmentBillDao.queryBillGoodsByParams(map);
    }

    @Override
    public GbDepartmentBillEntity queryDepartBillByTradeNo(String ordersSn) {

		return gbDepartmentBillDao.queryDepartBillByTradeNo(ordersSn);
    }

//    @Override
//    public List<GbDepartmentBillEntity> queryDepartBillListByTradeNo(String ordersSn) {
//
//		return gbDepartmentBillDao.queryDepartBillListByTradeNo(ordersSn);
//    }

    @Override
    public GbDepartmentBillEntity queryDepartBillByTsxTradeNo(String nxDbTradeNo) {

		return gbDepartmentBillDao.queryDepartBillByTsxTradeNo(nxDbTradeNo);
    }


}
