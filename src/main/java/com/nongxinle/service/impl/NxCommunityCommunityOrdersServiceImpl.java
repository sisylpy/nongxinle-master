package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
import com.nongxinle.dao.*;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.service.NxCommunityOrdersService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatDayTime;


@Service("nxOrdersService")
public class NxCommunityCommunityOrdersServiceImpl implements NxCommunityOrdersService {
	@Autowired
	private NxCommunityOrdersDao nxCommunityOrdersDao;

	@Autowired
	private NxCommunityOrdersSubDao nxCommunityOrdersSubDao;


	@Autowired
	private NxCustomerUserGoodsDao nxCustomerUserGoodsDao;



	@Override
	public NxCommunityOrdersEntity queryObject(Integer nxOrdersId){
		return nxCommunityOrdersDao.queryObject(nxOrdersId);
	}
	
	@Override
	public List<NxCommunityOrdersEntity> queryList(Map<String, Object> map){
		return nxCommunityOrdersDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityOrdersDao.queryTotal(map);
	}

	@Override
	public void save(NxCommunityOrdersEntity nxOrders){
		nxOrders.setNxCoSubFinished(0);

		nxCommunityOrdersDao.save(nxOrders);

		Integer ordersUserId = nxOrders.getNxCoUserId();

		Integer nxOrdersId = nxOrders.getNxCommunityOrdersId();
		List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();

		for (NxCommunityOrdersSubEntity sub : nxOrdersSubEntities) {
			//子订单
			sub.setNxCosOrdersId(nxOrdersId);
			sub.setNxCosStatus(3);
			sub.setNxCosBuyStatus(3);
			sub.setNxCosOrderUserId(ordersUserId);
			sub.setNxCosDistributerId(nxOrders.getNxCoDistributerId());
			sub.setNxCosCommunityId(nxOrders.getNxCoCommunityId());
			nxCommunityOrdersSubDao.update(sub);


//			//客户用户记录更新
			Integer nxOsCommunityGoodsId = sub.getNxCosCommunityGoodsId();
			Map<String, Object> map = new HashMap<>();
			map.put("nxOsCommunityGoodsId", nxOsCommunityGoodsId);
			map.put("nxCugUserId", ordersUserId);
			NxCustomerUserGoodsEntity userGoodsEntity = nxCustomerUserGoodsDao.queryByCommunityGoodsId(map);



			if(userGoodsEntity != null){
				userGoodsEntity.setNxCugLastOrderTime(formatWhatDayTime(0));
				userGoodsEntity.setNxCugLastOrderQuantity(sub.getNxCosQuantity());
				userGoodsEntity.setNxCugLastOrderStandard(sub.getNxCosStandard());
				userGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
				userGoodsEntity.setNxCugJoinMyTemplate(0);
				Integer nxCugOrderTimes = userGoodsEntity.getNxCugOrderTimes();
				userGoodsEntity.setNxCugOrderTimes(nxCugOrderTimes + 1);
				String nxCugOrderAmount = userGoodsEntity.getNxCugOrderAmount();
				String nxOsQuantity = sub.getNxCosQuantity();
				BigDecimal add = new BigDecimal(nxCugOrderAmount).add(new BigDecimal(nxOsQuantity));
				userGoodsEntity.setNxCugOrderAmount(add.toString());
				nxCustomerUserGoodsDao.update(userGoodsEntity);
			}else {
				NxCustomerUserGoodsEntity newUserGoodsEntity = new NxCustomerUserGoodsEntity();
				newUserGoodsEntity.setNxCugFirstOrderTime(formatWhatDay(0));
				newUserGoodsEntity.setNxCugOrderAmount(sub.getNxCosQuantity());
				newUserGoodsEntity.setNxCugCommunityGoodsId(sub.getNxCosCommunityGoodsId());
				newUserGoodsEntity.setNxCugOrderTimes(1);
				newUserGoodsEntity.setNxCugUserId(ordersUserId);
				newUserGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
				newUserGoodsEntity.setNxCugJoinMyTemplate(0);
				newUserGoodsEntity.setNxCugLastOrderQuantity(sub.getNxCosQuantity());
				newUserGoodsEntity.setNxCugLastOrderStandard(sub.getNxCosStandard());
				nxCustomerUserGoodsDao.save(newUserGoodsEntity);


			}

			//商品点击数加1
//			NxCommunityGoodsEntity disGoods = nxCommunityGoodsDao.queryObject(nxOsCommunityGoodsId);
//			disGoods.setNxCgGoodsTotalHits(disGoods.getNxCgGoodsTotalHits() + 1);

		}

	}


	
	@Override
	public void update(NxCommunityOrdersEntity nxOrders){

		nxCommunityOrdersDao.update(nxOrders);
	}
	
