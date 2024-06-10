package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
//import com.nongxinle.dao.NxDepartmentGoodsDao;
import com.nongxinle.dao.NxDepartmentUserDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.HttpUtils;
import com.sun.source.util.Trees;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.nongxinle.dao.NxDepartmentOrdersDao;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusNew;


@Service("nxDepartmentOrdersService")
public class NxDepartmentOrdersServiceImpl implements NxDepartmentOrdersService {
	@Autowired
	private NxDepartmentOrdersDao nxDepartmentOrdersDao;
	@Autowired
	private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
	@Autowired
	private NxDistributerGoodsService dgService;


	@Override
	public List<NxDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDisOrdersByParams(map);
	}

	@Override
	public List<NxDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map) {
		return nxDepartmentOrdersDao.queryDistributerTodayDepartments(map);
	}

	
	@Override
	public void save(NxDepartmentOrdersEntity nxDepartmentOrders){
		//如果不是连锁采购发送订单
//		nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
//		nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
//		nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
//		nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//		nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
		nxDepartmentOrdersDao.save(nxDepartmentOrders);

	}

	@Override
	public List<NxDistributerFatherGoodsEntity>  disGetUnPlanPurchaseApplys(Map<String, Object> map) {
		return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplys(map);
	}



//	///////////





	@Override
	public void update(NxDepartmentOrdersEntity nxDepartmentOrders){
		nxDepartmentOrdersDao.update(nxDepartmentOrders);
	}

	@Override
	public void delete(Integer nxDepartmentOrdersId){
		nxDepartmentOrdersDao.delete(nxDepartmentOrdersId);
	}

	@Override
	public int queryTotalByParams(Map<String, Object> map) {
		return nxDepartmentOrdersDao.queryTotalByParams(map);
	}



    @Override
    public List<NxDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map) {

		return nxDepartmentOrdersDao.disQueryDisOrdersByParams(map);
    }



    @Override
    public List<NxDepartmentOrdersEntity> queryReturnOrdersByBillId(Integer billId) {

		return nxDepartmentOrdersDao.queryReturnOrdersByBillId(billId);
    }

	@Override
	public void saveGbOrders(NxDepartmentOrdersEntity ordersEntity) {

		//判断是否是部门商品
		Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
		Integer doGbDepartmentId = ordersEntity.getNxDoGbDepartmentId();
		//查询部门还是订货群是否添加过此商品
		Map<String, Object> map = new HashMap<>();
		map.put("gbDepId", doGbDepartmentId);
		map.put("disGoodsId", doDisGoodsId);
		List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
		if(disGoodsEntities.size() == 0 ){
			//添加部门商品
			NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(doDisGoodsId);
			String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
			//
			NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
			disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
			disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
			disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
			disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
			disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
			disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
			disGoodsEntity.setNxDdgGbDepartmentId(ordersEntity.getNxDoGbDepartmentId());
			disGoodsEntity.setNxDdgGbDepartmentFatherId(ordersEntity.getNxDoGbDepartmentFatherId());
			disGoodsEntity.setNxDdgIsGbDepartment(1);
			disGoodsEntity.setNxDdgOrderQuantity(ordersEntity.getNxDoQuantity());
			disGoodsEntity.setNxDdgOrderStandard(ordersEntity.getNxDoStandard());
			disGoodsEntity.setNxDdgOrderRemark(ordersEntity.getNxDoRemark());
			disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
			nxDepartmentDisGoodsService.save(disGoodsEntity);
			Integer nxDepartmentDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();
			ordersEntity.setNxDoDepDisGoodsId(nxDepartmentDisGoodsId);
		}else {
			Integer nxDepartmentDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
			ordersEntity.setNxDoDepDisGoodsId(nxDepartmentDisGoodsId);
			//
			NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(nxDepartmentDisGoodsId);
			disGoodsEntity.setNxDdgOrderQuantity(ordersEntity.getNxDoQuantity());
			disGoodsEntity.setNxDdgOrderStandard(ordersEntity.getNxDoStandard());
			disGoodsEntity.setNxDdgOrderRemark(ordersEntity.getNxDoRemark());
			disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
			nxDepartmentDisGoodsService.update(disGoodsEntity);
		}
		ordersEntity.setNxDoStatus(getNxOrderStatusNew());
		nxDepartmentOrdersDao.save(ordersEntity);

	}

    @Override
    public List<NxDepartmentEntity> queryOrderDepartmentList(Map<String, Object> map1) {
		return nxDepartmentOrdersDao.queryOrderDepartmentList(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDepOrdersOrderFatherGoods(Map<String, Object> map) {
		return nxDepartmentOrdersDao.queryDepOrdersOrderFatherGoods(map);
    }

    @Override
    public Integer queryDepOrdersAcount(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDepOrdersAcount(map);
    }

    @Override
    public Double queryDepOrdersSubtotal(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDepOrdersSubtotal(map);
    }



    @Override
    public Double queryDepOrdersProfitSubtotal(Map<String, Object> map2) {

		return nxDepartmentOrdersDao.queryDepOrdersProfitSubtotal(map2);

    }

	@Override
	public List<NxRestrauntEntity> queryOrderNxRestrauntList(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryOrderNxRestrauntList(map1);

	}

    @Override
    public List<GbDepartmentEntity> queryOrderGbDepartmentList(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryOrderGbDepartmentList(map1);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrder(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDepWeightOrder(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryNotWeightDisOrdersByParams(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryNotWeightDisOrdersByParams(map1);
    }

    @Override
    public List<GbDistributerEntity> queryOrderGbDistributerList(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryOrderGbDistributerList(map1);
    }

	@Override
	public void saveForGb(NxDepartmentOrdersEntity ordersEntity) {
		 nxDepartmentOrdersDao.save(ordersEntity);
	}


    @Override
    public List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysSearch(Map<String, Object> map) {

		return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplysSearch(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrderSearch(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDepWeightOrderSearch(map);
    }

    @Override
    public int disGetPurchaseGoodsApplysCount(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysNew(Map<String, Object> map) {

		return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplysNew(map);
    }

//    @Override
//    public List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApply(Map<String, Object> map) {
//
//		return nxDepartmentOrdersDao.disGetOutStockGoodsApply(map);
//    }

    @Override
    public NxDepartmentOrdersEntity queryObjectNew(Integer nxDepartmentOrdersId) {

		return nxDepartmentOrdersDao.queryObjectNew(nxDepartmentOrdersId);
    }

	@Override
	public List<NxDistributerFatherGoodsEntity> queryGreatGrandOrderFatherGoods(Map<String, Object> map) {
		return nxDepartmentOrdersDao.queryGreatGrandOrderFatherGoods(map);
	}


	@Override
    public List<NxDistributerFatherGoodsEntity> queryDisGoodsForTodayOrders(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDisGoodsForTodayOrders(map);
    }

    @Override
    public List<NxDepartmentEntity> queryPureOrderNxDepartment(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryPureOrderNxDepartment(map);
    }

    @Override
    public List<GbDepartmentEntity> queryPureOrderGbDepartment(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryPureOrderGbDepartment(map);
    }

    @Override
    public double queryDisGoodsOrderWeightTotal(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryDisGoodsOrderWeightTotal(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApplyForStock(Map<String, Object> map) {

		return nxDepartmentOrdersDao.disGetOutStockGoodsApplyForStock(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryPrintDepOrder(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryPrintDepOrder(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryGrandGoodsOrder(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDisGetPrintOrderGreatGrandGoods(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDisGetPrintOrderGreatGrandGoods(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrderGb(Map<String, Object> map) {

		return nxDepartmentOrdersDao.queryDepWeightOrderGb(map);
    }



    @Override
    public List<NxDistributerFatherGoodsEntity> queryFatherGoodsByParams(Map<String, Object> map1222) {

		return nxDepartmentOrdersDao.queryFatherGoodsByParams(map1222);
    }

    @Override
    public double queryDepOrdersProfitScale(Map<String, Object> map1222) {

		return nxDepartmentOrdersDao.queryDepOrdersProfitScale(map1222);
    }

    @Override
    public double queryCostSubtotal(Map<String, Object> map1222) {

		return nxDepartmentOrdersDao.queryCostSubtotal(map1222);
    }

    @Override
    public List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map2) {

		return nxDepartmentOrdersDao.queryDisPurchaseBatch(map2);
    }


    @Override
	public NxDepartmentOrdersEntity queryObject(Integer nxDepartmentOrdersId){
		return nxDepartmentOrdersDao.queryObject(nxDepartmentOrdersId);
	}
	
	@Override
	public List<NxDepartmentOrdersEntity> queryList(Map<String, Object> map){
		return nxDepartmentOrdersDao.queryList(map);
	}
	


	private String getToken(){
		String url = "https://api.weixin.qq.com/cgi-bin/token?appid=wxbc686226ccc443f1&secret=cefb0c474497e74879687862b0d8733e&grant_type=client_credential";
		String result = HttpUtils.get(url);
		Map<String,Object> map = JSON.parseObject(result);
		String access_token = map.get("access_token").toString();
		return access_token;
	}


    @Override
    public List<NxDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1) {

		return nxDepartmentOrdersDao.queryOrdersForDisGoods(map1);
    }


}
