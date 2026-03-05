package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentOrderHistoryDao;
import com.nongxinle.service.NxDepartmentOrderHistoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("nxDepartmentOrderHistoryService")
public class NxDepartmentOrderHistoryServiceImpl implements NxDepartmentOrderHistoryService {
	@Autowired
	private NxDepartmentOrderHistoryDao nxDepartmentOrderHistoryDao;
	@Autowired
	private NxDepartmentOrdersDao nxDepartmentOrdersDao;
	
	@Override
	public NxDepartmentOrderHistoryEntity queryObject(Integer nxDepartmentOrdersId){
		return nxDepartmentOrderHistoryDao.queryObject(nxDepartmentOrdersId);
	}
	
	@Override
	public List<NxDepartmentOrderHistoryEntity> queryList(Map<String, Object> map){
		return nxDepartmentOrderHistoryDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDepartmentOrderHistoryDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDepartmentOrderHistoryEntity nxDepartmentOrderHistory){
		nxDepartmentOrderHistoryDao.save(nxDepartmentOrderHistory);
	}
	
	@Override
	public void update(NxDepartmentOrderHistoryEntity nxDepartmentOrderHistory){
		nxDepartmentOrderHistoryDao.update(nxDepartmentOrderHistory);
	}
	
	@Override
	public void delete(Integer nxDepartmentOrdersId){
		nxDepartmentOrderHistoryDao.delete(nxDepartmentOrdersId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDepartmentOrdersIds){
		nxDepartmentOrderHistoryDao.deleteBatch(nxDepartmentOrdersIds);
	}

    @Override
    public List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParams(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryDisHistoryOrdersByParams(map);
    }

	@Transactional
    @Override
    public void moveOrderFromHistory(NxDepartmentOrderHistoryEntity order) {
		Integer orderId = order.getNxDepartmentOrdersId();
		System.out.println("订单开始：orderId = " + orderId);

		// 1. 防止重复迁移（先检查历史表中是否已有此订单）
		NxDepartmentOrdersEntity existing = nxDepartmentOrdersDao.queryObject(orderId);
		if (existing != null) {
			System.out.println("订单已存在于历史表中，跳过迁移：orderId = " + orderId);
			return;
		}

		// 2. 插入到历史表
		int insertCount = nxDepartmentOrdersDao.insertToOrder(orderId);
		System.out.println("indcouentnttt" + insertCount);
		if (insertCount == 0) {
			throw new RuntimeException("插入历史表失败，orderId = " + orderId);
		}

		// 3. 删除原始表
		int deleteCount = nxDepartmentOrderHistoryDao.delete(orderId);
		System.out.println("deleteCountdeleteCount" + deleteCount);
		if (deleteCount == 0) {
			throw new RuntimeException("删除原始订单失败，orderId = " + orderId);
		}

		System.out.println("✅ 订单迁移成功，orderId = " + orderId);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryGrandGoodsOrder(map);
    }

	@Override
	public Double queryDepOrdersSubtotal(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryDepOrdersSubtotal(map);
	}

	@Override
	public Integer queryDepOrdersAcount(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryDepOrdersAcount(map);
	}

    @Override
    public double queryDisGoodsOrderWeightTotal(Map<String, Object> mapFather) {


		return nxDepartmentOrderHistoryDao.queryDisGoodsOrderWeightTotal(mapFather);
    }

    @Override
    public List<Integer> queryGoodsIds(Map<String, Object> mapToday) {

		return nxDepartmentOrderHistoryDao.queryGoodsIds(mapToday);
    }

    @Override
    public NxDepartmentOrderHistoryEntity selectLastOrder(Map<String, Object> lastParams) {

		return nxDepartmentOrderHistoryDao.selectLastOrder(lastParams);
    }

    @Override
    public List<DailyUsage> selectDailyUsage(Map<String, Object> usageParams) {

		return nxDepartmentOrderHistoryDao.selectDailyUsage(usageParams);
    }

    @Override
    public List<Integer> selectFrequentGoods(Map<String, Object> freqParams) {

		return nxDepartmentOrderHistoryDao.selectFrequentGoods(freqParams);
    }

    @Override
    public List<NxDepartmentOrderHistoryEntity> queryDisHistoryOrdersByParamsForAi(Map<String, Object> orderHistoryParams) {

		return nxDepartmentOrderHistoryDao.queryDisHistoryOrdersByParamsForAi(orderHistoryParams);
    }

    @Override
    public List<Integer> selectFrequentGoodsWithPage(Map<String, Object> freqParams) {
        return nxDepartmentOrderHistoryDao.selectFrequentGoodsWithPage(freqParams);
    }

	@Override
	public int selectFrequentGoodsCount(Map<String, Object> freqParams) {

		return nxDepartmentOrderHistoryDao.selectFrequentGoodsCount(freqParams);

	}

    @Override
    public List<Map<String, Object>> queryDepGoodsHistoryPrice(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryDepGoodsHistoryPrice(map);

	}

    @Override
    public Integer queryReturnOrderCount(Map<String, Object> mapR) {
		return nxDepartmentOrderHistoryDao.queryReturnOrderCount(mapR);
    }

	@Override
	public double queryReturnSubtotal(Map<String, Object> mapR) {

		return nxDepartmentOrderHistoryDao.queryReturnSubtotal(mapR);
	}

	@Override
	public List<NxDepartmentOrderHistoryEntity> queryOrdersByBillIdWithTraceReport(Map<String, Object> map) {
		return nxDepartmentOrderHistoryDao.queryOrdersByBillIdWithTraceReport(map);
	}

    @Override
    public List<NxDistributerGoodsEntity> queryOfferOrdersGoods(Map<String, Object> map) {

		return nxDepartmentOrderHistoryDao.queryOfferOrdersGoods(map);
    }

}
