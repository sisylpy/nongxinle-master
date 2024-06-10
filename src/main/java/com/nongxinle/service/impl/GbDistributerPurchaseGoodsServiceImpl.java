package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDistributerPurchaseGoodsEntity;
import com.nongxinle.service.GbDistributerPurchaseGoodsService;



@Service("gbDistributerPurchaseGoodsService")
public class GbDistributerPurchaseGoodsServiceImpl implements GbDistributerPurchaseGoodsService {
	@Autowired
	private com.nongxinle.dao.GbDistributerPurchaseGoodsDao gbDistributerPurchaseGoodsDao;


	@Override
	public List<GbDistributerFatherGoodsEntity> queryDisPurchaseGoods(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryDisPurchaseGoods(map);
	}




//	//////////////






	@Override
	public GbDistributerPurchaseGoodsEntity queryObject(Integer nxDistributerPurchaseGoods){
		return gbDistributerPurchaseGoodsDao.queryObject(nxDistributerPurchaseGoods);
	}
	
	@Override
	public List<GbDistributerPurchaseGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerPurchaseGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerPurchaseGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerPurchaseGoodsEntity nxDistributerPurchaseGoods){
		gbDistributerPurchaseGoodsDao.save(nxDistributerPurchaseGoods);
	}
	
	@Override
	public void update(GbDistributerPurchaseGoodsEntity nxDistributerPurchaseGoods){
		gbDistributerPurchaseGoodsDao.update(nxDistributerPurchaseGoods);
	}
	
	@Override
	public void delete(Integer nxDistributerPurchaseGoods){
		gbDistributerPurchaseGoodsDao.delete(nxDistributerPurchaseGoods);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerPurchaseGoodss){
		gbDistributerPurchaseGoodsDao.deleteBatch(nxDistributerPurchaseGoodss);
	}



//    @Override
//    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByUUID(String uuid) {
//
//		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsByUUID(uuid);
//    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByGoodsId(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsByGoodsId(map);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> purUserGetPurchaseGoods(Integer purUserId) {

		return gbDistributerPurchaseGoodsDao.queryPurUserPurchaseGoods(purUserId);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByBatchId(Integer purchaseBatchId) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsByBatchId(purchaseBatchId);
    }

	@Override
	public List<GbDistributerPurchaseGoodsEntity> queryForDisGoods(Map<String, Object> map2) {
		return  gbDistributerPurchaseGoodsDao.queryForDisGoods(map2);
	}

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsByParams(Map<String, Object> map2) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsByParams(map2);
    }

    @Override
    public int queryPurchaseGoodsTotal(Map<String, Object> map2) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsTotal(map2);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWithDetailByParams(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryStockPurchaseGoods(Map<String, Object> map4) {

		return gbDistributerPurchaseGoodsDao.queryStockPurchaseGoods(map4);
    }

    @Override
    public Integer queryGbPurchaseGoodsCount(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryGbPurchaseGoodsCount(map);
    }

    @Override
    public Double queryPurchaseGoodsSubTotal(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsSubTotal(map);
    }

    @Override
    public int queryPurchaseGoodsAmount(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsAmount(map);
    }

    @Override
    public Double queryPurchaseInventoryGoodsSubTotal(Map<String, Object> map) {
        
		return gbDistributerPurchaseGoodsDao.queryPurchaseInventoryGoodsSubTotal(map);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseInventoryGoodsList(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseInventoryGoodsList(map);
    }

    @Override
    public Double queryPurchaseGoodsWeightTotal(Map<String, Object> map1) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWeightTotal(map1);
    }

    @Override
    public String queryPurchaseGoodsPrice(Map<String, Object> map1) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsPrice(map1);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisPurchaseGoodsForNxDis(Map<String, Object> map4) {

		return gbDistributerPurchaseGoodsDao.queryDisPurchaseGoodsForNxDis(map4);
    }

    @Override
    public String queryPurGoodsMaxPrice(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurGoodsMaxPrice(map);
    }

	@Override
	public String queryPurGoodsMinPrice(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurGoodsMinPrice(map);
	}

    @Override
    public GbDistributerPurchaseGoodsEntity queryPurGoodsWithOrders(Integer id) {

		return gbDistributerPurchaseGoodsDao.queryPurGoodsWithOrders(id);
    }


}
