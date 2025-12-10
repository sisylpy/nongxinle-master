package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.service.GbDistributerPurchaseGoodsService;



@Service("gbDistributerPurchaseGoodsService")
public class GbDistributerPurchaseGoodsServiceImpl implements GbDistributerPurchaseGoodsService {

    @Autowired
	private com.nongxinle.dao.GbDistributerPurchaseGoodsDao gbDistributerPurchaseGoodsDao;

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



    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurchaseGoodsWithDetailByParams(Map<String, Object> map) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWithDetailByParams(map);
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
    public Double queryPurchaseGoodsWeightTotal(Map<String, Object> map1) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWeightTotal(map1);
    }

    @Override
    public String queryPurchaseGoodsPrice(Map<String, Object> map1) {

		return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsPrice(map1);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryGbFatherDisPurchaseGoods(Map<String, Object> map4) {

		return gbDistributerPurchaseGoodsDao.queryGbFatherDisPurchaseGoods(map4);
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

    @Override
    public int queryGbPurchaseOrderAmount(Map<String, Object> map1) {

		return gbDistributerPurchaseGoodsDao.queryGbPurchaseOrderAmount(map1);
    }


    @Override
    public List<GbDistributerGoodsEntity> queryDisTreeGoods(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryDisTreeGoods(map);
    }

    @Override
    public String queryPurchaseGoodsWeight(Map<String, Object> mapDay) {

	    return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsWeight(mapDay);
    }

    @Override
    public int queryPurchaseGoodsOrderCount(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsOrderCount(map);
    }

    @Override
    public Integer queryGbDisGoods(Map<String, Object> queryMap) {

	    return gbDistributerPurchaseGoodsDao.queryGbDisGoods(queryMap);
    }


    @Override
    public List<NxJrdhSupplierEntity> queryDisPurGoodsSupplierList(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryDisPurGoodsSupplierList(map);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> querySimplePurGoods(Map<String, Object> map4) {

	    return gbDistributerPurchaseGoodsDao.querySimplePurGoods(map4);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryOnlyPurGoods(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryOnlyPurGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryGrandGoodsByDisGoods(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryGrandGoodsByDisGoods(map);
    }

    @Override
    public List<GbDepartmentUserEntity> queryPurUserList(Map<String, Object> map) {
        return gbDistributerPurchaseGoodsDao.queryPurUserList(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopTimes(Map<String, Object> map) {
        return gbDistributerPurchaseGoodsDao.queryGbPurchaseGoodsTopTimes(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopSubtotal(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryGbPurchaseGoodsTopSubtotal(map);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryPurchaseGreatGrand(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryPurchaseGreatGrand(map);
    }

    @Override
    public double queryGbPurchaseSubtotalTopSubtotal(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryGbPurchaseSubtotalTopSubtotal(map);
    }

    @Override
    public Integer queryGbDisGoodsTreeCount(Map<String, Object> queryMap) {
        System.out.println("Service层开始执行queryGbDisGoodsTreeCount，参数: " + queryMap);
        try {
            Integer result = gbDistributerPurchaseGoodsDao.queryGbDisGoodsTreeCount(queryMap);
            System.out.println("Service层queryGbDisGoodsTreeCount执行完成，结果: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Service层queryGbDisGoodsTreeCount异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGbPurchaseGoodsTopPriceFluctuation(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryGbPurchaseGoodsTopPriceFluctuation(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisTreeGoodsWithPurList(Map<String, Object> queryMap) {

	    return gbDistributerPurchaseGoodsDao.queryDisTreeGoodsWithPurList(queryMap);
    }

    @Override
    public List<Map<String, Object>> debugQueryGoodsPriceData(Map<String, Object> map) {
        return gbDistributerPurchaseGoodsDao.debugQueryGoodsPriceData(map);
    }

    @Override
    public int queryGbGoodsCount(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryGbGoodsCount(map);
    }

    @Override
    public GbDistributerPurchaseGoodsEntity queryPurchaseGoodsLastItem(Map<String, Object> mapG) {

	    return gbDistributerPurchaseGoodsDao.queryPurchaseGoodsLastItem(mapG);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryReturnDisTreeGoodsWithPurList(Map<String, Object> map) {

	    return gbDistributerPurchaseGoodsDao.queryReturnDisTreeGoodsWithPurList(map);
    }


}