	@Override
	public void delete(Integer nxOrdersId){
		nxCommunityOrdersDao.delete(nxOrdersId);

		Map<String, Object> map = new HashMap<>();
		map.put("orderId", nxOrdersId);
		List<NxCommunityOrdersSubEntity> subEntities =  nxCommunityOrdersSubDao.querySubOrdersByParams(map);
		for (NxCommunityOrdersSubEntity sub :
				subEntities) {
			nxCommunityOrdersSubDao.delete(sub.getNxCommunityOrdersSubId());

		}

	}
	
	@Override
	public void deleteBatch(Integer[] nxOrdersIds){
		nxCommunityOrdersDao.deleteBatch(nxOrdersIds);
	}




	@Override
	public List<NxCommunityOrdersEntity> queryOrdersToWeigh(Map<String, Object> map) {
		return nxCommunityOrdersDao.queryOrdersToWeigh(map);
	}


	@Override
	public void updateSub(NxCommunityOrdersEntity nxOrders) {
		List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();
		for (NxCommunityOrdersSubEntity sub : nxOrdersSubEntities) {
			nxCommunityOrdersSubDao.update(sub);
		}

		Map<String, Object> map = new HashMap<>();
		map.put("nxOrdersId", nxOrders.getNxCommunityOrdersId());
		map.put("nxOrdersSubFinished", nxOrders.getNxCoSubFinished() );
		map.put("nxOrdersStatus", nxOrders.getNxCoStatus());
		map.put("nxOrdersAmount", nxOrders.getNxCoAmount());
		nxCommunityOrdersDao.update(map);
	}


	@Override
	public List<NxCommunityOrdersEntity> queryOrdersDetail(Map<String, Object> map) {
		return nxCommunityOrdersDao.queryOrdersDetail(map);
	}

	@Override
	public List<NxCommunityOrdersEntity> queryOrdersPaymentInformation(Map<String, Object> map) {
		return nxCommunityOrdersDao.queryOrdersPaymentInformation(map);
	}

    @Override
    public Integer updatePaymentStatus(Map<String, Object> map) {
		return nxCommunityOrdersDao.update(map);
	}

    @Override
    public List<NxCommunityOrdersEntity> queryCustomerOrder(Map<String, Object> map) {

		return nxCommunityOrdersDao.queryCustomerOrder(map);
    }

    @Override
    public List<NxCommunityOrdersEntity> queryDeliveryOrder(Map<String, Object> map) {
        return nxCommunityOrdersDao.queryDeliveryOrders(map);
    }

    @Override
    public List<NxCommunityOrdersEntity> queryOrderWithUserInfo(Map<String, Object> mapU) {
        return nxCommunityOrdersDao.queryOrderWithUserInfo(mapU);

    }

	@Override
	public NxCommunityOrdersEntity queryOrderByTradeNo(String ordersSn) {

		return nxCommunityOrdersDao.queryOrderByTradeNo(ordersSn);
	}

	@Override
	public NxCommunityOrdersEntity queryPindanDetail(Map<String, Object> map) {

		return nxCommunityOrdersDao.queryPindanDetail(map);
	}

	@Override
	public void justSave(NxCommunityOrdersEntity ordersEntity) {

		 nxCommunityOrdersDao.save(ordersEntity);
	}


}
