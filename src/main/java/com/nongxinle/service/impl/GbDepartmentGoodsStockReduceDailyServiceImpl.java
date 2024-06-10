package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentGoodsStockReduceDailyDao;
import com.nongxinle.entity.GbDepartmentGoodsStockReduceDailyEntity;
import com.nongxinle.service.GbDepartmentGoodsStockReduceDailyService;



@Service("gbDepartmentGoodsStockReduceDailyService")
public class GbDepartmentGoodsStockReduceDailyServiceImpl implements GbDepartmentGoodsStockReduceDailyService {
	@Autowired
	private GbDepartmentGoodsStockReduceDailyDao gbDepartmentGoodsStockReduceDailyDao;
	
	@Override
	public GbDepartmentGoodsStockReduceDailyEntity queryObject(Integer gbDepartmentGoodsStockReduceDailyId){
		return gbDepartmentGoodsStockReduceDailyDao.queryObject(gbDepartmentGoodsStockReduceDailyId);
	}
	
	@Override
	public List<GbDepartmentGoodsStockReduceDailyEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceDailyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceDailyDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsStockReduceDailyEntity gbDepartmentGoodsStockReduceDaily){
		gbDepartmentGoodsStockReduceDailyDao.save(gbDepartmentGoodsStockReduceDaily);
	}
	
	@Override
	public void update(GbDepartmentGoodsStockReduceDailyEntity gbDepartmentGoodsStockReduceDaily){
		gbDepartmentGoodsStockReduceDailyDao.update(gbDepartmentGoodsStockReduceDaily);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsStockReduceDailyId){
		gbDepartmentGoodsStockReduceDailyDao.delete(gbDepartmentGoodsStockReduceDailyId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsStockReduceDailyIds){
		gbDepartmentGoodsStockReduceDailyDao.deleteBatch(gbDepartmentGoodsStockReduceDailyIds);
	}

    @Override
    public List<GbDepartmentGoodsStockReduceDailyEntity> queryDepGoodsStockReduceDailyListByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockReduceDailyDao.queryDepGoodsStockReduceDailyListByParams(map);
    }

    @Override
    public int queryListTotal(Map<String, Object> map1) {

		return gbDepartmentGoodsStockReduceDailyDao.queryListTotal(map1);
    }

    @Override
    public Double queryRestWeightTotal(Map<String, Object> map1) {

		return gbDepartmentGoodsStockReduceDailyDao.queryRestWeightTotal(map1);
    }

    @Override
    public GbDepartmentGoodsStockReduceDailyEntity queryReduceDailyItem(Map<String, Object> map1) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceDailyItem(map1);

    }

    @Override
    public Integer queryReduceDailyCount(Map<String, Object> map1222) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceDailyCount(map1222);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryReduceGoodsFatherTypeByParams(Map<String, Object> map1222) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceGoodsFatherTypeByParams(map1222);
    }

    @Override
    public Double queryReduceProfitSubtotal(Map<String, Object> map1222) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceProfitSubtotal(map1222);
    }

    @Override
    public Double queryReduceSellingSubtotal(Map<String, Object> map1222) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceSellingSubtotal(map1222);
    }

    @Override
    public TreeSet<GbDistributerGoodsEntity> queryGoodsStockRecordTreeByParams(Map<String, Object> map) {

		return gbDepartmentGoodsStockReduceDailyDao.queryGoodsStockRecordTreeByParams(map);
    }

    @Override
    public TreeSet<GbDepartmentEntity> queryWhichDepsHaveDailyTotal(Map<String, Object> mapDep) {

		return gbDepartmentGoodsStockReduceDailyDao.queryWhichDepsHaveDailyTotal(mapDep);
    }

    @Override
    public Double queryReduceFinishCount(Map<String, Object> map0) {

		return gbDepartmentGoodsStockReduceDailyDao.queryReduceFinishCount(map0);

    }

}
