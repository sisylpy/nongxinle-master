package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentGoodsStockEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentGoodsStockRecordDao;
import com.nongxinle.entity.GbDepartmentGoodsStockRecordEntity;
import com.nongxinle.service.GbDepartmentGoodsStockRecordService;



@Service("gbDepartmentGoodsStockRecordService")
public class GbDepartmentGoodsStockRecordServiceImpl implements GbDepartmentGoodsStockRecordService {
	@Autowired
	private GbDepartmentGoodsStockRecordDao gbDepartmentGoodsStockRecordDao;
	
	@Override
	public GbDepartmentGoodsStockRecordEntity queryObject(Integer gbDepartmentGoodsStockRecordId){
		return gbDepartmentGoodsStockRecordDao.queryObject(gbDepartmentGoodsStockRecordId);
	}
	
	@Override
	public List<GbDepartmentGoodsStockRecordEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsStockRecordDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsStockRecordDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsStockRecordEntity gbDepartmentGoodsStockRecord){
		gbDepartmentGoodsStockRecordDao.save(gbDepartmentGoodsStockRecord);
	}
	
	@Override
	public void update(GbDepartmentGoodsStockRecordEntity gbDepartmentGoodsStockRecord){
		gbDepartmentGoodsStockRecordDao.update(gbDepartmentGoodsStockRecord);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsStockRecordId){
		gbDepartmentGoodsStockRecordDao.delete(gbDepartmentGoodsStockRecordId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsStockRecordIds){
		gbDepartmentGoodsStockRecordDao.deleteBatch(gbDepartmentGoodsStockRecordIds);
	}

    @Override
    public Integer queryGoodsStockRecordCount(Map<String, Object> map1451) {

		return gbDepartmentGoodsStockRecordDao.queryGoodsStockRecordCount(map1451);
    }

    @Override
    public Double queryGoodsStockRecordSubtotal(Map<String, Object> map42) {

		return gbDepartmentGoodsStockRecordDao.queryGoodsStockRecordSubtotal(map42);
    }

    @Override
    public List<GbDepartmentGoodsStockRecordEntity> queryGoodsStockListByParams(Map<String, Object> map5) {

		return gbDepartmentGoodsStockRecordDao.queryGoodsStockListByParams(map5);
    }

    @Override
    public Double queryManyTotal(Map<String, Object> map0) {

		return gbDepartmentGoodsStockRecordDao.queryManyTotal(map0);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepStockRecordDisFatherGoodsFather(Map<String, Object> map0) {

		return gbDepartmentGoodsStockRecordDao.queryDepStockRecordDisFatherGoodsFather(map0);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockRecordDao.queryDisGoodsByParams(map);
    }

    @Override
    public Double queryGoodsStockRecordWeightTotal(Map<String, Object> map1) {

		return gbDepartmentGoodsStockRecordDao.queryGoodsStockRecordWeightTotal(map1);
    }

    @Override
    public List<GbDepartmentGoodsStockRecordEntity> queryDepGoodsStockRecordDetailByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockRecordDao.queryDepGoodsStockRecordDetailByParams(map);
    }


}
