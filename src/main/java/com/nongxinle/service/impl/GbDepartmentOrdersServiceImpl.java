package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.nongxinle.dao.GbDepartmentOrdersDao;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderBuyStatusNew;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderStatusNew;


@Service("gbDepartmentOrdersService")
public class GbDepartmentOrdersServiceImpl implements GbDepartmentOrdersService {
	@Autowired
	private GbDepartmentOrdersDao gbDepartmentOrdersDao;

	@Override
	public List<GbDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map) {
		return gbDepartmentOrdersDao.queryDisOrdersByParams(map);
	}

	@Override
	public List<GbDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map) {
		return gbDepartmentOrdersDao.queryDistributerTodayDepartments(map);
	}

	
	@Override
	public void save(GbDepartmentOrdersEntity gbDepartmentOrders){
		if (gbDepartmentOrders.getGbDoStatus() == null) {
			gbDepartmentOrders.setGbDoStatus(getGbOrderStatusNew());
		}
		if (gbDepartmentOrders.getGbDoBuyStatus() == null) {
			gbDepartmentOrders.setGbDoBuyStatus(getGbOrderBuyStatusNew());
		}
		gbDepartmentOrdersDao.save(gbDepartmentOrders);
	}

	@Override
	public List<GbDistributerFatherGoodsEntity>  disGetUnPlanPurchaseApplys(Map<String, Object> map) {
		return gbDepartmentOrdersDao.disGetUnPlanPurchaseApplys(map);
	}




	@Override
	public void update(GbDepartmentOrdersEntity gbDepartmentOrders){
		gbDepartmentOrdersDao.update(gbDepartmentOrders);
	}

	@Override
	public void delete(Integer gbDepartmentOrdersId){
		gbDepartmentOrdersDao.delete(gbDepartmentOrdersId);
	}

	@Override
	public int queryTotalByParams(Map<String, Object> map) {
		return gbDepartmentOrdersDao.queryTotalByParams(map);
	}


    @Override
    public List<GbDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map) {

		return gbDepartmentOrdersDao.disQueryDisOrdersByParams(map);
    }

    @Override
    public GbDepartmentOrdersEntity queryGbOrderByNxOrderId(Integer nxDepartmentOrdersId) {

		return gbDepartmentOrdersDao.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
    }

    @Override
    public List<GbDepartmentOrdersEntity> queryMendianDisOrdersByParams(Map<String, Object> map3) {

		return gbDepartmentOrdersDao.queryMendianDisOrdersByParams(map3);
    }

    @Override
    public List<GbDepartmentOrdersEntity> queryOrdersByBillId(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryOrdersByBillId(map);
    }

    @Override
    public void justSave(GbDepartmentOrdersEntity ordersEntity) {
		gbDepartmentOrdersDao.save(ordersEntity);

    }

    @Override
    public List<GbDepartmentOrdersEntity> queryDisOrdersListByParams(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryDisOrdersListByParams(map);
    }

    @Override
    public List<GbDepartmentEntity> queryFatherDepartment(Map<String, Object> map14) {

		return gbDepartmentOrdersDao.queryFatherDepartment(map14);
    }

    @Override
    public List<NxDistributerEntity> queryGbDepNxDistributerOrder(Map<String, Object> map4) {

		return gbDepartmentOrdersDao.queryGbDepNxDistributerOrder(map4);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> queryWeightGoodsByParams(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryWeightGoodsByParams(map);
    }

    @Override
    public Integer queryGbDepartmentOrderAmount(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryGbDepartmentOrderAmount(map);
    }


    @Override
    public List<GbDistributerFatherGoodsEntity> disGetPrintedWeightApplys(Map<String, Object> map) {

		return gbDepartmentOrdersDao.disGetPrintedWeightApplys(map);
    }

	@Override
	public List<GbDistributerFatherGoodsEntity> queryFatherGoods(Map<String, Object> map1) {
		return gbDepartmentOrdersDao.queryFatherGoods(map1);


	}

    @Override
    public List<GbDistributerGoodsEntity> disGetTodayGoodsOrder(Map<String, Object> map) {

		return gbDepartmentOrdersDao.disGetTodayGoodsOrder(map);
    }

    @Override
    public Double queryGbOrdersSellingSubtotal(Map<String, Object> map2) {

		return gbDepartmentOrdersDao.queryGbOrdersSellingSubtotal(map2);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> stockGetDepApply(Map<String, Object> map3) {

		return gbDepartmentOrdersDao.stockGetDepApply(map3);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> disGetUnPlanPurchaseApplysStock(Map<String, Object> map) {

		return gbDepartmentOrdersDao.disGetUnPlanPurchaseApplysStock(map);
    }



    @Override
    public Integer queryOrdersDisGoodsAcount(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryOrdersDisGoodsAcount(map);
    }

    @Override
    public List<GbDepartmentOrdersEntity> queryPeisongOrdersByParams(Map<String, Object> map) {

		return gbDepartmentOrdersDao.queryPeisongOrdersByParams(map);
    }

    @Override
    public Double queryGbOrdersSubtotal(Map<String, Object> map3d) {

	    return gbDepartmentOrdersDao.queryGbOrdersSubtotal(map3d);
    }

    @Override
    public GbDepartmentOrdersEntity queryReturnOrderByReduceId(Integer gbDepartmentGoodsStockReduceId) {

	    return gbDepartmentOrdersDao.queryReturnOrderByReduceId(gbDepartmentGoodsStockReduceId);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryGreatGrandForJrdh(Map<String, Object> mapDep) {

	    return gbDepartmentOrdersDao.queryGreatGrandForJrdh(mapDep);
    }

    @Override
    public List<NxJrdhSupplierEntity> querySupplierByOrdersParams(Map<String, Object> mapDep) {

	    return gbDepartmentOrdersDao.querySupplierByOrdersParams(mapDep);
    }

    @Override
    public GbDepartmentOrdersEntity selectLastOrder(Map<String, Object> params) {

	    return gbDepartmentOrdersDao.selectLastOrder(params);
    }

    @Override
    public List<Integer> selectFrequentGoods(Map<String, Object> freqParams) {

	    return gbDepartmentOrdersDao.selectFrequentGoods(freqParams);
    }

    @Override
    public List<DailyUsage> selectDailyUsage(Map<String, Object> map) {

	    return gbDepartmentOrdersDao.selectDailyUsage(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map1) {

	    return gbDepartmentOrdersDao.queryGrandGoodsOrder(map1);
    }

    @Override
    public List<GbDepartmentOrdersEntity> queryDepWeightOrderSearch(Map<String, Object> map) {

	    return gbDepartmentOrdersDao.queryDepWeightOrderSearch(map);

    }

    @Override
    public double queryOrderWeightTotalByPurGoodsId(Integer gbDoPurchaseGoodsId) {

	    return gbDepartmentOrdersDao.queryOrderWeightTotalByPurGoodsId(gbDoPurchaseGoodsId);
    }

    @Override
    public double queryOrderWeightTotalByParams(Map<String, Object> map) {

	    return gbDepartmentOrdersDao.queryOrderWeightTotalByParams(map);
    }


    @Override
	public GbDepartmentOrdersEntity queryObject(Integer gbDepartmentOrdersId){
		return gbDepartmentOrdersDao.queryObject(gbDepartmentOrdersId);
	}
	
	@Override
	public List<GbDepartmentOrdersEntity> queryList(Map<String, Object> map){
		return gbDepartmentOrdersDao.queryList(map);
	}





    @Override
    public List<GbDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1) {

		return gbDepartmentOrdersDao.queryOrdersForDisGoods(map1);
    }


}
