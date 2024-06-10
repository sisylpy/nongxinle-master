package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentGoodsStockDao;
import com.nongxinle.service.GbDepartmentGoodsStockService;



@Service("gbDepartmentGoodsStockService")
public class GbDepartmentGoodsStockServiceImpl implements GbDepartmentGoodsStockService {
	@Autowired
	private GbDepartmentGoodsStockDao gbDepartmentGoodsStockDao;
	
	@Override
	public GbDepartmentGoodsStockEntity queryObject(Integer gbDepartmentGoodsStockId){
		return gbDepartmentGoodsStockDao.queryObject(gbDepartmentGoodsStockId);
	}
	
	@Override
	public List<GbDepartmentGoodsStockEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsStockDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsStockDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsStockEntity gbDepartmentGoodsStock){
		gbDepartmentGoodsStockDao.save(gbDepartmentGoodsStock);
	}
	
	@Override
	public void update(GbDepartmentGoodsStockEntity gbDepartmentGoodsStock){
		gbDepartmentGoodsStockDao.update(gbDepartmentGoodsStock);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsStockId){
		gbDepartmentGoodsStockDao.delete(gbDepartmentGoodsStockId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsStockIds){
		gbDepartmentGoodsStockDao.deleteBatch(gbDepartmentGoodsStockIds);
	}

    @Override
    public List<GbDepartmentGoodsStockEntity> queryGoodsStockByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryGoodsStockByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsStockByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryDisGoodsStockByParams(map);
    }

	@Override
	public List<GbDistributerGoodsEntity> queryDisGoodsStockDetailByParams(Map<String, Object> map) {
		return gbDepartmentGoodsStockDao.queryDisGoodsStockDetailByParams(map);
	}

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepGoodsStockByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryDepGoodsStockByParams(map);
    }

    @Override
    public Integer queryGoodsStockCount(Map<String, Object> map14) {

		return gbDepartmentGoodsStockDao.queryGoodsStockCount(map14);
    }

    @Override
    public Double queryDepGoodsRestTotal(Map<String, Object> map5) {

		return gbDepartmentGoodsStockDao.queryDepGoodsRestTotal(map5);
    }

    @Override
    public List<GbDepartmentGoodsStockEntity> queryStockListByParams(Integer depFatherId) {


		return gbDepartmentGoodsStockDao.queryStockListByParams(depFatherId);
    }

    @Override
    public Double queryDepGoodsSubtotal(Map<String, Object> map4) {

		return gbDepartmentGoodsStockDao.queryDepGoodsSubtotal(map4);
    }

    @Override
    public GbDepartmentGoodsStockEntity queryMinFullTimeForDayStock(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryMinFullTimeForDayStock(map);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> queryEveryDayOutStockShelfGoods(Map<String, Object> map1) {

		return gbDepartmentGoodsStockDao.queryEveryDayOutStockShelfGoods(map1);
    }

    @Override
    public GbDepartmentGoodsStockEntity queryMaxFullTimeForDayStock(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryMaxFullTimeForDayStock(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepStockDisFatherGoodsFather(Map<String, Object> map) {

		return gbDepartmentGoodsStockDao.queryDepStockDisFatherGoodsFather(map);
    }

    @Override
    public List<GbDepartmentGoodsStockEntity> queryGoodsStockWithReduceList(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryGoodsStockWithReduceList(map);
    }

    @Override
    public Double queryDepGoodsRestWeightTotal(Map<String, Object> map1) {

	    return gbDepartmentGoodsStockDao.queryDepGoodsRestWeightTotal(map1);
    }

    @Override
    public long queryGoodsStockTimeStamp(Map<String, Object> map0) {

	    return gbDepartmentGoodsStockDao.queryGoodsStockTimeStamp(map0);
    }

    @Override
    public List<GbDepartmentGoodsStockEntity> queryGoodsStockReduceByParams(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryGoodsStockReduceByParams(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepStockTreeFatherGoodsByParams(Map<String, Object> map) {
        return gbDepartmentGoodsStockDao.queryDepStockTreeFatherGoodsByParams(map);
    }

    @Override
    public Double queryStockSellingPriceTotal(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryStockSellingPriceTotal(map);
    }

    @Override
    public Double queryStockPriceTotal(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryStockPriceTotal(map);
    }


    @Override
    public Double queryDepStockWeightTotal(Map<String, Object> map) {
        return gbDepartmentGoodsStockDao.queryDepStockWeightTotal(map);
    }

    @Override
    public Double queryDepStockRestWeightTotal(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryDepStockRestWeightTotal(map);
    }

    @Override
    public List<GbDepartmentEntity> queryWhichDepHasStock(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryWhichDepHasStock(map);
    }

    @Override
    public Double queryStockCostRateTotal(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryStockCostRateTotal(map);
    }

    @Override
    public Double queryGoodsPriceTotal(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryGoodsPriceTotal(map);
    }

    @Override
    public Double queryGoodsPriceScale(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryGoodsPriceScale(map);
    }

    @Override
    public Double queryDepStockRestSubtotal(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepStockRestSubtotal(map);
    }

    @Override
    public TreeSet<GbDepartmentEntity> queryDepGoodsTreeDepartments(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepGoodsTreeDepartments(map);
    }

    @Override
    public String queryDepStockMaxDgsPrice(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepStockMaxDgsPrice(map);
    }

    @Override
    public String queryDepStockMinDgsPrice(Map<String, Object> map) {
        return gbDepartmentGoodsStockDao.queryDepStockMinDgsPrice(map);
    }

    @Override
    public double queryDepStockProfitSubtotal(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepStockProfitSubtotal(map);
    }

    @Override
    public double queryDepStockAfterProfitSubtotal(Map<String, Object> map) {

	    return gbDepartmentGoodsStockDao.queryDepStockAfterProfitSubtotal(map);
    }

    @Override
    public double queryDepStockProduceSellingSubtotal(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepStockProduceSellingSubtotal(map);
    }

    @Override
    public List<GbDepartmentGoodsStockEntity> queryDepStockListByParams(Map<String, Object> mapGoods) {
	    return gbDepartmentGoodsStockDao.queryDepStockListByParams(mapGoods);
    }

    @Override
    public GbDepartmentGoodsStockEntity queryReturnStockItemByOrderId(Integer gbDepartmentOrdersId) {
	    return gbDepartmentGoodsStockDao.queryReturnStockItemByOrderId(gbDepartmentOrdersId);
    }

    @Override
    public double queryDepStockReturnSubtotal(Map<String, Object> map) {
	    return gbDepartmentGoodsStockDao.queryDepStockReturnSubtotal(map);
    }

    @Override
    public Double queryDepStockReturnWeightTotal(Map<String, Object> disGoodsMap) {
	    return gbDepartmentGoodsStockDao.queryDepStockReturnWeightTotal(disGoodsMap);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsWithFromDepGoods(Map<String, Object> map0) {
	    return gbDepartmentGoodsStockDao.queryDisGoodsWithFromDepGoods(map0);
    }

    @Override
    public Double queryDepStockSubtotal(Map<String, Object> mapDisGoods) {
	    return gbDepartmentGoodsStockDao.queryDepStockSubtotal(mapDisGoods);
    }

    @Override
    public List<GbDepartmentEntity> queryStockDepWithFatherGoods(Map<String, Object> map0) {
	    return gbDepartmentGoodsStockDao.queryStockDepWithFatherGoods(map0);
    }


}
