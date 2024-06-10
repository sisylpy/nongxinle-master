package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentGoodsStockReduceDao;
import com.nongxinle.service.GbDepartmentGoodsStockReduceService;



@Service("gbDepartmentGoodsStockReduceService")
public class GbDepartmentGoodsStockReduceServiceImpl implements GbDepartmentGoodsStockReduceService {
	@Autowired
	private GbDepartmentGoodsStockReduceDao gbDepartmentGoodsStockReduceDao;
	
	@Override
	public GbDepartmentGoodsStockReduceEntity queryObject(Integer gbDepartmentGoodsStockReduceId){
		return gbDepartmentGoodsStockReduceDao.queryObject(gbDepartmentGoodsStockReduceId);
	}
	
	@Override
	public List<GbDepartmentGoodsStockReduceEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsStockReduceEntity gbDepartmentGoodsStockReduce){
		gbDepartmentGoodsStockReduceDao.save(gbDepartmentGoodsStockReduce);
	}
	
	@Override
	public void update(GbDepartmentGoodsStockReduceEntity gbDepartmentGoodsStockReduce){
		gbDepartmentGoodsStockReduceDao.update(gbDepartmentGoodsStockReduce);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsStockReduceId){
		gbDepartmentGoodsStockReduceDao.delete(gbDepartmentGoodsStockReduceId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsStockReduceIds){
		gbDepartmentGoodsStockReduceDao.deleteBatch(gbDepartmentGoodsStockReduceIds);
	}

    @Override
    public GbDepartmentGoodsStockReduceEntity queryReduceCostByParams(Map<String, Object> map111) {
		return gbDepartmentGoodsStockReduceDao.queryReduceCostByParams(map111);
    }

    @Override
    public Integer queryReduceTypeCount(Map<String, Object> map12) {
		return gbDepartmentGoodsStockReduceDao.queryReduceTypeCount(map12);
    }

    @Override
    public Double queryReduceProduceTotal(Map<String, Object> map12) {
		return gbDepartmentGoodsStockReduceDao.queryReduceProduceTotal(map12);
    }

    @Override
    public Double queryReduceLossTotal(Map<String, Object> map121) {
		return gbDepartmentGoodsStockReduceDao.queryReduceLossTotal(map121);
    }

    @Override
    public Double queryReduceWasteTotal(Map<String, Object> map122) {
		return gbDepartmentGoodsStockReduceDao.queryReduceWasteTotal(map122);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryReduceGoodsTypeByParams(Map<String, Object> map121) {
		return gbDepartmentGoodsStockReduceDao.queryReduceGoodsTypeByParams(map121);
    }



    @Override
    public Double queryReduceReturnTotal(Map<String, Object> map1213) {
		return gbDepartmentGoodsStockReduceDao.queryReduceReturnTotal(map1213);
    }

	@Override
	public List<GbDepartmentGoodsStockReduceEntity> queryStockReduceListByParams(Map<String, Object> mapRed) {
		return gbDepartmentGoodsStockReduceDao.queryStockReduceListByParams(mapRed);
	}

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryReduceGoodsFatherTypeByParams(Map<String, Object> map1222) {
        return gbDepartmentGoodsStockReduceDao.queryReduceGoodsFatherTypeByParams(map1222);

    }

    @Override
    public Double queryReduceWasteWeightTotal(Map<String, Object> map122) {
		return gbDepartmentGoodsStockReduceDao.queryReduceWasteWeightTotal(map122);
    }

    @Override
    public Double queryReduceLossWeightTotal(Map<String, Object> map121) {
		return gbDepartmentGoodsStockReduceDao.queryReduceLossWeightTotal(map121);
    }

    @Override
    public Double queryReduceProduceWeightTotal(Map<String, Object> map123) {
		return gbDepartmentGoodsStockReduceDao.queryReduceProduceWeightTotal(map123);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryReduceTypeGoodsFather(Map<String, Object> map) {

		return gbDepartmentGoodsStockReduceDao.queryReduceTypeGoodsFather(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryReduceFatherGoods(Map<String, Object> map) {

		return gbDepartmentGoodsStockReduceDao.queryReduceFatherGoods(map);
    }

    @Override
    public TreeSet<GbDistributerGoodsEntity> queryGoodsStockRecordTreeByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockReduceDao.queryGoodsStockRecordTreeByParams(map);
    }

    @Override
    public GbDistributerGoodsEntity queryReduceGoodsTypeWorkByParams(Map<String, Object> map) {

	    return gbDepartmentGoodsStockReduceDao.queryReduceGoodsTypeWorkByParams(map);
    }



    @Override
    public Double queryReduceCostSubtotal(Map<String, Object> map1222) {

	    return gbDepartmentGoodsStockReduceDao.queryReduceCostSubtotal(map1222);
    }

    @Override
    public Double queryReduceCostWeightTotal(Map<String, Object> map1) {

	    return gbDepartmentGoodsStockReduceDao.queryReduceCostWeightTotal(map1);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryDepStockReduceTreeFatherGoodsByParams(Map<String, Object> map) {

	    return gbDepartmentGoodsStockReduceDao.queryDepStockReduceTreeFatherGoodsByParams(map);
    }

    @Override
    public double queryReduceReturnWeightTotal(Map<String, Object> mapDepStock) {

	    return gbDepartmentGoodsStockReduceDao.queryReduceReturnWeightTotal(mapDepStock);
    }

    @Override
    public List<GbDepartmentEntity> queryReduceDepartment(Map<String, Object> disGoodsMap) {


	    return gbDepartmentGoodsStockReduceDao.queryReduceDepartment(disGoodsMap);
    }

    @Override
    public List<GbDistributerPurchaseGoodsEntity> queryPurGoodsForCost(Map<String, Object> map) {

	    return gbDepartmentGoodsStockReduceDao.queryPurGoodsForCost(map);
    }


}
